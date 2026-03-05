package com.example.simpleblog.controller;

import com.example.simpleblog.entity.Article;
import com.example.simpleblog.entity.ArticleStatus;
import com.example.simpleblog.entity.Category;
import com.example.simpleblog.form.ArticleForm;
import com.example.simpleblog.repository.ArticleRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ArticleController {

    private final ArticleRepository articleRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // 公開記事一覧 + 検索（タイトル部分一致）
    @GetMapping("/articles")
    public String index(@RequestParam(required = false) String q, Model model) {
        List<Article> articles;
        if (q == null || q.isBlank()) {
            articles = articleRepository.findByStatusOrderByCreatedAtDesc(ArticleStatus.PUBLIC);
        } else {
            articles = articleRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(ArticleStatus.PUBLIC, q.trim());
        }
        model.addAttribute("articles", articles);
        model.addAttribute("q", q);
        return "articles/index";
    }

    // 詳細（PUBLICは誰でもOK。DRAFTは本人だけ見えるようにする）
    @GetMapping("/articles/{id}")
    public String show(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        Optional<Article> opt = articleRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/articles";

        Article article = opt.get();

        if (article.getStatus() == ArticleStatus.DRAFT) {
            if (userId == null || !userId.equals(article.getUserId())) {
                return "redirect:/login"; // or "redirect:/articles"
            }
        }

        model.addAttribute("article", article);
        return "articles/show";
    }

    // 新規作成フォーム（Interceptorでログイン必須）
    @GetMapping("/articles/new")
    public String newForm(Model model) {
        model.addAttribute("articleForm", defaultForm());
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", ArticleStatus.values());
        return "articles/new";
    }

    // 作成
    @PostMapping("/articles")
    public String create(@Valid @ModelAttribute ArticleForm articleForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) throws IOException {

        validateImage(articleForm.getImage(), bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("statuses", ArticleStatus.values());
            return "articles/new";
        }

        Long userId = (Long) session.getAttribute("userId");

        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(articleForm.getTitle());
        article.setBody(articleForm.getBody());
        article.setCategory(articleForm.getCategory());
        article.setStatus(articleForm.getStatus());

        String imagePath = saveImageIfPresent(articleForm.getImage());
        article.setImagePath(imagePath);

        Article saved = articleRepository.save(article);
        return "redirect:/articles/" + saved.getId();
    }

    // 編集フォーム（Interceptorでログイン必須）
    @GetMapping("/articles/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) return "redirect:/articles";


        // 本人のみ
        if (userId == null || !userId.equals(article.getUserId())) {
            return "redirect:/articles/" + id;
        }


        ArticleForm form = new ArticleForm();
        form.setTitle(article.getTitle());
        form.setBody(article.getBody());
        form.setCategory(article.getCategory());
        form.setStatus(article.getStatus());

        model.addAttribute("articleId", id);
        model.addAttribute("article", article);
        model.addAttribute("articleForm", form);
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", ArticleStatus.values());
        return "articles/edit";
    }

    // 更新
    @PostMapping("/articles/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ArticleForm articleForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) throws IOException {

        Long userId = (Long) session.getAttribute("userId");

        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) return "redirect:/articles";

        if (userId == null || !userId.equals(article.getUserId())) {
            return "redirect:/articles/" + id;
        }

        validateImage(articleForm.getImage(), bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("articleId", id);
            model.addAttribute("article", article);
            model.addAttribute("categories", Category.values());
            model.addAttribute("statuses", ArticleStatus.values());
            return "articles/edit";
        }

        article.setTitle(articleForm.getTitle());
        article.setBody(articleForm.getBody());
        article.setCategory(articleForm.getCategory());
        article.setStatus(articleForm.getStatus());

        // 新しい画像が来た時だけ差し替え
        String newImagePath = saveImageIfPresent(articleForm.getImage());
        if (newImagePath != null) {
            article.setImagePath(newImagePath);
        }

        articleRepository.save(article);
        return "redirect:/articles/" + id;
    }

    // 削除（本人のみ）
    @PostMapping("/articles/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) return "redirect:/articles";

        if (userId == null || !userId.equals(article.getUserId())) {
            return "redirect:/articles/" + id;
        }

        articleRepository.delete(article);
        return "redirect:/articles";
    }

    // 自分の下書き一覧（任意要件）
    @GetMapping("/me/drafts")
    public String drafts(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        List<Article> drafts = articleRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, ArticleStatus.DRAFT);
        model.addAttribute("drafts", drafts);
        return "me/drafts";
    }

    // ---- helpers ----

    private ArticleForm defaultForm() {
        ArticleForm f = new ArticleForm();
        f.setCategory(Category.TECH);
        f.setStatus(ArticleStatus.PUBLIC);
        return f;
    }

    private void validateImage(MultipartFile image, BindingResult bindingResult) {
        if (image == null || image.isEmpty()) return;

        String contentType = image.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            bindingResult.rejectValue("image", "image.type", "画像はjpg/pngのみです");
            return;
        }

        long max = 2L * 1024 * 1024;
        if (image.getSize() > max) {
            bindingResult.rejectValue("image", "image.size", "画像サイズは2MBまでです");
        }
    }

    private String saveImageIfPresent(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) return null;

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String original = image.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf(".")) : "";
        String filename = UUID.randomUUID() + ext;

        Path target = dir.resolve(filename);
        Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // ブラウザから参照するパス
        return "/uploads/" + filename;
    }
}
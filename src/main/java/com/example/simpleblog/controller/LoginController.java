package com.example.simpleblog.controller;

import com.example.simpleblog.entity.User;
import com.example.simpleblog.form.LoginForm;
import com.example.simpleblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<User> userOpt = userRepository.findByEmail(form.getEmail());
        if (userOpt.isEmpty()) {
            model.addAttribute("loginError", "メールまたはパスワードが違います");
            return "login";
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(form.getPassword(), user.getPasswordHash())) {
            model.addAttribute("loginError", "メールまたはパスワードが違います");
            return "login";
        }

        // ログイン成功：sessionにuserId保存
        session.setAttribute("userId", user.getId());

        // いったん記事一覧へ（次で作る）
        return "redirect:/articles";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}


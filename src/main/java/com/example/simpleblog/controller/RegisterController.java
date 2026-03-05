package com.example.simpleblog.controller;

import com.example.simpleblog.entity.User;
import com.example.simpleblog.form.RegisterForm;
import com.example.simpleblog.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegisterController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult,
            Model model
    ) {
        // 入力バリデーション
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // メール重複チェック
        if (userRepository.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "このメールアドレスは既に登録されています");
            return "register";
        }

        // 保存（パスワードはハッシュ化）
        User user = new User();
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));

        userRepository.save(user);

        // 次はログイン画面へ（ログインは次ステップで作る）
        return "redirect:/login";
    }
}
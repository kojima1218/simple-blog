package com.example.simpleblog.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "メールは必須です")
    @Email(message = "メール形式が正しくありません")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
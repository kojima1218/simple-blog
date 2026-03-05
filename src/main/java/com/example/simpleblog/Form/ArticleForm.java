package com.example.simpleblog.form;

import com.example.simpleblog.entity.ArticleStatus;
import com.example.simpleblog.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class ArticleForm {

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String body;

    @NotNull
    private Category category;

    @NotNull
    private ArticleStatus status;

    // 画像は任意
    private MultipartFile image;
}
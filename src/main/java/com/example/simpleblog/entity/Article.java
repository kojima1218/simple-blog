package com.example.simpleblog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "articles")
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false, length=100)
    private String title;

    @Column(nullable=false, length=5000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ArticleStatus status;

    @Column(name="image_path", length=255)
    private String imagePath;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
package com.example.simpleblog.repository;

import com.example.simpleblog.entity.Article;
import com.example.simpleblog.entity.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByStatusOrderByCreatedAtDesc(ArticleStatus status);

    List<Article> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(ArticleStatus status, String keyword);

    Optional<Article> findByIdAndStatus(Long id, ArticleStatus status);

    List<Article> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ArticleStatus status);



}
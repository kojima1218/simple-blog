package com.example.simpleblog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        Long userId = (session == null) ? null : (Long) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
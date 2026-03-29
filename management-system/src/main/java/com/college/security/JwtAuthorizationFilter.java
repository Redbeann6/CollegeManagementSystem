package com.college.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.college.model.User;
import com.college.repository.UserRepository;
import com.college.model.User;
import com.college.service.UserService;
import com.college.util.JwtUtils;

import org.springframework.stereotype.Component;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);
    private JwtUtils jwtUtils;
    private UserDetailsService userDetailsService;
    private UserRepository userRepository;
    private com.college.service.UserService userService;

    public JwtAuthorizationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.err.println("JwtAuthorizationFilter.doFilterInternal called");
        System.err.println("Request URI: " + request.getRequestURI());
        System.err.println("Servlet Path: " + request.getServletPath());
        System.err.flush();

        // 获取请求的ServletPath
        String servletPath = request.getServletPath();
        
        // 检查是否是不需要认证的请求
        if (servletPath.startsWith("/auth/") || servletPath.startsWith("/api/avatar") || servletPath.startsWith("/avatar/")) { // 检查认证相关路径和头像访问路径
            System.err.println("Public path detected, skipping JWT filter");
            System.err.flush();
            filterChain.doFilter(request, response);
            return;
        }

        // 获取Authorization头
        String authorizationHeader = request.getHeader("Authorization");
        System.err.println("Authorization header: " + authorizationHeader);
        System.err.flush();

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            System.err.println("Token extracted: " + token);
            System.err.flush();

            try {
                // 验证JWT令牌
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    System.err.println("Token valid, username: " + username);
                    System.err.flush();

                    // 从数据库中获取用户信息
                    User user = userRepository.findByUsername(username).orElse(null);
                    if (user == null) {
                        logger.warn("用户不存在: " + username);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 检查用户是否被禁用
                    if (!user.isEnabled()) {
                        logger.warn("用户已被禁用: " + username);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 直接使用从数据库获取的用户对象
                    UserDetails userDetails = user;
                    if (userDetails != null) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        System.err.println("Authentication created: " + authentication);
                        System.err.flush();

                        // 设置认证信息到安全上下文
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    System.err.println("Token validation failed");
                    System.err.flush();
                }
            } catch (Exception e) {
                System.err.println("Exception in JWT filter: " + e.getMessage());
                e.printStackTrace(System.err);
                System.err.flush();
            }
        } else {
            System.err.println("No valid authorization header");
            System.err.flush();
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
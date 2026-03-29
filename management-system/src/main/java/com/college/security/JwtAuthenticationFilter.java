package com.college.security;

import com.college.dto.LoginRequest;
import com.college.dto.LoginResponse;
import com.college.dto.Response;
import com.college.dto.UserDTO;
import com.college.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    // 构造函数
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, 
                                 JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        // 设置默认的登录请求URL
        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            System.err.println("[DEBUG] JwtAuthenticationFilter.attemptAuthentication called");
            System.err.println("[DEBUG] Request URL: " + request.getRequestURL());
            System.err.println("[DEBUG] Request method: " + request.getMethod());
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            System.err.println("[DEBUG] Parsed login request: username=" + loginRequest.getUsername() + ", password=" + loginRequest.getPassword());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            System.err.println("[DEBUG] Created authentication token: " + authToken);
            Authentication authentication = authenticationManager.authenticate(authToken);
            System.err.println("[DEBUG] Authentication result: " + authentication);
            return authentication;
        } catch (IOException e) {
            System.err.println("[DEBUG] IOException in attemptAuthentication: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException("登录请求数据解析失败", e);
        } catch (AuthenticationException e) {
            System.err.println("[DEBUG] AuthenticationException in attemptAuthentication: " + e.getMessage());
            e.printStackTrace(System.err);
            throw e;
        } catch (Exception e) {
            System.err.println("[DEBUG] Exception in attemptAuthentication: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException("认证过程中发生错误", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String username = userDetails.getUsername();
        String jwt = jwtUtils.generateToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        // 简化响应，不进行复杂的用户信息处理
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setRole(null); // 暂时设置为null

        LoginResponse loginResponse = new LoginResponse(jwt, refreshToken, userDTO);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = response.getWriter();
        writer.write(new ObjectMapper().writeValueAsString(Response.success(loginResponse)));
        writer.flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 使用401表示认证失败，而不是403
        PrintWriter writer = response.getWriter();
        writer.write(new ObjectMapper().writeValueAsString(Response.error("用户名或密码错误")));
        writer.flush();
    }
}
package com.college.controller;

import com.college.dto.LoginRequest;
import com.college.dto.LoginResponse;
import com.college.dto.RegisterRequest;
import com.college.dto.Response;
import com.college.dto.UserDTO;
import com.college.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") // 移除重复的/api前缀，因为已经在context-path中配置了
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        System.err.println("[DEBUG] AuthController.login called with username: " + loginRequest.getUsername() + ", password: " + loginRequest.getPassword());
        System.err.flush();
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            System.err.println("[DEBUG] AuthController.login successful");
            System.err.flush();
            return ResponseEntity.ok(Response.success("登录成功", loginResponse));
        } catch (Exception e) {
            System.err.println("[DEBUG] AuthController.login error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.err.flush();
            return ResponseEntity.status(401).body(Response.error("用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Response<UserDTO>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.err.println("[DEBUG] AuthController.register called");
            UserDTO userDTO = authService.register(registerRequest);
            System.err.println("[DEBUG] AuthController.register successful");
            return ResponseEntity.ok(Response.success("注册成功", userDTO));
        } catch (Exception e) {
            System.err.println("[DEBUG] AuthController.register error: " + e.getMessage());
            e.printStackTrace(System.err);
            // 返回详细的错误信息，帮助定位问题
            return ResponseEntity.status(500).body(Response.error("注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<String>> refreshToken(@RequestParam String refreshToken) {
        try {
            String newToken = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(Response.success("令牌刷新成功", newToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Response.error("无效的刷新令牌"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestHeader("Authorization") String token) {
        try {
            // 从Bearer token中提取实际token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            authService.logout(token);
            return ResponseEntity.ok(Response.success("退出成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Response.success("退出成功", null));
        }
    }
}
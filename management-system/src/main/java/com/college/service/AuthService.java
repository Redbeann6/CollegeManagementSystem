package com.college.service;

import com.college.dto.LoginRequest;
import com.college.dto.LoginResponse;
import com.college.dto.RegisterRequest;
import com.college.dto.UserDTO;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    UserDTO register(RegisterRequest registerRequest);
    String refreshToken(String refreshToken);
    void logout(String token);
}
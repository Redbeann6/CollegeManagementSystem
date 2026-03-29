package com.college.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicTestController {

    // 完全公开的测试端点
    @GetMapping("/test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Public test endpoint is working!");
    }
}

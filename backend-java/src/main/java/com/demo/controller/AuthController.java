package com.demo.controller;

import com.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            Map<String, Object> result = authService.login(username, password);
            return ResponseEntity.ok(Map.of("code", 200, "data", result, "msg", "登录成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("code", 401, "msg", e.getMessage()));
        }
    }
}

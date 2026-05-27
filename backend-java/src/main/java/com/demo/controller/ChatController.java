package com.demo.controller;

import com.demo.service.ChatService;
import com.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    @GetMapping("/sse")
    public SseEmitter chatSse(@RequestParam String message,
                              @RequestParam String token) {
        // 从 token 解析用户信息
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        return chatService.chat(userId, message, role);
    }
}

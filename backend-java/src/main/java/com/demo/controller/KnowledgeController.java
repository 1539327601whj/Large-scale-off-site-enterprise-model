package com.demo.controller;

import com.demo.entity.KnowledgeDoc;
import com.demo.entity.PromptConfig;
import com.demo.service.KnowledgeService;
import com.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final JwtUtil jwtUtil;

    @PostMapping("/knowledge/upload")
    public ResponseEntity<?> upload(@RequestBody Map<String, String> request,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        String title = request.get("title");
        String content = request.get("content");
        Map<String, Object> result = knowledgeService.uploadDocument(title, content, userId);
        return ResponseEntity.ok(Map.of("code", 200, "data", result, "msg", "上传成功"));
    }

    @GetMapping("/knowledge/list")
    public ResponseEntity<?> list() {
        List<KnowledgeDoc> docs = knowledgeService.listDocuments();
        return ResponseEntity.ok(Map.of("code", 200, "data", docs));
    }

    @DeleteMapping("/knowledge/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        knowledgeService.deleteDocument(id, userId);
        return ResponseEntity.ok(Map.of("code", 200, "msg", "删除成功"));
    }

    @PostMapping("/prompt")
    public ResponseEntity<?> savePrompt(@RequestBody Map<String, String> request,
                                        @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        String systemPrompt = request.get("system_prompt");
        knowledgeService.savePrompt("default", systemPrompt, userId);
        return ResponseEntity.ok(Map.of("code", 200, "msg", "提示词已保存"));
    }

    @GetMapping("/prompt")
    public ResponseEntity<?> getPrompt() {
        PromptConfig config = knowledgeService.getPrompt("default");
        return ResponseEntity.ok(Map.of("code", 200, "data", config));
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}

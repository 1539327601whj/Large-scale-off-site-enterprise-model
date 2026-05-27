package com.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SensitiveWordService sensitiveWordService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.backend.url}")
    private String pythonBackendUrl;

    private static final int MAX_HISTORY = 10; // Redis 保留最近 10 轮对话
    private static final long SSE_TIMEOUT = 120_000L;

    public SseEmitter chat(Long userId, String message, String role) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        CompletableFuture.runAsync(() -> {
            try {
                // 1. 敏感词过滤
                if (sensitiveWordService.containsSensitiveWord(message)) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("您的消息包含敏感内容，请修改后重试。"));
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                    return;
                }

                // 2. 从 Redis 获取历史上下文
                List<Map<String, String>> history = getHistory(userId);

                // 3. 调用 Python RAG 后端（流式）
                String jsonBody = objectMapper.writeValueAsString(Map.of(
                        "message", message,
                        "history", history
                ));

                URL url = new URL(pythonBackendUrl + "/api/rag/stream");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout((int) SSE_TIMEOUT);

                // 写入请求体并关闭输出流（必须关闭，否则服务端不会处理请求）
                try (var os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                // 检查响应码
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    log.error("Python 后端返回错误码: {}", responseCode);
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Python 后端异常，状态码: " + responseCode));
                    emitter.complete();
                    return;
                }

                // 4. 逐行读取 Python 的 SSE 响应，转发给前端
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                break;
                            }
                            emitter.send(SseEmitter.event().name("message").data(data));
                        }
                    }
                }

                // 5. 将本轮对话存入 Redis
                addToHistory(userId, "user", message);
                // 注意：assistant 的回复在前端拼接，这里我们存一个标记
                // 完整回复需要前端配合回传，这里简化处理

                emitter.complete();

            } catch (Exception e) {
                log.error("SSE 对话异常", e);
                try {
                    String errMsg = "服务异常";
                    if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                        errMsg = "Python 后端未启动或 Ollama 服务未运行，请检查服务状态。";
                    }
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(errMsg));
                } catch (IOException ex) {
                    log.error("发送错误消息失败", ex);
                }
                emitter.complete();
            }
        });

        emitter.onCompletion(() -> log.info("SSE 连接完成, userId={}", userId));
        emitter.onTimeout(() -> log.warn("SSE 连接超时, userId={}", userId));
        emitter.onError(e -> log.error("SSE 连接异常, userId={}", userId, e));

        return emitter;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getHistory(Long userId) {
        String key = "chat:history:" + userId;
        List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
        List<Map<String, String>> history = new ArrayList<>();
        if (rawList != null) {
            for (Object item : rawList) {
                try {
                    if (item instanceof String) {
                        Map<String, String> map = objectMapper.readValue((String) item, Map.class);
                        history.add(map);
                    } else if (item instanceof Map) {
                        history.add((Map<String, String>) item);
                    }
                } catch (Exception e) {
                    log.warn("解析 Redis 历史记录失败: {}", e.getMessage());
                }
            }
        }
        return history;
    }

    private void addToHistory(Long userId, String role, String content) {
        String key = "chat:history:" + userId;
        try {
            String entry = objectMapper.writeValueAsString(Map.of("role", role, "content", content));
            redisTemplate.opsForList().rightPush(key, entry);
        } catch (Exception e) {
            log.warn("存储 Redis 历史记录失败: {}", e.getMessage());
        }
        // 限制长度
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > MAX_HISTORY * 2) {
            redisTemplate.opsForList().trim(key, -MAX_HISTORY * 2, -1);
        }
    }

    public void saveAssistantReply(Long userId, String content) {
        addToHistory(userId, "assistant", content);
    }
}

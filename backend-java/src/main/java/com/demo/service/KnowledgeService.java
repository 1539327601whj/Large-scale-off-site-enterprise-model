package com.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.entity.KnowledgeDoc;
import com.demo.entity.PromptConfig;
import com.demo.mapper.KnowledgeDocMapper;
import com.demo.mapper.PromptConfigMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocMapper knowledgeDocMapper;
    private final PromptConfigMapper promptConfigMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.backend.url}")
    private String pythonBackendUrl;

    public Map<String, Object> uploadDocument(String title, String content, Long userId) {
        // 1. 存入 MySQL
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(title);
        doc.setContent(content);
        doc.setCreatedBy(userId);
        knowledgeDocMapper.insert(doc);

        // 2. 调用 Python 上传到 ChromaDB
        try {
            Map<String, String> body = Map.of("title", title, "content", content);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonBackendUrl + "/api/rag/upload", body, Map.class);
        } catch (Exception e) {
            // Python 端失败不影响 MySQL 记录
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", doc.getId());
        result.put("title", title);
        return result;
    }

    public List<KnowledgeDoc> listDocuments() {
        return knowledgeDocMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDoc>()
                        .orderByDesc(KnowledgeDoc::getCreatedAt));
    }

    public void deleteDocument(Long id, Long userId) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (doc != null) {
            knowledgeDocMapper.deleteById(id);
            // 调用 Python 删除向量
            try {
                restTemplate.delete(pythonBackendUrl + "/api/rag/doc/" + id);
            } catch (Exception ignored) {}
        }
    }

    public void savePrompt(String name, String systemPrompt, Long userId) {
        // 查找已有配置，有则更新，无则新增
        PromptConfig config = promptConfigMapper.selectOne(
                new LambdaQueryWrapper<PromptConfig>()
                        .eq(PromptConfig::getName, name));
        if (config != null) {
            config.setSystemPrompt(systemPrompt);
            promptConfigMapper.updateById(config);
        } else {
            config = new PromptConfig();
            config.setName(name);
            config.setSystemPrompt(systemPrompt);
            config.setCreatedBy(userId);
            promptConfigMapper.insert(config);
        }

        // 同步到 Python 端
        try {
            restTemplate.postForEntity(
                    pythonBackendUrl + "/api/rag/prompt",
                    Map.of("system_prompt", systemPrompt),
                    Map.class);
        } catch (Exception ignored) {}
    }

    public PromptConfig getPrompt(String name) {
        return promptConfigMapper.selectOne(
                new LambdaQueryWrapper<PromptConfig>()
                        .eq(PromptConfig::getName, name));
    }
}

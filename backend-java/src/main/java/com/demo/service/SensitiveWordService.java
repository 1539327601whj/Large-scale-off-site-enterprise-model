package com.demo.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SensitiveWordService {

    private Pattern pattern;

    @PostConstruct
    public void init() {
        // demo 阶段敏感词库，可后续从数据库/文件加载
        List<String> words = Arrays.asList(
                "暴力", "赌博", "毒品", "恐怖", "色情"
        );
        String regex = String.join("|", words);
        this.pattern = Pattern.compile(regex);
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return false;
        return pattern.matcher(text).find();
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        return pattern.matcher(text).replaceAll("***");
    }
}

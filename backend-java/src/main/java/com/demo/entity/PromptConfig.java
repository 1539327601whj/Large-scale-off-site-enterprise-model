package com.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prompt_config")
public class PromptConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String systemPrompt;
    private Long createdBy;
    private LocalDateTime updatedAt;
}

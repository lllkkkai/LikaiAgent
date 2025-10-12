package com.lllkkk.ai.agent.modules.log.handle.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.kimi")
public class AIConfig {

    /**
     * Kimi API密钥
     */
    private String apiKey;

    /**
     * Kimi API基础URL
     */
    private String baseUrl = "https://api.moonshot.cn/v1";

    /**
     * 使用的模型名称
     */
    private String model = "moonshot-v1-8k";

    /**
     * 最大令牌数
     */
    private int maxTokens = 2000;

    /**
     * 温度参数（创造性）
     */
    private double temperature = 0.3;
}
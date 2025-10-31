package com.lllkkk.ai.agent.modules.log.handle.controller;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.AnalysisResult;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.AIAnalyzer;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.LogFilter;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.LogParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志分析控制器
 * 提供REST API接口用于接收日志字符串并进行AI分析
 */
@Slf4j
@RestController
@RequestMapping("/api/log-analysis")
@RequiredArgsConstructor
public class LogAnalysisController {

    private final LogParser logParser;
    private final LogFilter logFilter;
    private final AIAnalyzer aiAnalyzer;

    /**
     * 分析日志字符串
     *
     * @param logContent 日志内容字符串
     * @return 分析结果
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeLog(@RequestBody String logContent) {
        log.info("接收到日志分析请求，内容长度: {}", logContent.length());

        try {
            // 1. 解析日志
            LogRecord parsedRecord = logParser.parse(logContent);
            if (parsedRecord == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("无法解析日志内容，请检查日志格式"));
            }

            log.info("日志解析成功: {} - {}", parsedRecord.getExceptionClass(), parsedRecord.getExceptionMessage());

            // 2. 过滤日志（提取业务相关信息）
            LogRecord filteredRecord = logFilter.filter(parsedRecord);

            // 3. AI分析
            AnalysisResult analysisResult = aiAnalyzer.analyze(filteredRecord);

            if (analysisResult == null){
                return null;
            }

            // 4. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", analysisResult);
            response.put("logInfo", Map.of(
                "exceptionClass", parsedRecord.getExceptionClass(),
                "exceptionMessage", parsedRecord.getExceptionMessage(),
                "timestamp", parsedRecord.getTimestamp(),
                "level", parsedRecord.getLevel()
            ));

            log.info("日志分析完成: {}", analysisResult.getSummary());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("日志分析过程发生错误", e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("分析过程发生错误: " + e.getMessage()));
        }
    }

    /**
     * 简单的健康检查接口
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Log Analysis Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
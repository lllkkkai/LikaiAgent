package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.AnalysisResult;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.AIAnalyzer;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.CodeLocator;
import com.lllkkk.ai.agent.modules.log.handle.infrastructure.client.KimiAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KimiAnalyzerImpl implements AIAnalyzer {

    private final KimiAIClient kimiAIClient;
    private final CodeLocator codeLocator;

    @Override
    public AnalysisResult analyze(LogRecord record) {
        try {
            // 构建AI分析提示词
            String prompt = buildAnalysisPrompt(record);

            // 调用AI接口获取分析结果
            String aiResponse = kimiAIClient.analyzeLog(prompt);

            // 解析AI响应并构建结果
            return parseAIResponse(aiResponse, record);

        } catch (Exception e) {
            log.error("AI分析日志失败", e);
            return buildFallbackResult(record, e.getMessage());
        }
    }

    /**
     * 构建AI分析提示词
     */
    private String buildAnalysisPrompt(LogRecord record) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位Java异常分析专家，请分析以下异常日志并给出专业的诊断建议。\n\n");

        // 基本信息
        prompt.append("=== 异常基本信息 ===\n");
        prompt.append("时间戳: ").append(record.getTimestamp()).append("\n");
        prompt.append("异常级别: ").append(record.getLevel()).append("\n");
        prompt.append("异常类: ").append(record.getExceptionClass()).append("\n");
        prompt.append("异常消息: ").append(record.getExceptionMessage()).append("\n\n");

        // 堆栈信息
        if (record.getStackFrames() != null && !record.getStackFrames().isEmpty()) {
            prompt.append("=== 业务相关堆栈信息 ===\n");
            for (int i = 0; i < Math.min(5, record.getStackFrames().size()); i++) {
                StackFrame frame = record.getStackFrames().get(i);
                prompt.append(String.format("%d. %s.%s(%s:%d)\n",
                    i + 1,
                    frame.getClassName(),
                    frame.getMethodName(),
                    frame.getClassName().substring(frame.getClassName().lastIndexOf('.') + 1),
                    frame.getLineNumber()));

                // 获取源码上下文
                try {
                    List<String> codeSnippet = codeLocator.fetchSnippet(frame, 3);
                    if (!codeSnippet.isEmpty()) {
                        prompt.append("   相关代码片段:\n");
                        int startLine = Math.max(1, frame.getLineNumber() - 2);
                        for (int j = 0; j < codeSnippet.size(); j++) {
                            int lineNum = startLine + j;
                            String marker = (lineNum == frame.getLineNumber()) ? ">" : " ";
                            prompt.append(String.format("   %s %4d: %s\n", marker, lineNum, codeSnippet.get(j).trim()));
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取代码片段失败: {}", frame, e);
                }
                prompt.append("\n");
            }
        }

        prompt.append("=== 分析要求 ===\n");
        prompt.append("1. 根因分析: 请分析导致此异常的根本原因\n");
        prompt.append("2. 错误摘要: 用简洁的语言描述问题\n");
        prompt.append("3. 修复建议: 提供具体的解决方案和代码修改建议\n");
        prompt.append("4. 相关位置: 指出可能需要修改的代码位置\n\n");

        prompt.append("请以以下JSON格式返回分析结果:\n");
        prompt.append("{\n");
        prompt.append("  \"rootCause\": \"根本原因\",\n");
        prompt.append("  \"summary\": \"错误摘要\",\n");
        prompt.append("  \"fixSuggestion\": \"修复建议\",\n");
        prompt.append("  \"relatedLocation\": \"相关代码位置\",\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 解析AI响应
     */
    private AnalysisResult parseAIResponse(String aiResponse, LogRecord record) {
        try {
            // 提取JSON部分（AI可能返回一些额外文本）
            String jsonPart = extractJsonFromResponse(aiResponse);

            // 简单的JSON解析（可以后续使用Jackson优化）
            String rootCause = extractJsonField(jsonPart, "rootCause");
            String summary = extractJsonField(jsonPart, "summary");
            String fixSuggestion = extractJsonField(jsonPart, "fixSuggestion");
            String relatedLocation = extractJsonField(jsonPart, "relatedLocation");

            // 如果解析失败，提供默认分析
            if (rootCause == null || rootCause.isEmpty()) {
                return buildDefaultResult(record);
            }

            return AnalysisResult.builder()
                    .rootCause(rootCause)
                    .summary(summary)
                    .fixSuggestion(fixSuggestion)
                    .relatedLocation(relatedLocation)
                    .build();

        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            return buildDefaultResult(record);
        }
    }

    /**
     * 从AI响应中提取JSON部分
     */
    private String extractJsonFromResponse(String response) {
        // 查找JSON的开始和结束位置
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }

        return response;
    }

    /**
     * 简单的JSON字段提取
     */
    private String extractJsonField(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\": \"";
            int start = json.indexOf(pattern);
            if (start == -1) {
                pattern = "\"" + fieldName + "\":\"";
                start = json.indexOf(pattern);
            }

            if (start != -1) {
                start += pattern.length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.warn("提取JSON字段失败: {}", fieldName, e);
        }

        return "";
    }

    /**
     * 构建默认分析结果
     */
    private AnalysisResult buildDefaultResult(LogRecord record) {
        return AnalysisResult.builder()
                .rootCause("异常类型: " + record.getExceptionClass() + " - " + record.getExceptionMessage())
                .summary("发生" + record.getExceptionClass() + "异常，需要检查相关代码逻辑")
                .fixSuggestion("建议检查异常堆栈中指向的业务代码，确认是否有空指针、数组越界、类型转换等常见问题")
                .relatedLocation(getTopStackFrameLocation(record))
                .build();
    }

    /**
     * 构建降级结果
     */
    private AnalysisResult buildFallbackResult(LogRecord record, String error) {
        return AnalysisResult.builder()
                .rootCause("AI分析暂时不可用: " + error)
                .summary("日志中包含" + record.getExceptionClass() + "异常")
                .fixSuggestion("请稍后重试，或根据堆栈信息手动分析代码问题")
                .relatedLocation(getTopStackFrameLocation(record))
                .build();
    }

    /**
     * 获取顶部堆栈帧位置
     */
    private String getTopStackFrameLocation(LogRecord record) {
        if (record.getStackFrames() != null && !record.getStackFrames().isEmpty()) {
            StackFrame frame = record.getStackFrames().get(0);
            return String.format("%s.%s:%d",
                frame.getClassName(),
                frame.getMethodName(),
                frame.getLineNumber());
        }
        return "未知位置";
    }
}

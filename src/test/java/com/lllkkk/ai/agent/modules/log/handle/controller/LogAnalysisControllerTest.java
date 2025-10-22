package com.lllkkk.ai.agent.modules.log.handle.controller;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.AnalysisResult;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.AIAnalyzer;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.LogFilter;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.LogParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogAnalysisController.class)
class LogAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogParser logParser;

    @MockBean
    private LogFilter logFilter;

    @MockBean
    private AIAnalyzer aiAnalyzer;

    @Test
    void analyzeLog_Success() throws Exception {
        // 准备测试数据
        String testLog = "2024-01-01 10:00:00 ERROR com.example.Test - NullPointerException: Cannot invoke method";

        LogRecord mockLogRecord = new LogRecord();
        mockLogRecord.setTimestamp("2024-01-01 10:00:00");
        mockLogRecord.setLevel("ERROR");
        mockLogRecord.setExceptionClass("java.lang.NullPointerException");
        mockLogRecord.setExceptionMessage("Cannot invoke method");

        AnalysisResult mockAnalysisResult = new AnalysisResult();
        mockAnalysisResult.setRootCause("空指针异常");
        mockAnalysisResult.setSummary("方法调用时对象为null");
        mockAnalysisResult.setFixSuggestion("检查对象是否为null");
        mockAnalysisResult.setRelatedLocation("com.example.Test:15");

        // 设置mock行为
        when(logParser.parse(testLog)).thenReturn(mockLogRecord);
        when(logFilter.filter(mockLogRecord)).thenReturn(mockLogRecord);
        when(aiAnalyzer.analyze(mockLogRecord)).thenReturn(mockAnalysisResult);

        // 执行测试
        mockMvc.perform(post("/api/log-analysis/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .content(testLog))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rootCause").value("空指针异常"))
                .andExpect(jsonPath("$.data.summary").value("方法调用时对象为null"))
                .andExpect(jsonPath("$.logInfo.exceptionClass").value("java.lang.NullPointerException"));
    }

    @Test
    void analyzeLog_ParseFailure() throws Exception {
        // 准备测试数据
        String invalidLog = "invalid log content";

        // 设置mock行为 - 解析失败
        when(logParser.parse(invalidLog)).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/api/log-analysis/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .content(invalidLog))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("无法解析日志内容，请检查日志格式"));
    }

    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(post("/api/log-analysis/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Log Analysis Service"));
    }
}
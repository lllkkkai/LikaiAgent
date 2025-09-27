package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.AnalysisResult;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.AIAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class KimiAnalyzerImpl implements AIAnalyzer {

    @Override
    public AnalysisResult analyze(LogRecord record) {
        // 根据项目内报错堆栈 获取对应的源代码
        return null;
    }
}

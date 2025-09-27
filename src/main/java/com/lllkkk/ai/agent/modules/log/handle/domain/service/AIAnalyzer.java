package com.lllkkk.ai.agent.modules.log.handle.domain.service;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.AnalysisResult;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;

public interface AIAnalyzer {
    AnalysisResult analyze(LogRecord record);
}

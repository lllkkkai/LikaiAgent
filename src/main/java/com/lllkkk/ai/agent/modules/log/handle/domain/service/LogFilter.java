package com.lllkkk.ai.agent.modules.log.handle.domain.service;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;

public interface LogFilter {
    LogRecord filter(LogRecord record);
}

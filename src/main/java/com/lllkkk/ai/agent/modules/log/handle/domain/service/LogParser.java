package com.lllkkk.ai.agent.modules.log.handle.domain.service;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;

public interface LogParser {
    LogRecord parse(String rawLog);
}

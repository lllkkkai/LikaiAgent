package com.lllkkk.ai.agent.modules.log.handle.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogRecord {
    public String projectName;
    public String timestamp;
    public String level;
    public String exceptionClass;
    public String exceptionMessage;
    public List<StackFrame> stackFrames;
    public String rawLog;
}

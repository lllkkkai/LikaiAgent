package com.lllkkk.ai.agent.modules.log.handle.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StackFrame {
    public String className;
    public String methodName;
    public int lineNumber;
    public boolean businessFlag;
}

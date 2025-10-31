package com.lllkkk.ai.agent.modules.log.handle.domain.service;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;

import java.util.List;

public interface CodeLocator {
    /**
     * 根据堆栈帧获取源码片段
     * @param frame 堆栈信息
     * @param contextLines 行号上下文行数
     * @return 代码片段
     */
    List<String> fetchSnippet(StackFrame frame, int contextLines);

    List<String> fetchSnippet(String projectName, StackFrame frame);
}

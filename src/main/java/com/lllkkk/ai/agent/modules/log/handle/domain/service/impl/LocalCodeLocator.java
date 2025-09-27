package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.CodeLocator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocalCodeLocator implements CodeLocator {

    private final String srcRoot = "src/main/java"; // 项目源码根目录

    /**
     * 根据堆栈帧获取源码片段
     *
     * @param frame        堆栈信息
     * @param contextLines 上下文行数（例如上下各3行）
     * @return 代码片段列表
     */
    @Override
    public List<String> fetchSnippet(StackFrame frame, int contextLines) {
        if (frame == null || frame.getClassName() == null || frame.getLineNumber() <= 0) {
            return List.of();
        }

        String classPath = frame.getClassName().replace('.', '/') + ".java";
        Path path = Paths.get(srcRoot, classPath);

        if (!Files.exists(path)) {
            return List.of(); // 文件不存在
        }

        try {
            List<String> lines = Files.readAllLines(path);
            int totalLines = lines.size();
            int start = Math.max(0, frame.getLineNumber() - 1 - contextLines); // java行号从1开始
            int end = Math.min(totalLines, frame.getLineNumber() - 1 + contextLines + 1);

            return new ArrayList<>(lines.subList(start, end));

        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}

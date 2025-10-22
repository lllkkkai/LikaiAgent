package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.CodeLocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LocalCodeLocator implements CodeLocator {

    @Value("${code.locator.source-root:src/main/java}")
    private String sourceRoot;

    @Value("${code.locator.enable-source-lookup:true}")
    private boolean enableSourceLookup;

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
            log.warn("无效的堆栈帧: {}", frame);
            return List.of();
        }

        if (!enableSourceLookup) {
            log.debug("源码查找功能已禁用");
            return List.of();
        }

        try {
            // 尝试多种方式获取源码
            List<String> sourceCode = getSourceCode(frame);

            if (sourceCode.isEmpty()) {
                log.warn("无法获取类 {} 的源码", frame.getClassName());
                return List.of();
            }

            return extractCodeSnippet(sourceCode, frame.getLineNumber(), contextLines);

        } catch (Exception e) {
            log.error("获取源码片段失败: 类={}, 行号={}",
                     frame.getClassName(), frame.getLineNumber(), e);
            return List.of();
        }
    }

    /**
     * 尝试多种方式获取源码
     */
    private List<String> getSourceCode(StackFrame frame) {
        String fullClassName = frame.getFullyQualifiedName();
        // 1. 尝试从文件系统获取（开发环境）
        List<String> sourceCode = getSourceFromFileSystem(frame.getFullyQualifiedName());
        if (!sourceCode.isEmpty()) {
            log.debug("从文件系统获取源码成功: {}", fullClassName);
            return sourceCode;
        }

        log.warn("无法获取类 {} 的源码，已尝试所有方法", fullClassName);
        return List.of();
    }

    /**
     * 从文件系统获取源码
     */
    private List<String> getSourceFromFileSystem(String fullyQualifiedName) {
        try {
            String classPath = fullyQualifiedName;
            // 尝试多个可能的路径
            List<String> possiblePaths = List.of(
                Paths.get(sourceRoot, classPath).toString()                    // 配置的源码根目录
            );

            for (String pathStr : possiblePaths) {
                Path path = Paths.get(pathStr);
                if (Files.exists(path) && Files.isReadable(path)) {
                    return Files.readAllLines(path);
                }
            }
        } catch (IOException e) {
            log.debug("从文件系统获取源码失败: {}", fullyQualifiedName, e);
        }
        return List.of();
    }

    /**
     * 从类路径获取源码（适用于包含源码的JAR包）
     */
    private List<String> getSourceFromClasspath(String className) {
        try {
            String resourcePath = className.replace('.', '/') + ".java";
            Resource resource = new ClassPathResource(resourcePath);

            if (resource.exists() && resource.isReadable()) {
                return readLinesFromInputStream(resource.getInputStream());
            }
        } catch (IOException e) {
            log.debug("从类路径获取源码失败: {}", className, e);
        }
        return List.of();
    }

    /**
     * 从Maven标准结构获取（适用于IDE运行环境）
     */
    private List<String> getSourceFromMavenStructure(String className) {
        try {
            // 获取当前类的加载路径，推断项目结构
            String currentClassPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

            if (currentClassPath.contains("target/classes")) {
                // 推断源码路径
                String projectRoot = currentClassPath.substring(0, currentClassPath.indexOf("target/classes"));
                String classPath = className.replace('.', '/') + ".java";
                Path sourcePath = Paths.get(projectRoot, "src/main/java", classPath);

                if (Files.exists(sourcePath) && Files.isReadable(sourcePath)) {
                    return Files.readAllLines(sourcePath);
                }
            }
        } catch (Exception e) {
            log.debug("从Maven结构获取源码失败: {}", className, e);
        }
        return List.of();
    }

    /**
     * 从输入流读取行列表
     */
    private List<String> readLinesFromInputStream(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 提取代码片段
     */
    private List<String> extractCodeSnippet(List<String> sourceCode, int lineNumber, int contextLines) {
        int totalLines = sourceCode.size();

        // Java行号从1开始，转换为0-based索引
        int targetIndex = lineNumber - 1;

        if (targetIndex < 0 || targetIndex >= totalLines) {
            log.warn("行号超出范围: 文件行数={}, 目标行号={}", totalLines, lineNumber);
            return List.of();
        }

        int start = Math.max(0, targetIndex - contextLines);
        int end = Math.min(totalLines, targetIndex + contextLines + 1);

        return new ArrayList<>(sourceCode.subList(start, end));
    }
}
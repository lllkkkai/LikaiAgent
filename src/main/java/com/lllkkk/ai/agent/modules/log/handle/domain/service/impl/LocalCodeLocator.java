package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.CodeLocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class LocalCodeLocator implements CodeLocator {

    @Value("${code.locator.source-root:src/main/java}")
    private String sourceRoot;

    @Value("${code.locator.enable-source-lookup:true}")
    private boolean enableSourceLookup;

    @Value("${code.locator.path-file:code-locator-paths.json}")
    private String pathFile;

    private Map<String, String> projectPathMap = new HashMap<>();

    @PostConstruct
    public void loadProjectPaths() {
        File file = new File(pathFile);
        if (!file.exists()) {
            log.warn("⚠️ 未找到本地路径配置文件 [{}]，源码定位功能将受限", pathFile);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            projectPathMap = mapper.readValue(file, new TypeReference<>() {});
            log.info("✅ 成功加载本地项目路径配置，共 {} 个项目", projectPathMap.size());
        } catch (IOException e) {
            log.error("❌ 加载本地路径配置文件失败: {}", pathFile, e);
        }
    }

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
            List<String> sourceCode = new ArrayList<>();
            if (sourceCode.isEmpty()) {
                log.warn("无法获取类 {} 的源码", frame.getClassName());
                return List.of();
            }

            return extractCodeSnippet(sourceCode, frame.getLineNumber(), contextLines);
        } catch (Exception e) {
            log.error("获取源码片段失败: 类={}, 行号={}", frame.getClassName(), frame.getLineNumber(), e);
            return List.of();
        }
    }

    @Override
    public List<String> fetchSnippet(String projectName, StackFrame frame) {
        if (frame == null || frame.getClassName() == null || frame.getLineNumber() <= 0) {
            log.warn("无效的堆栈帧: {}", frame);
            return List.of();
        }

        if (!enableSourceLookup) {
            log.debug("源码查找功能已禁用");
            return List.of();
        }

        List<String> sourceCode = getSourceCode(projectName, frame);
        return sourceCode;
    }

    private List<String> getSourceCode(String projectName, StackFrame frame) {
        String fullClassName = frame.getFullyQualifiedName();
        List<String> sourceCode = getSourceFromFileSystem(projectName, frame);
        if (!sourceCode.isEmpty()) {
            log.debug("从文件系统获取源码成功: {}", fullClassName);
            return sourceCode;
        }

        log.warn("无法获取类 {} 的源码，已尝试所有方法", fullClassName);
        return List.of();
    }

    private List<String> getSourceFromFileSystem(String projectName, StackFrame frame) {
        String rootDirPath = getRootDirPath(projectName);
        if (rootDirPath == null) {
            log.warn("未配置项目 [{}] 的路径，请在 code-locator-paths.json 中添加", projectName);
            return List.of();
        }

        List<String> filePaths = findJavaSource(rootDirPath, frame.getFullyQualifiedName());
        if (filePaths.isEmpty()) {
            return List.of();
        }

        String filePath = filePaths.get(0);
        Path path = Paths.get(filePath);

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (frame.getLineNumber() > 0 && frame.getLineNumber() <= lines.size()) {
                int start = Math.max(0, frame.getLineNumber() - 5);
                int end = Math.min(lines.size(), frame.getLineNumber() + 5);
                return lines.subList(start, end);
            }
            return lines;
        } catch (IOException e) {
            log.error("读取源码文件失败: {}", filePath, e);
            return List.of();
        }
    }

    private String getRootDirPath(String projectName) {
        return projectPathMap.get(projectName);
    }

    public static List<String> findJavaSource(String rootDirPath, String fullClassName) {
        String relativePath = fullClassName.replace('.', File.separatorChar) + ".java";
        File rootDir = new File(rootDirPath);

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("无效的目录路径: " + rootDirPath);
        }

        List<String> results = new ArrayList<>();
        try {
            Files.walk(rootDir.toPath())
                    .filter(path -> path.toString().endsWith(relativePath))
                    .forEach(path -> results.add(path.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    private List<String> extractCodeSnippet(List<String> sourceCode, int lineNumber, int contextLines) {
        int totalLines = sourceCode.size();
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

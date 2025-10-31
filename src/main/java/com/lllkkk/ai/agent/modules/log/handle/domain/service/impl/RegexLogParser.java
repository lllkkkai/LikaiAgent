package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.LogParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegexLogParser implements LogParser {

    private static final Pattern EXCEPTION_PROJECT_PATTERN = Pattern.compile("异常项目: (.+)");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("异常时间: (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("异常级别: (\\w+)");
    private static final Pattern EXCEPTION_DESC_PATTERN = Pattern.compile("异常描述: (.+)", Pattern.MULTILINE);
    private static final Pattern STACK_TRACE_PATTERN =
            Pattern.compile("详细信息:\\s*\\[([\\s\\S]*?)(?:\\]|$)", Pattern.DOTALL);

    private static final Pattern STACK_FRAME_PATTERN = Pattern.compile("([^\\(]+)\\(([^:]+)(?::(\\d+))?\\)");
    private static final Pattern FRAME_SPLITTER = Pattern.compile(",\\s*");
    @Override
    public LogRecord parse(String rawLog) {
        String projectName = extractValue(rawLog, EXCEPTION_PROJECT_PATTERN, "");
        String timestamp = extractValue(rawLog, TIMESTAMP_PATTERN, null);
        String level = extractValue(rawLog, LEVEL_PATTERN, "UNKNOWN");

        String exceptionClass = null;
        String exceptionMessage = "";
        String exceptionDesc = extractValue(rawLog, EXCEPTION_DESC_PATTERN, null);
        if (exceptionDesc != null) {
            String[] parts = exceptionDesc.split(": ", 2);
            exceptionClass = parts[0];
            if (parts.length > 1) {
                exceptionMessage = parts[1];
            }
        }

        return LogRecord.builder()
                .projectName(projectName)
                .rawLog(rawLog)
                .timestamp(timestamp)
                .level(level)
                .exceptionClass(exceptionClass)
                .exceptionMessage(exceptionMessage)
                .stackFrames(parseStackTrace(rawLog))
                .build();
    }

    private String extractValue(String log, Pattern pattern, String defaultValue) {
        Matcher matcher = pattern.matcher(log);
        return matcher.find() ? matcher.group(1).trim() : defaultValue;
    }

    private List<StackFrame> parseStackTrace(String rawLog) {
        List<StackFrame> stackFrames = new ArrayList<>();

        Matcher matcher = STACK_TRACE_PATTERN.matcher(rawLog);
        if (!matcher.find()) {
            System.err.println("⚠️ 未能匹配到堆栈内容");
            return stackFrames;
        }

        String stackTraceContent = matcher.group(1).trim();

        String[] frames = stackTraceContent.split(",\\s*\\r?\\n?|,\\s+");

        for (String frameString : frames) {
            Matcher frameMatcher = STACK_FRAME_PATTERN.matcher(frameString.trim());
            if (frameMatcher.find()) {
                StackFrame frame = new StackFrame();

                String fullMethodPath = frameMatcher.group(1).trim();
                int lastDotIndex = fullMethodPath.lastIndexOf('.');

                frame.className = (lastDotIndex != -1)
                        ? fullMethodPath.substring(0, lastDotIndex)
                        : "";
                frame.methodName = (lastDotIndex != -1)
                        ? fullMethodPath.substring(lastDotIndex + 1)
                        : fullMethodPath;

                String lineNumberStr = frameMatcher.group(3);
                frame.lineNumber = (lineNumberStr != null) ? Integer.parseInt(lineNumberStr) : -1;

                frame.fullyQualifiedName = frame.className;
                frame.businessFlag = fullMethodPath.startsWith("com.dyyl"); // 你自己定义
                stackFrames.add(frame);
            }
        }

        return stackFrames;
    }


}

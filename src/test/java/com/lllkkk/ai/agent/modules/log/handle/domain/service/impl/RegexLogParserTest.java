package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexLogParserTest {

    private RegexLogParser regexLogParser;

    @BeforeEach
    void setUp() {
        regexLogParser = new RegexLogParser();
    }

    @Test
    void testParse_Success() {
        // Arrange
        String rawLog = """
                异常项目: order-web
                IP地址: 172.22.1.15
                异常级别: ERROR
                异常来源: com.dyyl.order.controller.YlMeetingSignController
                异常时间: 2025-09-27 15:48:12
                日志内容: 上传图片失败
                异常描述: java.lang.NullPointerException
                详细信息:
                 [com.dyyl.order.meeting.application.service.RegistrationServiceImpl.lambda$uploadShareImage$10(RegistrationServiceImpl.java:694), java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:174), java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1384), java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:482), java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472), java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:842)]
                """;

        // Act
        LogRecord result = regexLogParser.parse(rawLog);

        // Assert
        assertNotNull(result);
        assertEquals("2025-09-27 15:48:12", result.timestamp);
        assertEquals("ERROR", result.level);
        assertEquals("java.lang.NullPointerException", result.exceptionClass);
        assertEquals("", result.exceptionMessage);

        List<StackFrame> stackFrames = result.stackFrames;
        assertNotNull(stackFrames);
        assertEquals(6, stackFrames.size());

        // Assert first stack frame
        StackFrame firstFrame = stackFrames.get(0);
        assertEquals("com.dyyl.order.meeting.application.service.RegistrationServiceImpl", firstFrame.className);
        assertEquals("lambda$uploadShareImage$10", firstFrame.methodName);
        assertEquals(694, firstFrame.lineNumber);
        assertTrue(firstFrame.businessFlag, "First frame should be business code");

        // Assert second stack frame
        StackFrame secondFrame = stackFrames.get(1);
        assertEquals("java.util.stream.ReferencePipeline$2$1", secondFrame.className);
        assertEquals("accept", secondFrame.methodName);
        assertEquals(174, secondFrame.lineNumber);
        assertFalse(secondFrame.businessFlag, "Second frame should not be business code");

        // Assert last stack frame
        StackFrame lastFrame = stackFrames.get(5);
        assertEquals("java.util.stream.ReduceOps$ReduceOp", lastFrame.className);
        assertEquals("evaluateSequential", lastFrame.methodName);
        assertEquals(842, lastFrame.lineNumber);
        assertFalse(lastFrame.businessFlag, "Last frame should not be business code");
    }
}

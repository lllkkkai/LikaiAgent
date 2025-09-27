package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogFilterImplTest {

    private LogFilterImpl logFilter;
    private RegexLogParser regexLogParser;

    @BeforeEach
    void setUp() {
        logFilter = new LogFilterImpl();
        regexLogParser = new RegexLogParser(); // To generate test data
    }

    /**
     * Helper method to generate the LogRecord from RegexLogParserTest.
     */
    private LogRecord createLogRecordFromParserTest() {
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
        return regexLogParser.parse(rawLog);
    }

    @Test
    void testFilter_WithBusinessFrames() {
        // Arrange
        LogRecord record = createLogRecordFromParserTest();
        assertNotNull(record.getStackFrames());
        assertEquals(6, record.getStackFrames().size(), "Pre-condition: Should have 6 frames before filtering");

        // Act
        LogRecord filteredRecord = logFilter.filter(record);

        // Assert
        assertNotNull(filteredRecord);
        List<StackFrame> filteredFrames = filteredRecord.getStackFrames();
        assertNotNull(filteredFrames);
        assertEquals(1, filteredFrames.size(), "Should be filtered down to 1 business frame");
        assertEquals("com.dyyl.order.meeting.application.service.RegistrationServiceImpl", filteredFrames.get(0).getClassName());
    }

    @Test
    void testFilter_WithOnlyNonBusinessFrames() {
        // Arrange
        List<StackFrame> nonBusinessFrames = new ArrayList<>();
        nonBusinessFrames.add(new StackFrame("java.util.ArrayList", "forEach", 101, false));
        nonBusinessFrames.add(new StackFrame("org.springframework.web.filter.OncePerRequestFilter", "doFilter", 112, false));

        LogRecord record = LogRecord.builder()
                .stackFrames(nonBusinessFrames)
                .build();

        // Act
        LogRecord filteredRecord = logFilter.filter(record);

        // Assert
        assertNotNull(filteredRecord);
        List<StackFrame> filteredFrames = filteredRecord.getStackFrames();
        assertNotNull(filteredFrames);
        assertEquals(1, filteredFrames.size(), "Should keep the first frame if no business frames are found");
        assertEquals("java.util.ArrayList", filteredFrames.get(0).getClassName());
        assertEquals("forEach", filteredFrames.get(0).getMethodName());
    }

    @Test
    void testFilter_WithEmptyStackFrames() {
        // Arrange
        LogRecord record = LogRecord.builder()
                .stackFrames(new ArrayList<>())
                .build();

        // Act
        LogRecord filteredRecord = logFilter.filter(record);

        // Assert
        assertNotNull(filteredRecord);
        assertTrue(filteredRecord.getStackFrames().isEmpty(), "Should remain empty");
    }
}

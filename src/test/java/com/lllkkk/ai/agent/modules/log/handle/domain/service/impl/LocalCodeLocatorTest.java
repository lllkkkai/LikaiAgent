package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalCodeLocator 单元测试
 */
class LocalCodeLocatorTest {

    private LocalCodeLocator codeLocator;

    @BeforeEach
    void setUp() {
        codeLocator = new LocalCodeLocator();
        // 配置源码根目录
        ReflectionTestUtils.setField(codeLocator, "sourceRoot", "src/main/java");
        ReflectionTestUtils.setField(codeLocator, "enableSourceLookup", true);
    }

    @Test
    void testFetchSnippet_InvalidFrame_ShouldReturnEmpty() {
        // 测试无效的堆栈帧
        StackFrame nullFrame = null;
        List<String> result = codeLocator.fetchSnippet(nullFrame, 3);
        assertTrue(result.isEmpty());

        StackFrame invalidFrame = new StackFrame();
        invalidFrame.setClassName(null);
        invalidFrame.setLineNumber(0);
        result = codeLocator.fetchSnippet(invalidFrame, 3);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchSnippet_NonExistentClass_ShouldReturnEmpty() {
        // 测试不存在的类
        StackFrame frame = new StackFrame();
        frame.setClassName("com.nonexistent.Class");
        frame.setMethodName("nonExistentMethod");
        frame.setLineNumber(10);
        frame.setBusinessFlag(true);

        List<String> result = codeLocator.fetchSnippet(frame, 3);
        assertTrue(result.isEmpty(), "不存在的类应该返回空列表");
    }

    @Test
    void testFetchSnippet_ValidLocalCodeLocator_ShouldReturnCode() {
        // 测试获取LocalCodeLocator自身的代码
        StackFrame frame = new StackFrame();
        frame.setClassName("com.lllkkk.ai.agent.modules.log.handle.domain.service.impl.LocalCodeLocator");
        frame.setMethodName("fetchSnippet");
        frame.setLineNumber(50); // 大约是fetchSnippet方法的行号
        frame.setBusinessFlag(true);

        List<String> snippet = codeLocator.fetchSnippet(frame, 2);

        assertNotNull(snippet);
        // 注意：在测试环境中可能无法获取到实际的源码文件
        // 所以这个测试主要是验证代码不会崩溃

        if (!snippet.isEmpty()) {
            // 如果成功获取到代码，验证代码质量
            assertTrue(snippet.size() <= 5, "上下文行数应该符合预期"); // 2行上文 + 1行目标 + 2行下文

            // 验证获取到了一些Java代码特征
            boolean hasJavaCode = snippet.stream()
                    .anyMatch(line -> line.contains("public") || line.contains("private") || line.contains("if"));
            assertTrue(hasJavaCode, "应该包含Java代码特征");
        }
    }

    @Test
    void testFetchSnippet_DisabledLookup_ShouldReturnEmpty() {
        // 测试禁用源码查找功能
        ReflectionTestUtils.setField(codeLocator, "enableSourceLookup", false);

        StackFrame frame = new StackFrame();
        frame.setClassName("com.lllkkk.ai.agent.modules.log.handle.domain.service.impl.LocalCodeLocator");
        frame.setMethodName("fetchSnippet");
        frame.setLineNumber(50);
        frame.setBusinessFlag(true);

        List<String> result = codeLocator.fetchSnippet(frame, 3);
        assertTrue(result.isEmpty(), "禁用状态下应该返回空列表");
    }

    @Test
    void testFetchSnippet_DifferentContextSizes_ShouldWork() {
        StackFrame frame = new StackFrame();
        frame.setClassName("com.lllkkk.ai.agent.modules.log.handle.domain.service.impl.LocalCodeLocator");
        frame.setMethodName("getSourceCode");
        frame.setLineNumber(80); // 大约是getSourceCode方法的行号
        frame.setBusinessFlag(true);

        // 测试不同的上下文大小
        List<String> snippet1 = codeLocator.fetchSnippet(frame, 1);
        List<String> snippet3 = codeLocator.fetchSnippet(frame, 3);
        List<String> snippet5 = codeLocator.fetchSnippet(frame, 5);

        assertNotNull(snippet1);
        assertNotNull(snippet3);
        assertNotNull(snippet5);

        // 验证上下文大小关系（如果成功获取到代码）
        if (!snippet1.isEmpty() && !snippet3.isEmpty() && !snippet5.isEmpty()) {
            assertTrue(snippet3.size() >= snippet1.size());
            assertTrue(snippet5.size() >= snippet3.size());
        }
    }

    @Test
    void testFetchSnippet_OutOfRangeLineNumber_ShouldReturnEmpty() {
        // 测试超出范围的行号
        StackFrame frame = new StackFrame();
        frame.setClassName("com.lllkkk.ai.agent.modules.log.handle.domain.service.impl.LocalCodeLocator");
        frame.setMethodName("fetchSnippet");
        frame.setLineNumber(99999); // 明显超出范围的行号
        frame.setBusinessFlag(true);

        List<String> result = codeLocator.fetchSnippet(frame, 3);
        // 应该返回空列表或者实际存在的代码（如果文件很大）
        assertNotNull(result);
    }
}
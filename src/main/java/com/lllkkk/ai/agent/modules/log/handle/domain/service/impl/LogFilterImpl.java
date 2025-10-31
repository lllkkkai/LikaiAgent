package com.lllkkk.ai.agent.modules.log.handle.domain.service.impl;

import com.lllkkk.ai.agent.modules.log.handle.domain.model.LogRecord;
import com.lllkkk.ai.agent.modules.log.handle.domain.model.StackFrame;
import com.lllkkk.ai.agent.modules.log.handle.domain.service.LogFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogFilterImpl implements LogFilter {

    private static final String[] BUSINESS_PACKAGES = {
            "com.dyyl", "com.wkb"
    };

    @Override
    public LogRecord filter(LogRecord record) {
        if (record == null || record.getStackFrames() == null) {
            return record;
        }

        List<StackFrame> filteredFrames = record.getStackFrames().stream()
                .filter(this::isBusinessFrame)
                .filter(frame -> frame.getLineNumber() > 0)
                .toList();

        if (filteredFrames.isEmpty() && !record.getStackFrames().isEmpty()) {
            filteredFrames = List.of(record.getStackFrames().get(0));
        }

        record.setStackFrames(filteredFrames);
        return record;
    }

    private boolean isBusinessFrame(StackFrame frame) {
        String fullClass = frame.getClassName();
        if (fullClass == null) return false;
        for (String pkg : BUSINESS_PACKAGES) {
            if (fullClass.startsWith(pkg)) return true;
        }
        return false;
    }
}

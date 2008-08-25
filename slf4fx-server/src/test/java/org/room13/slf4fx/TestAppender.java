package org.room13.slf4fx;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.room13.slf4fx.messages.LogRecordMessage;

import java.util.Set;

/**
 * TODO: Document the class
 */
public class TestAppender extends AppenderSkeleton {
    private Set<LogRecordMessage> _expectation;

    public TestAppender() {
    }

    public void setExpectation(final Set<LogRecordMessage> expectation) {
        _expectation = expectation;
    }

    public Set<LogRecordMessage> getExpectation() {
        return _expectation;
    }

    protected void append(final LoggingEvent event) {
        final LogRecordMessage message = new LogRecordMessage();
        message.setCategory(event.getLoggerName());
        message.setLevel(asInternalLevel(event.getLevel()));
        message.setMessage(event.getMessage().toString());
        if (_expectation == null)
            return;
        if (_expectation.contains(message))
            _expectation.remove(message);
    }

    private LogRecordMessage.Level asInternalLevel(final org.apache.log4j.Level level) {
        if (org.apache.log4j.Level.ERROR.equals(level))
            return LogRecordMessage.Level.ERROR;
        if (org.apache.log4j.Level.WARN.equals(level))
            return LogRecordMessage.Level.WARN;
        if (org.apache.log4j.Level.DEBUG.equals(level))
            return LogRecordMessage.Level.DEBUG;
        if (org.apache.log4j.Level.TRACE.equals(level))
            return LogRecordMessage.Level.TRACE;
        return LogRecordMessage.Level.INFO;
    }

    public boolean requiresLayout() {
        return false;
    }

    public void close() {
    }
}

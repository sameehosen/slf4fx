/*
 * Copyright (C) 2009 Dmitry Motylev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE.txt-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.room13.slf4fx;

import org.room13.slf4fx.messages.LogRecordMessage;
import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.util.Set;

/**
 * TODO: Document the class
 */
public class TestLogger extends MarkerIgnoringBase implements Logger {
    private Set<LogRecordMessage> _expectations;

    public TestLogger(final String aName) {
        name = aName;
    }

    public void setExpectations(final Set<LogRecordMessage> expectation) {
        _expectations = expectation;
    }

    public Set<LogRecordMessage> getExpectations() {
        return _expectations;
    }

    private void logMessage(final LogRecordMessage.Level aLevel, final String aMessage) {
        //System.err.printf("%s %s %s\n", aLevel, _name, aMessage);
        final LogRecordMessage message = new LogRecordMessage();
        message.setCategory(getName());
        message.setLevel(aLevel);
        message.setMessage(aMessage);
        if (_expectations == null)
            return;
        if (_expectations.contains(message))
            _expectations.remove(message);
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isTraceEnabled() {
        return true;
    }

    public void error(final String msg) {
        logMessage(LogRecordMessage.Level.ERROR, msg);
    }

    public void error(final String format, final Object arg) {
        error(MessageFormatter.format(format, arg));
    }

    public void error(final String format, final Object arg1, final Object arg2) {
        error(MessageFormatter.format(format, arg1, arg2));
    }

    public void error(final String format, final Object[] argArray) {
        error(MessageFormatter.format(format, argArray));
    }

    public void error(final String msg, final Throwable t) {
        error(msg);
    }

    public void warn(final String msg) {
        logMessage(LogRecordMessage.Level.WARN, msg);
    }

    public void warn(final String format, final Object arg) {
        warn(MessageFormatter.format(format, arg));
    }

    public void warn(final String format, final Object arg1, final Object arg2) {
        warn(MessageFormatter.format(format, arg1, arg2));
    }

    public void warn(final String format, final Object[] argArray) {
        warn(MessageFormatter.format(format, argArray));
    }

    public void warn(final String msg, final Throwable t) {
        warn(msg);
    }

    public void info(final String msg) {
        logMessage(LogRecordMessage.Level.INFO, msg);
    }

    public void info(final String format, final Object arg) {
        info(MessageFormatter.format(format, arg));
    }

    public void info(final String format, final Object arg1, final Object arg2) {
        info(MessageFormatter.format(format, arg1, arg2));
    }

    public void info(final String format, final Object[] argArray) {
        info(MessageFormatter.format(format, argArray));
    }

    public void info(final String msg, final Throwable t) {
        info(msg);
    }

    public void debug(final String msg) {
        logMessage(LogRecordMessage.Level.DEBUG, msg);
    }

    public void debug(final String format, final Object arg) {
        debug(MessageFormatter.format(format, arg));
    }

    public void debug(final String format, final Object arg1, final Object arg2) {
        debug(MessageFormatter.format(format, arg1, arg2));
    }

    public void debug(final String format, final Object[] argArray) {
        debug(MessageFormatter.format(format, argArray));
    }

    public void debug(final String msg, final Throwable t) {
        debug(msg);
    }

    public void trace(final String msg) {
        logMessage(LogRecordMessage.Level.TRACE, msg);
    }

    public void trace(final String format, final Object arg) {
        trace(MessageFormatter.format(format, arg));
    }

    public void trace(final String format, final Object arg1, final Object arg2) {
        trace(MessageFormatter.format(format, arg1, arg2));
    }

    public void trace(final String format, final Object[] argArray) {
        trace(MessageFormatter.format(format, argArray));
    }

    public void trace(final String msg, final Throwable t) {
        trace(msg);
    }
}

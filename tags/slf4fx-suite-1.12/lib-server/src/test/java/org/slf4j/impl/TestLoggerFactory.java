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
package org.slf4j.impl;

import org.room13.slf4fx.TestLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TestLoggerFactory implements ILoggerFactory {
    private final ConcurrentMap<String, TestLogger> _loggers =
            new ConcurrentHashMap<String, TestLogger>();

    public TestLoggerFactory() {
        // nothing to do
    }

    public Logger getLogger(String name) {
        TestLogger logger = _loggers.get(name);
        if (logger == null) {
            TestLogger newLogger = new TestLogger(name);
            logger = _loggers.putIfAbsent(name, newLogger);
            if (logger == null) {
                return newLogger;
            }
        }
        return logger;
    }
}

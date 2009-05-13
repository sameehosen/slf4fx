/*
 * Copyright 2008 Dmitry Motylev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.room13.slf4fx;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.room13.slf4fx.messages.LogRecordMessage;
import static org.room13.slf4fx.messages.LogRecordMessage.Level.*;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Document the class
 */
@Test
public class SLF4FxServerTest {
    private SLF4FxServer _server;
    private TestLogger _testLogger;

    private LogRecordMessage newLogRecordMessage(final String category, final LogRecordMessage.Level level, final String message) {
        final LogRecordMessage logRecord = new LogRecordMessage();
        logRecord.setCategory(category);
        logRecord.setLevel(level);
        logRecord.setMessage(message);
        return logRecord;
    }

    @BeforeSuite
    public void prepareLogger() {
        _testLogger = (TestLogger) LoggerFactory.getLogger("slf4fx.myApplication." + TestClient.class.getName());
    }

    @BeforeTest
    public void startServer() throws IOException {
        _server = new SLF4FxServer();

        final Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("myApplication", "mySecret");
        _server.setCredentials(credentials);

        _server.start();
    }

    @AfterTest
    public void stopServer() {
        _server.stop();
    }

    public SocketConnector newConnector(final IoHandler ioHandler) throws IOException {
        final SocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(SLF4FxProtocolEncoder.class, SLF4FxProtocolDecoder.class));
        connector.setHandler(ioHandler);
        return connector;
    }

    @Test
    public void testAccessDenied() throws IOException {
        final Set<LogRecordMessage> expectations = new HashSet<LogRecordMessage>();
        final TestClient client = new TestClient("myApplication", "hisSecret", expectations);

        final SocketConnector connector = newConnector(client.getIoHandler());
        connector.connect(_server.getDefaultLocalAddress());
        try {
            // give a client chance to send data
            Thread.sleep(400);
        } catch (InterruptedException e) {
            // ignore
        }
        assertEquals(client.getAccessGranted(), Boolean.FALSE);
    }

    @Test
    public void testAccessGranted() throws IOException {
        final Set<LogRecordMessage> expectations = new HashSet<LogRecordMessage>();
        final TestClient client = new TestClient("myApplication", "mySecret", expectations);
        final SocketConnector connector = newConnector(client.getIoHandler());
        connector.connect(_server.getDefaultLocalAddress());
        try {
            // give a client chance to send data
            Thread.sleep(400);
        } catch (InterruptedException e) {
            // ignore
        }
        assertEquals(client.getAccessGranted(), Boolean.TRUE);
    }

    @Test
    public void testLogging() throws IOException {
        final Set<LogRecordMessage> expectations = new HashSet<LogRecordMessage>();
        expectations.add(newLogRecordMessage(_testLogger.getName(), ERROR, "slf4fx log error message"));
        expectations.add(newLogRecordMessage(_testLogger.getName(), WARN, "slf4fx log warn message"));
        expectations.add(newLogRecordMessage(_testLogger.getName(), INFO, "slf4fx log info message"));
        expectations.add(newLogRecordMessage(_testLogger.getName(), DEBUG, "slf4fx log debug message"));
        expectations.add(newLogRecordMessage(_testLogger.getName(), TRACE, "slf4fx log trace message"));
        _testLogger.setExpectations(expectations);

        final Set<LogRecordMessage> eventsToSend = new HashSet<LogRecordMessage>();
        eventsToSend.add(newLogRecordMessage(TestClient.class.getName(), ERROR, "slf4fx log error message"));
        eventsToSend.add(newLogRecordMessage(TestClient.class.getName(), WARN, "slf4fx log warn message"));
        eventsToSend.add(newLogRecordMessage(TestClient.class.getName(), INFO, "slf4fx log info message"));
        eventsToSend.add(newLogRecordMessage(TestClient.class.getName(), DEBUG, "slf4fx log debug message"));
        eventsToSend.add(newLogRecordMessage(TestClient.class.getName(), TRACE, "slf4fx log trace message"));

        newConnector(new TestClient("myApplication", "mySecret", eventsToSend).getIoHandler())
                .connect(_server.getDefaultLocalAddress());
        try {
            // give a client chance to send data
            Thread.sleep(400);
        } catch (InterruptedException e) {
            // ignore
        }

        assertEquals(_testLogger.getExpectations().size(), 0);
    }
}

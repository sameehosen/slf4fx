package org.room13.slf4fx;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.room13.slf4fx.messages.LogRecordMessage;
import static org.room13.slf4fx.messages.LogRecordMessage.Level.*;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import static java.util.Collections.EMPTY_SET;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Document the class
 */
@Test
public class SLF4FxServerTest {
    private static final int _PORT = 8888;
    private final TestAppender _appender = new TestAppender();

    private LogRecordMessage newLogRecordMessage(final String category, final LogRecordMessage.Level level, final String message) {
        final LogRecordMessage logRecord = new LogRecordMessage();
        logRecord.setCategory(category);
        logRecord.setLevel(level);
        logRecord.setMessage(message);
        return logRecord;
    }

    @BeforeSuite
    public void prepareLogger() {
        Logger root = Logger.getRootLogger();
        root.addAppender(_appender);
        root.setLevel(Level.TRACE);
    }

    @BeforeTest
    public void startServer() throws IOException {
        final SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(SLF4FxProtocolEncoder.class, SLF4FxProtocolDecoder.class));
        final Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("myApplication", "mySecret");
        final SLF4FxStateMachine serverStateMachine = new SLF4FxStateMachine();
        serverStateMachine.setKnownApplicaions(credentials);
        acceptor.setHandler(serverStateMachine.createIoHandler());
        acceptor.setReuseAddress(true);
        final InetSocketAddress address = new InetSocketAddress(_PORT);
        acceptor.bind(address);
    }

    public SocketConnector newConnector(final IoHandler ioHandler) throws IOException {
        final SocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(SLF4FxProtocolEncoder.class, SLF4FxProtocolDecoder.class));
        connector.setHandler(ioHandler);
        return connector;
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testAccessDenied() throws IOException {
        final TestClient client = new TestClient("myApplication", "hisSecret", EMPTY_SET);
        final SocketConnector connector = newConnector(client.getIoHandler());
        connector.connect(new InetSocketAddress(_PORT));
        try {
            // give a client chance to send data
            Thread.sleep(400);
        } catch (InterruptedException e) {
            // ignore
        }
        assertEquals(client.getAccessGranted(), Boolean.FALSE);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testAccessGranted() throws IOException {
        final TestClient client = new TestClient("myApplication", "mySecret", EMPTY_SET);
        final SocketConnector connector = newConnector(client.getIoHandler());
        connector.connect(new InetSocketAddress(_PORT));
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
        final Set<LogRecordMessage> messages = new HashSet<LogRecordMessage>();
        messages.add(newLogRecordMessage("org.room13.slf4fx.TestClient", ERROR, "slf4fx log error message"));
        messages.add(newLogRecordMessage("org.room13.slf4fx.TestClient", WARN, "slf4fx log warn message"));
        messages.add(newLogRecordMessage("org.room13.slf4fx.TestClient", INFO, "slf4fx log info message"));
        messages.add(newLogRecordMessage("org.room13.slf4fx.TestClient", DEBUG, "slf4fx log debug message"));
        messages.add(newLogRecordMessage("org.room13.slf4fx.TestClient", TRACE, "slf4fx log trace message"));

        final Set<LogRecordMessage> expectations = new HashSet<LogRecordMessage>();
        expectations.add(newLogRecordMessage("slf4fx.myApplication.org.room13.slf4fx.TestClient", ERROR, "slf4fx log error message"));
        expectations.add(newLogRecordMessage("slf4fx.myApplication.org.room13.slf4fx.TestClient", WARN, "slf4fx log warn message"));
        expectations.add(newLogRecordMessage("slf4fx.myApplication.org.room13.slf4fx.TestClient", INFO, "slf4fx log info message"));
        expectations.add(newLogRecordMessage("slf4fx.myApplication.org.room13.slf4fx.TestClient", DEBUG, "slf4fx log debug message"));
        expectations.add(newLogRecordMessage("slf4fx.myApplication.org.room13.slf4fx.TestClient", TRACE, "slf4fx log trace message"));
        _appender.setExpectation(expectations);

        final TestClient client = new TestClient("myApplication", "mySecret", messages);
        newConnector(client.getIoHandler()).connect(new InetSocketAddress(_PORT));
        try {
            // give a client chance to send data
            Thread.sleep(400);
        } catch (InterruptedException e) {
            // ignore
        }
        assertEquals(_appender.getExpectation().size(), 0);
    }
}

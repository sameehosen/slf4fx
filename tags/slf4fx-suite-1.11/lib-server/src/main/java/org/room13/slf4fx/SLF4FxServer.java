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

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SLF4Fx server.
 * <p/>
 * By default server binds to localhost:18888 with reader buffer size of 1024 bytes,
 * default session timeout is 60 seconds and server does not known any credentials (accepts any).
 */
public class SLF4FxServer {
    private static final Logger _log = LoggerFactory.getLogger(SLF4FxServer.class);
    private SocketAddress _defaultLocalAddress = new InetSocketAddress("localhost", 18888);
    private int _sessionTimeout = 60;
    private int _readerBufferSize = 1024;
    private Map<String, String> _credentials = new HashMap<String, String>();
    private String _flexPolicyResponse = null;
    private NioSocketAcceptor _acceptor = null;

    public void setDefaultLocalAddress(final SocketAddress aDefaultLocalAddress) {
        _defaultLocalAddress = aDefaultLocalAddress;
    }

    public SocketAddress getDefaultLocalAddress() {
        return _defaultLocalAddress;
    }

    public void setSessionTimeout(final int aSessionTimeout) {
        _sessionTimeout = aSessionTimeout;
    }

    public int getSessionTimeout() {
        return _sessionTimeout;
    }

    public void setReaderBufferSize(final int aReaderBufferSize) {
        _readerBufferSize = aReaderBufferSize;
    }

    public int getReaderBufferSize() {
        return _readerBufferSize;
    }

    public void setCredentials(final Map<String, String> aCredentials) {
        _credentials = aCredentials;
    }

    public Map<String, String> getCredentials() {
        return _credentials;
    }

    public void setFlexPolicyResponse(final String aFlexPolicyResponse) {
        _flexPolicyResponse = aFlexPolicyResponse;
    }

    public String getFlexPolicyResponse() {
        return _flexPolicyResponse;
    }

    /**
     * Set credentials from given properties file
     *
     * @param file properties file with applications credentials
     * @throws IOException in case of any IO error
     */
    public void setCredentials(final File file) throws IOException {
        final Properties props = new Properties();
        InputStream istream = null;
        try {
            istream = new FileInputStream(file);
            props.load(istream);
        } finally {
            if (istream != null)
                try {
                    istream.close();
                } catch (IOException e) {
                    // ignore
                }
        }

        final Map<String, String> map = new HashMap<String, String>();
        for (final Object key : props.keySet()) {
            map.put(String.valueOf(key), props.getProperty(String.valueOf(key)));
        }
        setCredentials(map);
    }

    /**
     * Set response for flex &lt;policy-file-request/&gt; from given file.
     *
     * @param file text file with response
     * @throws IOException in case of any IO error
     */
    public void setFlexPolicyResponse(final File file) throws IOException {
        Reader reader = null;
        try {
            reader = new FileReader(file);
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[4096];
            for (int size; (size = reader.read(buffer)) != -1;) {
                sb.append(buffer, 0, size);
            }
            setFlexPolicyResponse(sb.toString());
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
        }
    }

    public void start() throws IOException {
        final long startTime = System.currentTimeMillis();

        _log.info("default local address: {}", _defaultLocalAddress);
        _log.info("session timeout: {} seconds", _sessionTimeout);
        _log.info("reader buffer size: {} bytes", _readerBufferSize);
        _log.info("known credentials: {}", _credentials.keySet());
        _log.info("<policy-file-request/> response: {}",
                _flexPolicyResponse == null ? "disabled" : _flexPolicyResponse);

        final DefaultIoFilterChainBuilder filterChainBuilder = new DefaultIoFilterChainBuilder();
        filterChainBuilder.addLast("codec", new ProtocolCodecFilter(
                SLF4FxProtocolEncoder.class,
                SLF4FxProtocolDecoder.class));

        final SLF4FxStateMachine stateMachine = new SLF4FxStateMachine();
        stateMachine.setSessionTimeout(getSessionTimeout());
        stateMachine.setReaderBufferSize(getReaderBufferSize());
        stateMachine.setCredentials(getCredentials());
        stateMachine.setFlexPolicyResponse(getFlexPolicyResponse());

        _acceptor = new NioSocketAcceptor();
        _acceptor.setHandler(stateMachine.createIoHandler());
        _acceptor.setFilterChainBuilder(filterChainBuilder);
        _acceptor.setReuseAddress(true);
        _acceptor.setCloseOnDeactivation(true);
        _acceptor.setDefaultLocalAddress(getDefaultLocalAddress());

        _acceptor.bind();
        _log.info("slf4fx server started in {} ms", System.currentTimeMillis() - startTime);
    }

    public void stop() {
        if (_acceptor == null) {
            return;
        }
        if (_acceptor.isActive()) {
            _acceptor.unbind();
        }
        _log.info("server stopped");
    }
}
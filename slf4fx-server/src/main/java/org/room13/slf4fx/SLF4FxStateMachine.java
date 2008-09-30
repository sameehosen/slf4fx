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
import org.apache.mina.core.session.IoSession;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.annotation.State;
import org.apache.mina.statemachine.context.IoSessionStateContextLookup;
import org.apache.mina.statemachine.context.StateContext;
import org.apache.mina.statemachine.context.StateContextFactory;
import org.apache.mina.statemachine.event.Event;
import static org.apache.mina.statemachine.event.IoHandlerEvents.*;
import org.room13.slf4fx.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Document the class
 */
public class SLF4FxStateMachine {
    private final Logger _log = LoggerFactory.getLogger(SLF4FxStateMachine.class);
    @State
    public static final String ROOT = "ROOT";
    @State(ROOT)
    public static final String HANDSHAKE = "HANDSHAKE";
    @State(ROOT)
    public static final String IDLE = "IDLE";
    private Map<String, String> _knownApplicaions = new HashMap<String, String>();
    private int _sessionTimeout = 30;
    private String _policyContent = null;
    private int _readerBufferSize = 1024;

    public int getSessionTimeout() {
        return _sessionTimeout;
    }

    public void setSessionTimeout(final int sessionTimeout) {
        _sessionTimeout = sessionTimeout;
    }

    public Map<String, String> getKnownApplicaions() {
        return _knownApplicaions;
    }

    public void setKnownApplicaions(final Map<String, String> knownApplicaions) {
        _knownApplicaions = knownApplicaions;
    }

    public String getPolicyContent() {
        return _policyContent;
    }

    public void setPolicyContent(final String policyContent) {
        _policyContent = policyContent;
    }

    public int getReaderBufferSize() {
        return _readerBufferSize;
    }

    public void setReaderBufferSize(final int readerBufferSize) {
        _readerBufferSize = readerBufferSize;
    }

    @IoHandlerTransition(on = ANY, in = ROOT, weight = 1000)
    public void unhandledEvent(final Event event) {
        final StringBuilder sb = new StringBuilder();
        int argc = 0;
        for (Object o : event.getArguments()) {
            if (argc > 0)
                sb.append('\n');
            sb.append("... [").append(argc++).append("]:=").append(o.getClass().getName());
        }
        _log.debug("[{}] unhandled event {}\n{}", new Object[]{
                event.getContext().getCurrentState().getId(), event.getId(), sb});
    }

    @IoHandlerTransition(on = EXCEPTION_CAUGHT, in = ROOT)
    public void exceptionCaught(final IoSession session, Throwable cause) {
        _log.error("exception caught {}({})", cause.getClass().getName(), cause.getMessage());
        session.close();
    }

    @IoHandlerTransition(on = SESSION_CREATED, in = ROOT)
    public void sessionCreated(final IoSession session) {
        session.getConfig().setReaderIdleTime(getSessionTimeout());
        session.getConfig().setReadBufferSize(getReaderBufferSize());
    }

    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = HANDSHAKE, next = IDLE)
    public void onAccessRequest(final SLF4FxSessionContext context, final IoSession session, final AccessRequest command) {
        if (isApplicationKnown(command.getApplicationId(), command.getSecret())) {
            context.setApplicationId(command.getApplicationId());
            context.setAccessCode(command.getSecret());
            session.write(new AccessResponse(true));
            _log.info("log session for application {} has been started", context.getApplicationId());
            return;
        }
        session.write(new AccessResponse(false));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = HANDSHAKE)
    public void onPolicyFileRequest(final IoSession session, final PolicyFileRequest command) {
        if (_policyContent==null) {
            _log.warn("application has requested policy file but policy was not provided");
            session.close();
        }
        final PolicyFileResponse response = new PolicyFileResponse();
        response.setPolicyContent(_policyContent);
        session.write(response);
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean isApplicationKnown(final String applicationId, final String secret) {
        if (_knownApplicaions.isEmpty())
            return true;
        return _knownApplicaions.containsKey(applicationId)
                && _knownApplicaions.get(applicationId).equals(secret);
    }

    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = IDLE)
    public void onLogRecord(final SLF4FxSessionContext context, final LogRecordMessage logRecord) {
        final String category = new StringBuilder("slf4fx.").append(context.getApplicationId()).append('.').append(logRecord.getCategory()).toString();
        final Logger logger = LoggerFactory.getLogger(category);
        switch (logRecord.getLevel()) {
            case ERROR:
                logger.error(logRecord.getMessage());
                break;
            case WARN:
                logger.warn(logRecord.getMessage());
                break;
            case INFO:
                logger.info(logRecord.getMessage());
                break;
            case DEBUG:
                logger.debug(logRecord.getMessage());
                break;
            case TRACE:
                logger.trace(logRecord.getMessage());
                break;
            default:
                throw new Error("unknown level " + logRecord.getLevel());
        }
    }

    @IoHandlerTransition(on = SESSION_IDLE, in = IDLE)
    public void onIdle(final SLF4FxSessionContext context, final IoSession session) {
        _log.info("inactive log session for application {} has been closed", context.getApplicationId());
        session.close();
    }

    public IoHandler createIoHandler() {
        final StateMachine stateMachine =
                StateMachineFactory.getInstance(IoHandlerTransition.class).create(HANDSHAKE, this);

        return new StateMachineProxyBuilder().setStateContextLookup(
                new IoSessionStateContextLookup(new StateContextFactory() {
                    public StateContext create() {
                        return new SLF4FxSessionContext();
                    }
                })).create(IoHandler.class, stateMachine);
    }
}
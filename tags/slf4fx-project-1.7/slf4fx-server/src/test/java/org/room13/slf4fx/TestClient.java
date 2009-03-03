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
import org.room13.slf4fx.messages.AccessRequest;
import org.room13.slf4fx.messages.AccessResponse;
import org.room13.slf4fx.messages.LogRecordMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * TODO: Document the class
 */
public class TestClient {
    private final Logger _log = LoggerFactory.getLogger(TestClient.class);
    @State
    public static final String ROOT = "ROOT";
    @State(ROOT)
    public static final String START = "START";
    @State(ROOT)
    public static final String HANDSHAKE = "HANDSHAKE";
    @State(ROOT)
    private static final String IDLE = "IDLE";
    private final String _applicationId;
    private final String _secret;
    private final Set<LogRecordMessage> _expectations;
    private Boolean _accessGranted = null;

    public TestClient(final String applicationId, final String secret, final Set<LogRecordMessage> expectations) {
        _applicationId = applicationId;
        _secret = secret;
        _expectations = expectations;
    }

    public String getApplicationId() {
        return _applicationId;
    }

    public String getSecret() {
        return _secret;
    }

    public void setAccessGranted(final Boolean accessGranted) {
        _accessGranted = accessGranted;
    }

    public Boolean getAccessGranted() {
        return _accessGranted;
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
    public void exceptionCaught(Throwable cause) {
        _log.error("exception caught %s(%s)\n", cause.getClass().getName(), cause.getMessage());
    }

    @IoHandlerTransition(on = SESSION_OPENED, in = START, next = HANDSHAKE)
    public void sendAccessRequest(final IoSession session) {
        session.write(new AccessRequest(getApplicationId(), getSecret()));
    }

    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = HANDSHAKE, next = IDLE)
    public void onAccessResponse(final IoSession session, final AccessResponse response) {
        if (response.isAccessGranted()) {
            setAccessGranted(Boolean.TRUE);
            for (LogRecordMessage message : _expectations)
                session.write(message);
            return;
        }
        setAccessGranted(Boolean.FALSE);
        session.close();
    }

    public IoHandler getIoHandler() {
        final StateMachine stateMachine =
                StateMachineFactory.getInstance(IoHandlerTransition.class).create(START, this);

        return new StateMachineProxyBuilder().setStateContextLookup(
                new IoSessionStateContextLookup(new StateContextFactory() {
                    public StateContext create() {
                        return new SLF4FxSessionContext();
                    }
                })).create(IoHandler.class, stateMachine);
    }
}

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

    public Map<String, String> getKnownApplicaions() {
        return _knownApplicaions;
    }

    public void setKnownApplicaions(final Map<String, String> knownApplicaions) {
        _knownApplicaions = knownApplicaions;
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

    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = HANDSHAKE, next = IDLE)
    public void onAccessRequest(final SLF4FxSessionContext context, final IoSession session, final AccessRequest command) {
        if (_knownApplicaions.containsKey(command.getApplicationId())
                && _knownApplicaions.get(command.getApplicationId()).equals(command.getSecret())) {
            context.setApplicationId(command.getApplicationId());
            context.setAccessCode(command.getSecret());
            session.write(new AccessResponse(true));
            _log.info("log session for application {} has been started", context.getApplicationId());
            return;
        }
        session.write(new AccessResponse(false));
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
package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageType.PolicyFileRequest;

import java.nio.charset.CharacterCodingException;

/**
 * TODO: Document the class
 */
public class PolicyFileRequest extends Message {
    private static final String POLICY_FILE_REQUEST = "policy-file-request/>";
    private boolean _valid = false;

    public MessageType getType() {
        return PolicyFileRequest;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(1);
        buffer.setAutoExpand(true);
        buffer.put(getType().getValue());
        buffer.putString(POLICY_FILE_REQUEST, getCharsetEncoder());
        buffer.flip();
        return buffer;
    }

    protected void readIoBuffer(final IoBuffer in) throws CharacterCodingException {
        _valid = POLICY_FILE_REQUEST.equals(in.getString(getCharsetDecoder()));
    }

    public boolean isValid() {
        return _valid;
    }
}

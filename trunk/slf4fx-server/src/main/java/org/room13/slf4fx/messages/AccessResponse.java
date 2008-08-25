package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageTypeId.AccessResponse;

import java.nio.charset.CharacterCodingException;

/**
 * TODO: Document the class
 */
public class AccessResponse implements Message {
    private boolean _isAccessGranted=false;

    public AccessResponse() {
    }

    public AccessResponse(final boolean accessGranted) {
        _isAccessGranted = accessGranted;
    }

    public boolean isAccessGranted() {
        return _isAccessGranted;
    }

    public MessageTypeId getMessageId() {
        return AccessResponse;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(2);
        buffer.put((byte) getMessageId().ordinal());
        buffer.put((byte) (_isAccessGranted ? 1 : 0));
        buffer.flip();
        return buffer;
    }

    public Message read(final IoBuffer in) throws CharacterCodingException {
        _isAccessGranted = in.get() != 0;
        return this;
    }
}

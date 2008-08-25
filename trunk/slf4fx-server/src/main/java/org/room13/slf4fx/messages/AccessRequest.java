package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageTypeId.AccessRequest;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Used by client and server to handshake
 */
public class AccessRequest implements Message {
    private final CharsetEncoder _charsetEncoder = Charset.forName("UTF-8").newEncoder();
    private final CharsetDecoder _charsetDecoder = Charset.forName("UTF-8").newDecoder();
    private String _applicationId;
    private String _secret;

    public AccessRequest() {
    }

    public AccessRequest(final String applicationId, final String secret) {
        _applicationId = applicationId;
        _secret = secret;
    }

    public String getApplicationId() {
        return _applicationId;
    }

    public String getSecret() {
        return _secret;
    }

    public MessageTypeId getMessageId() {
        return AccessRequest;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(1);
        buffer.setAutoExpand(true);
        buffer.put((byte) getMessageId().ordinal());
        buffer.putPrefixedString(getApplicationId(), _charsetEncoder);
        buffer.putPrefixedString(getSecret(), _charsetEncoder);
        buffer.flip();
        return buffer;
    }

    public AccessRequest read(final IoBuffer in) throws CharacterCodingException {
        _applicationId = in.getPrefixedString(_charsetDecoder).intern();
        _secret = in.getPrefixedString(_charsetDecoder).intern();
        return this;
    }
}

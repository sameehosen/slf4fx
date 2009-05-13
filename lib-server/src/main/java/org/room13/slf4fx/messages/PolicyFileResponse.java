package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageType.PolicyFileResponse;

import java.nio.charset.CharacterCodingException;

/**
 * TODO: Document the class
 */
public class PolicyFileResponse extends Message {
    private String _policyContent = null;

    public MessageType getType() {
        return PolicyFileResponse;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(1);
        if (_policyContent != null) {
            buffer.setAutoExpand(true);
            buffer.putString(_policyContent, getCharsetEncoder());
        }
        buffer.put((byte) 0);
        buffer.flip();
        return buffer;
    }

    protected void readIoBuffer(final IoBuffer in) throws CharacterCodingException {
        _policyContent = in.getString(getCharsetDecoder());
    }

    public String getPolicyContent() {
        return _policyContent;
    }

    public void setPolicyContent(final String policyContent) {
        _policyContent = policyContent;
    }
}

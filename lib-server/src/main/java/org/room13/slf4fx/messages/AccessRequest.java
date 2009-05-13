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
package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageType.AccessRequest;

import java.nio.charset.CharacterCodingException;

/**
 * Used by client and server to handshake
 */
public class AccessRequest extends Message {
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

    public MessageType getType() {
        return AccessRequest;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(1);
        buffer.setAutoExpand(true);
        buffer.put(getType().getValue());
        buffer.putPrefixedString(getApplicationId(), getCharsetEncoder());
        buffer.putPrefixedString(getSecret(), getCharsetEncoder());
        buffer.flip();
        return buffer;
    }

    protected void readIoBuffer(final IoBuffer in) throws CharacterCodingException {
        _applicationId = in.getPrefixedString(getCharsetDecoder()).intern();
        _secret = in.getPrefixedString(getCharsetDecoder()).intern();
    }
}

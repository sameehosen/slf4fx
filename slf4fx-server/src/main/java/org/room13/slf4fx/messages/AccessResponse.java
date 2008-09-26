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

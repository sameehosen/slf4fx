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

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.charset.CharacterCodingException;

/**
 * SLF4Fx protocol message. The SLF4Fx protocol is binary protocol. Each message is preceed by message type id (integer)
 * and rest depends on message type.
 */
public interface Message {
    public static enum MessageTypeId {
        Unknown,
        AccessRequest, AccessResponse,
        NewRecord,
        Close
    }

    MessageTypeId getMessageId();

    /**
     * Encodes the message into {@link IoBuffer}
     * @return message encoded as {@link IoBuffer}
     * @throws CharacterCodingException if UTF-8 encoding is not supported by JDK
     */
    IoBuffer toIoBuffer() throws CharacterCodingException;

    /**
     * Reads the message from given {@link IoBuffer}
     * @param in the {@link IoBuffer} to read from. The {@link org.room13.slf4fx.Message.MessageTypeId} encoded as int is already read from the buffer.
     * @return <code>this</code>
     * @throws CharacterCodingException if UTF-8 encoding is not supported by JDK
     */
    Message read(final IoBuffer in) throws CharacterCodingException;
}

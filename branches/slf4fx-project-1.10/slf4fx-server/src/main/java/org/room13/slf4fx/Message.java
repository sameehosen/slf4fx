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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * SLF4Fx protocol message. The SLF4Fx protocol is binary protocol. Each message is preceed by message type id (integer)
 * and rest depends on message type.
 */
public abstract class Message {
    public static enum MessageType {
        Unknown(0),
        AccessRequest(1), AccessResponse(2),
        NewRecord(3),
        Close(4),
        PolicyFileRequest(60), PolicyFileResponse(5);

        private final byte _value;

        private MessageType(final int value) {
            _value = (byte)value;
        }

        public byte getValue() {
            return _value;
        }

        public static MessageType valueOf(final byte value) {
            switch (value) {
                case 60:
                    return PolicyFileRequest;
                case 4:
                    return Close;
                case 3:
                    return NewRecord;
                case 2:
                    return AccessResponse;
                case 1:
                    return AccessRequest;
                case 0:
                default:
                    return Unknown;
            }
        }
    }
    private final CharsetEncoder _charsetEncoder = Charset.forName("UTF-8").newEncoder();
    private final CharsetDecoder _charsetDecoder = Charset.forName("UTF-8").newDecoder();

    protected CharsetEncoder getCharsetEncoder() {
        return _charsetEncoder;
    }

    protected CharsetDecoder getCharsetDecoder() {
        return _charsetDecoder;
    }

    abstract public MessageType getType();

    /**
     * Encodes the message into {@link IoBuffer}
     * @return message encoded as {@link IoBuffer}
     * @throws CharacterCodingException if UTF-8 encoding is not supported by JDK
     */
    abstract public IoBuffer toIoBuffer() throws CharacterCodingException;

    abstract protected void readIoBuffer(final IoBuffer in) throws CharacterCodingException;

    /**
     * Reads the message from given {@link IoBuffer}
     * @param in the {@link IoBuffer} to read from. The {@link org.room13.slf4fx.Message.MessageType} encoded as int is already read from the buffer.
     * @return <code>this</code>
     * @throws CharacterCodingException if UTF-8 encoding is not supported by JDK
     */
    public final Message read(final IoBuffer in) throws CharacterCodingException {
        readIoBuffer(in);
        return this;
    }
}

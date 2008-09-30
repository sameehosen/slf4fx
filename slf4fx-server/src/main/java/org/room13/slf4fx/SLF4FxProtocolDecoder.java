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
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.room13.slf4fx.messages.AccessRequest;
import org.room13.slf4fx.messages.AccessResponse;
import org.room13.slf4fx.messages.LogRecordMessage;
import org.room13.slf4fx.messages.PolicyFileRequest;

/**
 * Reads the message type from input IoBuffer and pass the rest of
 * the buffer to message type instance if type is recognized.
 */
public class SLF4FxProtocolDecoder extends ProtocolDecoderAdapter {
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        while (in.hasRemaining()) {
            switch (Message.MessageType.valueOf(in.get())) {
                case AccessRequest:
                    out.write(new AccessRequest().read(in));
                    break;
                case AccessResponse:
                    out.write(new AccessResponse().read(in));
                    break;
                case NewRecord:
                    out.write(new LogRecordMessage().read(in));
                    break;
                case PolicyFileRequest:
                    final PolicyFileRequest message = (PolicyFileRequest) new PolicyFileRequest().read(in);
                    if (message.isValid()) {
                        out.write(message);
                        break;
                    }
                    throw new IllegalArgumentException("malformed policy file request");
                case Unknown:
                default:
                    throw new UnsupportedOperationException("unsupported command");
            }
        }
    }
}

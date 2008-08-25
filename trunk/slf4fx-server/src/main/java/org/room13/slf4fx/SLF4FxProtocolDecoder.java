package org.room13.slf4fx;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.room13.slf4fx.messages.AccessRequest;
import org.room13.slf4fx.messages.AccessResponse;
import org.room13.slf4fx.messages.LogRecordMessage;

/**
 * Reads the message type from input IoBuffer and pass the rest of
 * the buffer to message type instance if type is recognized.
 */
public class SLF4FxProtocolDecoder extends ProtocolDecoderAdapter {

    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        while (in.hasRemaining()) {
            final int index = in.get();
            final Message.MessageTypeId typeId =
                    index < Message.MessageTypeId.values().length ?
                            Message.MessageTypeId.values()[index] : Message.MessageTypeId.Unknown;

            switch (typeId) {
                case AccessRequest:
                    out.write(new AccessRequest().read(in));
                    break;
                case AccessResponse:
                    out.write(new AccessResponse().read(in));
                    break;
                case NewRecord:
                    out.write(new LogRecordMessage().read(in));
                    break;
                case Unknown:
                default:
                    throw new UnsupportedOperationException("unsupported command");
            }
        }
    }
}

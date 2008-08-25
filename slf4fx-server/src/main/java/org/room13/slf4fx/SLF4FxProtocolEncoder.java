package org.room13.slf4fx;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * TODO: Document the class
 */
public class SLF4FxProtocolEncoder extends ProtocolEncoderAdapter {
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (message instanceof Message) {
            out.write(((Message) message).toIoBuffer());
        }
    }
}

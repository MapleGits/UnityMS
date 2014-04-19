package net.mina;

import client.MapleClient;
import net.MaplePacket;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import tools.MapleCustomEncryption;

public class MaplePacketEncoder implements ProtocolEncoder {
    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            byte[] input = ((MaplePacket) message).getBytes();
            byte[] unencrypted = new byte[input.length];
            System.arraycopy(input, 0, unencrypted, 0, input.length);
            byte[] ret = new byte[unencrypted.length + 4];
            MapleCustomEncryption.encryptData(unencrypted);
            System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
            ByteBuffer out_buffer = ByteBuffer.wrap(ret);
            out.write(out_buffer);
        } else { // no client object created yet, send unencrypted (hello)
            out.write(ByteBuffer.wrap(((MaplePacket) message).getBytes()));
        }
    }

    @Override
    public void dispose(IoSession session) throws Exception {
        // nothing to do
    }
}

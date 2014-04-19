package net.mina;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import tools.MapleCustomEncryption;

public class MaplePacketDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = MaplePacketDecoder.class.getName() + ".STATE";

    private static class DecoderState {
        public int packetlength = -1;
    }

    @Override
    protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        } 
        if (in.remaining() < 4 && decoderState.packetlength == -1) {
            System.out.println("Decode... not enough data");
            return false;
        }
        if (in.remaining() >= decoderState.packetlength) {
            byte decryptedPacket[] = new byte[decoderState.packetlength];
            in.get(decryptedPacket, 0, decoderState.packetlength);
            decoderState.packetlength = -1;
            MapleCustomEncryption.decryptData(decryptedPacket);
            out.write(decryptedPacket);
            return true;
        } else {
            System.out.println("Decode... not enough data to decode");
            System.out.println("Need " + decoderState.packetlength);
            return false;
        }
    }
}

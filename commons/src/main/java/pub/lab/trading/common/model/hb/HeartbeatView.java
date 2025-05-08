package pub.lab.trading.common.model.hb;

import org.agrona.DirectBuffer;
import play.lab.model.sbe.HeartbeatMessageDecoder;

public class HeartbeatView {
    private final HeartbeatMessageDecoder decoder = new HeartbeatMessageDecoder();

    public HeartbeatView wrap(DirectBuffer buffer, int offset, int blockLength, int version) {
        decoder.wrap(buffer, offset, blockLength, version);
        return this;
    }

    public long appId() {
        return decoder.appId();
    }

    public long timestamp() {
        return decoder.timestamp();
    }
}

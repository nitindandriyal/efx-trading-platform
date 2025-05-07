package pub.lab.trading.common.model.hb;

import org.agrona.DirectBuffer;
import play.lab.model.sbe.HeartbeatDecoder;

public class HeartbeatView {
    private final HeartbeatDecoder decoder = new HeartbeatDecoder();

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

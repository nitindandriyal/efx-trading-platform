package pub.lab.trading.common.model.hb;

import org.agrona.MutableDirectBuffer;
import play.lab.model.sbe.HeartbeatEncoder;
import play.lab.model.sbe.MessageHeaderEncoder;

public class HeartbeatWriter {
    private final HeartbeatEncoder encoder = new HeartbeatEncoder();
    private final MessageHeaderEncoder header = new MessageHeaderEncoder();

    public HeartbeatWriter wrap(MutableDirectBuffer buffer, int offset) {
        header.wrap(buffer, offset)
                .blockLength(encoder.sbeBlockLength())
                .templateId(encoder.sbeTemplateId())
                .schemaId(encoder.sbeSchemaId())
                .version(encoder.sbeSchemaVersion());

        encoder.wrap(buffer, offset + header.encodedLength());
        return this;
    }

    public HeartbeatWriter appId(int id) {
        encoder.appId(id);
        return this;
    }

    public HeartbeatWriter timestamp(long ts) {
        encoder.timestamp(ts);
        return this;
    }

    public int encodedLength() {
        return header.encodedLength() + encoder.encodedLength();
    }
}

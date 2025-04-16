package play.lab;

import io.aeron.Publication;
import io.aeron.Subscription;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.YieldingIdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.model.sbe.HeartbeatDecoder;
import pub.lab.trading.model.pricing.QuoteView;
import pub.lab.trading.model.hb.HeartbeatView;
import pub.lab.trading.model.hb.HeartbeatWriter;

import java.nio.ByteBuffer;

public class AeronIpcPoller {
    private static final Logger LOGGER = LoggerFactory.getLogger(AeronIpcPoller.class);

    private final Subscription quoteSub;
    private final Subscription hbSub;
    private final Publication hbPub;
    private final HeartbeatWriter hbWriter = new HeartbeatWriter();
    private final HeartbeatView hbView = new HeartbeatView();
    private final QuoteView quoteView = new QuoteView();
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(512));
    private final IdleStrategy idle = new YieldingIdleStrategy();

    private final int appId;
    private final long heartbeatIntervalMs;
    private long lastHbTime = 0;

    private volatile boolean running = true;

    public AeronIpcPoller(Subscription quoteSub, Subscription hbSub, Publication hbPub, int appId, long hbIntervalMs) {
        this.quoteSub = quoteSub;
        this.hbSub = hbSub;
        this.hbPub = hbPub;
        this.appId = appId;
        this.heartbeatIntervalMs = hbIntervalMs;
    }

    public void stop() {
        running = false;
    }

    public void run() {
        while (running) {
            boolean didWork = false;

            int q = quoteSub.poll((buf, offset, len, hdr) -> {
                quoteView.wrap(buf, offset);
                LOGGER.info("Received quote : {}", quoteView.priceCreationTimestamp());
            }, 10);
            didWork |= q > 0;

            int h = hbSub.poll((buf, offset, len, hdr) -> {
                hbView.wrap(buf, offset + 8, HeartbeatDecoder.BLOCK_LENGTH, HeartbeatDecoder.SCHEMA_VERSION);
            }, 10);
            didWork |= h > 0;

            long now = System.currentTimeMillis();
            if (now - lastHbTime >= heartbeatIntervalMs) {
                hbWriter.wrap(buffer, 0).appId(appId).timestamp(now);
                hbPub.offer(buffer, 0, hbWriter.encodedLength());
                lastHbTime = now;
                didWork = true;
            }

            if (!didWork) idle.idle(0);
            else idle.reset();
        }
    }

    public void init() {
        run();
    }
}


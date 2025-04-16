package play.lab.pricing.engine;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.YieldingIdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.model.sbe.HeartbeatDecoder;
import pub.lab.trading.AppId;
import pub.lab.trading.StreamId;
import pub.lab.trading.model.hb.HeartbeatView;
import pub.lab.trading.model.hb.HeartbeatWriter;
import pub.lab.trading.model.pricing.QuoteView;

import java.nio.ByteBuffer;

public class CoreEventLoop {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreEventLoop.class);

    private final HeartbeatWriter hbWriter = new HeartbeatWriter();
    private final HeartbeatView hbView = new HeartbeatView();
    private final QuoteView quoteView = new QuoteView();
    private final long heartbeatIntervalMs;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(512));
    private final IdleStrategy idle = new YieldingIdleStrategy();
    private final Subscription quoteSub;
    private final Publication marketQuotePub;
    private final Subscription heartbeatSub;
    private final Publication heartbeatPub;

    private long lastHbTime = 0;
    private volatile boolean running = true;

    public CoreEventLoop(long heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
        String aeronDir = System.getProperty("aeron.base.path") + "/trading";
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
        this.quoteSub = aeron.addSubscription("aeron:ipc", StreamId.RAW_QUOTE.getCode());
        this.marketQuotePub = aeron.addExclusivePublication("aeron:ipc", StreamId.MARKET_QUOTE.getCode());
        this.heartbeatSub = aeron.addSubscription("aeron:ipc", StreamId.HEARTBEAT.getCode());
        this.heartbeatPub = aeron.addExclusivePublication("aeron:ipc", StreamId.HEARTBEAT.getCode());
    }

    void runLoop() {
        while (running) {
            boolean didWork = false;

            int q = quoteSub.poll((buf, offset, len, hdr) -> {
                quoteView.wrap(buf, offset);
                LOGGER.info("Received quote : {}", quoteView.priceCreationTimestamp());
            }, 10);
            didWork |= q > 0;

            int h = heartbeatSub.poll((buf, offset, len, hdr) -> {
                hbView.wrap(buf, offset + 8, HeartbeatDecoder.BLOCK_LENGTH, HeartbeatDecoder.SCHEMA_VERSION);
            }, 10);
            didWork |= h > 0;

            long now = System.currentTimeMillis();
            if (now - lastHbTime >= heartbeatIntervalMs) {
                hbWriter.wrap(buffer, 0).appId(AppId.PRICING_ENGINE.getCode()).timestamp(now);
                heartbeatPub.offer(buffer, 0, hbWriter.encodedLength());
                lastHbTime = now;
                didWork = true;
            }

            if (!didWork) {
                idle.idle(0);
            } else {
                idle.reset();
            }
        }
    }

    void stop() {
        running = false;
        cleanup();
    }

    private void cleanup() {
        // todo
    }
}

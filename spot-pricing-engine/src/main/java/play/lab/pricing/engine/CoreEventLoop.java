package play.lab.pricing.engine;

import io.aeron.Aeron;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.pricing.engine.feed.MarketDataConsumer;
import pub.lab.trading.common.config.AppId;
import pub.lab.trading.common.lifecycle.HeartBeatAgent;
import pub.lab.trading.common.lifecycle.MultiStreamPoller;
import pub.lab.trading.common.lifecycle.Worker;

public class CoreEventLoop {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreEventLoop.class);

    private final AgentRunner agentRunner;
    private final Aeron aeron;

    public CoreEventLoop(final IdleStrategy idleStrategy, final long heartbeatIntervalMs) {
        String aeronDir = System.getProperty("aeron.base.path") + "/trading";
        this.aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
        agentRunner = new AgentRunner(idleStrategy, Throwable::printStackTrace, null, new MultiStreamPoller(
                "pricing-engine-poller",
                new Worker[]{
                        new MarketDataConsumer(aeron),
                        new HeartBeatAgent(AppId.PRICING_ENGINE, heartbeatIntervalMs, aeron)
                }
        ));
    }

    void start() {
        AgentRunner.startOnThread(agentRunner);
    }

    void stop() {
        agentRunner.close();
        aeron.close();
    }
}

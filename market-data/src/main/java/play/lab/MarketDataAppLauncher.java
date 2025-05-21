package play.lab;

import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.BackoffIdleStrategy;
import play.lab.marketdata.generator.FxPriceGenerator;
import pub.lab.trading.common.lifecycle.MultiStreamPoller;
import pub.lab.trading.common.lifecycle.Worker;

public class MarketDataAppLauncher {

    public static void main(String[] args) {
        AgentRunner agentRunner = new AgentRunner(new BackoffIdleStrategy(),
                Throwable::printStackTrace,
                null,
                new MultiStreamPoller(
                        "pricing-engine-poller",
                        new Worker[]{
                                new FxPriceGenerator()
                        }
                ));
        AgentRunner.startOnThread(agentRunner);
    }
}

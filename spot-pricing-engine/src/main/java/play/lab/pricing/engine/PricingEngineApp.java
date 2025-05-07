package play.lab.pricing.engine;

import org.agrona.concurrent.BackoffIdleStrategy;

public class PricingEngineApp {
    public static void main(String[] args) {
        new CoreEventLoop(new BackoffIdleStrategy(), 1_000).start();
    }
}

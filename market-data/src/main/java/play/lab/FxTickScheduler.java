package play.lab;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FxTickScheduler {
    private final FxPriceGenerator generator;
    private final TickThrottle throttle;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public FxTickScheduler(FxPriceGenerator gen, TickThrottle thr) {
        this.generator = gen;
        this.throttle = thr;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            generator.generateAll(System.currentTimeMillis(), throttle.getDtSeconds());
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
}

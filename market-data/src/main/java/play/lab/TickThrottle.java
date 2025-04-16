package play.lab;

public class TickThrottle {
    private volatile int ticksPerSecond;

    public TickThrottle(int defaultTps) {
        this.ticksPerSecond = defaultTps;
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }

    public void setTicksPerSecond(int tps) {
        this.ticksPerSecond = tps;
    }

    public double getDtSeconds() {
        return 1.0 / Math.max(1, ticksPerSecond);
    }
}

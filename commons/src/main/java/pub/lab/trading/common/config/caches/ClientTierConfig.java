package pub.lab.trading.common.config.caches;

import java.util.Objects;

public class ClientTierConfig {
    private volatile int tierId; // uint16
    private volatile String tierName; // string64
    private volatile double markupBps; // double
    private volatile double spreadTighteningFactor; // double
    private volatile long quoteThrottleMs; // uint32
    private volatile long latencyProtectionMs; // uint32
    private volatile long quoteExpiryMs; // uint32
    private volatile double minNotional; // double
    private volatile double maxNotional; // double
    private volatile short pricePrecision; // uint8
    private volatile boolean streamingEnabled; // BooleanEnum
    private volatile boolean limitOrderEnabled; // BooleanEnum
    private volatile boolean accessToCrosses; // BooleanEnum
    private volatile double creditLimitUsd; // double
    private volatile short tierPriority; // uint8
    private volatile double tierSkew; // double
    private volatile double clientTierSkew; // double
    private volatile double signal; // double

    public ClientTierConfig init(
            int tierId,
            String tierName,
            double markupBps,
            double spreadTighteningFactor,
            long quoteThrottleMs,
            long latencyProtectionMs,
            long quoteExpiryMs,
            double minNotional,
            double maxNotional,
            short pricePrecision,
            boolean streamingEnabled,
            boolean limitOrderEnabled,
            boolean accessToCrosses,
            double creditLimitUsd,
            short tierPriority,
            double tierSkew,
            double clientTierSkew,
            double signal
    ) {
        validate(tierName, markupBps, spreadTighteningFactor, quoteThrottleMs, latencyProtectionMs,
                quoteExpiryMs, minNotional, maxNotional, pricePrecision, creditLimitUsd, tierPriority);
        this.tierId = tierId;
        this.tierName = tierName;
        this.markupBps = markupBps;
        this.spreadTighteningFactor = spreadTighteningFactor;
        this.quoteThrottleMs = quoteThrottleMs;
        this.latencyProtectionMs = latencyProtectionMs;
        this.quoteExpiryMs = quoteExpiryMs;
        this.minNotional = minNotional;
        this.maxNotional = maxNotional;
        this.pricePrecision = pricePrecision;
        this.streamingEnabled = streamingEnabled;
        this.limitOrderEnabled = limitOrderEnabled;
        this.accessToCrosses = accessToCrosses;
        this.creditLimitUsd = creditLimitUsd;
        this.tierPriority = tierPriority;
        this.tierSkew = tierSkew;
        this.clientTierSkew = clientTierSkew;
        this.signal = signal;
        return this;
    }

    public ClientTierConfig update(
            String tierName,
            double markupBps,
            double spreadTighteningFactor,
            long quoteThrottleMs,
            long latencyProtectionMs,
            long quoteExpiryMs,
            double minNotional,
            double maxNotional,
            short pricePrecision,
            boolean streamingEnabled,
            boolean limitOrderEnabled,
            boolean accessToCrosses,
            double creditLimitUsd,
            short tierPriority,
            double tierSkew,
            double clientTierSkew,
            double signal
    ) {
        validate(tierName, markupBps, spreadTighteningFactor, quoteThrottleMs, latencyProtectionMs,
                quoteExpiryMs, minNotional, maxNotional, pricePrecision, creditLimitUsd, tierPriority);
        this.tierName = tierName;
        this.markupBps = markupBps;
        this.spreadTighteningFactor = spreadTighteningFactor;
        this.quoteThrottleMs = quoteThrottleMs;
        this.latencyProtectionMs = latencyProtectionMs;
        this.quoteExpiryMs = quoteExpiryMs;
        this.minNotional = minNotional;
        this.maxNotional = maxNotional;
        this.pricePrecision = pricePrecision;
        this.streamingEnabled = streamingEnabled;
        this.limitOrderEnabled = limitOrderEnabled;
        this.accessToCrosses = accessToCrosses;
        this.creditLimitUsd = creditLimitUsd;
        this.tierPriority = tierPriority;
        this.tierSkew = tierSkew;
        this.clientTierSkew = clientTierSkew;
        this.signal = signal;
        return this;
    }

    private void validate(
            String tierName,
            double markupBps,
            double spreadTighteningFactor,
            long quoteThrottleMs,
            long latencyProtectionMs,
            long quoteExpiryMs,
            double minNotional,
            double maxNotional,
            short pricePrecision,
            double creditLimitUsd,
            short tierPriority
    ) {
        Objects.requireNonNull(tierName, "Tier name must not be null");
        if (tierName.isEmpty()) {
            throw new IllegalArgumentException("Tier name must not be empty");
        }
        if (markupBps < 0) {
            throw new IllegalArgumentException("Markup basis points must be non-negative");
        }
        if (spreadTighteningFactor < 0) {
            throw new IllegalArgumentException("Spread tightening factor must be non-negative");
        }
        if (quoteThrottleMs < 0) {
            throw new IllegalArgumentException("Quote throttle milliseconds must be non-negative");
        }
        if (latencyProtectionMs < 0) {
            throw new IllegalArgumentException("Latency protection milliseconds must be non-negative");
        }
        if (quoteExpiryMs < 0) {
            throw new IllegalArgumentException("Quote expiry milliseconds must be non-negative");
        }
        if (minNotional < 0) {
            throw new IllegalArgumentException("Minimum notional must be non-negative");
        }
        if (maxNotional < minNotional) {
            throw new IllegalArgumentException("Maximum notional must be at least minimum notional");
        }
        if (pricePrecision < 0) {
            throw new IllegalArgumentException("Price precision must be non-negative");
        }
        if (creditLimitUsd < 0) {
            throw new IllegalArgumentException("Credit limit USD must be non-negative");
        }
        if (tierPriority < 0) {
            throw new IllegalArgumentException("Tier priority must be non-negative");
        }
    }

    public int tierId() {
        return tierId;
    }

    public String tierName() {
        return tierName;
    }

    public double markupBps() {
        return markupBps;
    }

    public double spreadTighteningFactor() {
        return spreadTighteningFactor;
    }

    public long quoteThrottleMs() {
        return quoteThrottleMs;
    }

    public long latencyProtectionMs() {
        return latencyProtectionMs;
    }

    public long quoteExpiryMs() {
        return quoteExpiryMs;
    }

    public double minNotional() {
        return minNotional;
    }

    public double maxNotional() {
        return maxNotional;
    }

    public short pricePrecision() {
        return pricePrecision;
    }

    public boolean streamingEnabled() {
        return streamingEnabled;
    }

    public boolean limitOrderEnabled() {
        return limitOrderEnabled;
    }

    public boolean accessToCrosses() {
        return accessToCrosses;
    }

    public double creditLimitUsd() {
        return creditLimitUsd;
    }

    public short tierPriority() {
        return tierPriority;
    }

    public double tierSkew() {
        return tierSkew;
    }

    public double clientTierSkew() {
        return clientTierSkew;
    }

    public double signal() {
        return signal;
    }

    @Override
    public String toString() {
        return "ClientTierConfig{" +
                "tierId=" + tierId +
                ", tierName='" + tierName + '\'' +
                ", markupBps=" + markupBps +
                ", spreadTighteningFactor=" + spreadTighteningFactor +
                ", quoteThrottleMs=" + quoteThrottleMs +
                ", latencyProtectionMs=" + latencyProtectionMs +
                ", quoteExpiryMs=" + quoteExpiryMs +
                ", minNotional=" + minNotional +
                ", maxNotional=" + maxNotional +
                ", pricePrecision=" + pricePrecision +
                ", streamingEnabled=" + streamingEnabled +
                ", limitOrderEnabled=" + limitOrderEnabled +
                ", accessToCrosses=" + accessToCrosses +
                ", creditLimitUsd=" + creditLimitUsd +
                ", tierPriority=" + tierPriority +
                ", tierSkew=" + tierSkew +
                ", clientTierSkew=" + clientTierSkew +
                ", signal=" + signal +
                '}';
    }
}
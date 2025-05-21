// simplified for brevity
package play.lab.marketdata.generator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.TickThrottle;
import play.lab.marketdata.model.MarketDataTick;
import play.lab.marketdata.model.RawPriceConfig;
import pub.lab.trading.common.lifecycle.Worker;
import pub.lab.trading.common.util.HolidayCalendar;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FxPriceGenerator implements Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(FxPriceGenerator.class);

    private static final double DEFAULT_SPREAD_BP = 0.5;
    private static final double DEFAULT_VOLATILITY = 0.5;
    private final Map<String, PairModel> pairs = new HashMap<>();
    private final Map<String, RawPriceConfig> configOverridesByCcy = new ConcurrentHashMap<>();
    private final RawPriceConfig defaultConfig = new RawPriceConfig("XXX", DEFAULT_VOLATILITY, DEFAULT_SPREAD_BP);
    private final QuotePublisher aeronPub = new QuotePublisher();
    private final TickThrottle throttle = new TickThrottle(1000);

    public FxPriceGenerator() {
        // Volatility overrides (annualized)
        configOverridesByCcy.put("USD", new RawPriceConfig("USD", 0.020, 0.5)); // US Dollar
        configOverridesByCcy.put("EUR", new RawPriceConfig("EUR", 0.018, 0.5)); // Euro
        configOverridesByCcy.put("JPY", new RawPriceConfig("JPY", 0.030, 1.0)); // Japanese Yen
        configOverridesByCcy.put("GBP", new RawPriceConfig("GBP", 0.025, 0.6)); // British Pound
        configOverridesByCcy.put("CHF", new RawPriceConfig("CHF", 0.017, 0.4)); // Swiss Franc
        configOverridesByCcy.put("AUD", new RawPriceConfig("AUD", 0.028, 0.6)); // Australian Dollar
        configOverridesByCcy.put("NZD", new RawPriceConfig("NZD", 0.030, 0.7)); // New Zealand Dollar
        configOverridesByCcy.put("CAD", new RawPriceConfig("CAD", 0.022, 0.5)); // Canadian Dollar

        // Default major pairs and crosses
        add("EURUSD", 1.1000);
        add("USDJPY", 145.00);
        add("GBPUSD", 1.2500);
        add("USDCHF", 0.8800);
        add("AUDUSD", 0.6600);
        add("NZDUSD", 0.6000);
        add("USDCAD", 1.3600);

        // Crosses (most traded)
        add("EURJPY", 158.00);
        add("EURGBP", 0.8800);
        add("EURCHF", 0.9700);
        add("GBPJPY", 184.50);
        add("AUDJPY", 98.50);
        add("NZDJPY", 90.20);
        add("CADJPY", 107.30);
        add("AUDNZD", 1.0700);
        add("EURCAD", 1.4700);
        add("GBPCHF", 1.1100);

    }

    private void add(String pair, double initialPrice) {
        double volatility = inferVolatility(pair);
        double spread = inferSpread(pair);
        pairs.put(pair, new PairModel(pair, initialPrice, volatility, spread));
    }

    public void addSymbol(String symbol, double initialPrice, double volatility, double spread) {
        configOverridesByCcy.put(symbol.substring(0, 3), new RawPriceConfig(symbol.substring(0, 3), volatility, spread));
        configOverridesByCcy.put(symbol.substring(3), new RawPriceConfig(symbol.substring(0, 3), volatility, spread));
        pairs.put(symbol, new PairModel(symbol, initialPrice, volatility, spread));
    }

    private double inferVolatility(String pair) {
        String base = pair.substring(0, 3);
        String quote = pair.substring(3);
        return (configOverridesByCcy.getOrDefault(base, defaultConfig).getVolatility()
                + configOverridesByCcy.getOrDefault(quote, defaultConfig).getVolatility()) / 2;
    }

    private double inferSpread(String pair) {
        String base = pair.substring(0, 3);
        String quote = pair.substring(3);
        return Math.max(
                configOverridesByCcy.getOrDefault(base, defaultConfig).getSpread(),
                configOverridesByCcy.getOrDefault(quote, defaultConfig).getSpread()
        );
    }

    public double getVol(String symbol) {
        String base = symbol.substring(0, 3);
        String quote = symbol.substring(3);
        return (configOverridesByCcy.getOrDefault(base, defaultConfig).getVolatility()
                + configOverridesByCcy.getOrDefault(quote, defaultConfig).getVolatility()) * 0.5;
    }

    public double getSpread(String symbol) {
        String base = symbol.substring(0, 3);
        String quote = symbol.substring(3);
        return Math.max(
                configOverridesByCcy.getOrDefault(base, defaultConfig).getSpread(),
                configOverridesByCcy.getOrDefault(quote, defaultConfig).getSpread()
        );
    }

    public void updateModel(String symbol, double vol, double spread) {
        String base = symbol.substring(0, 3);
        configOverridesByCcy.compute(base, (k, oldValue) -> {
            if (oldValue == null) {
                return new RawPriceConfig(base, vol, spread);
            } else {
                oldValue.setSpread(spread);
                oldValue.setVolatility(vol);
                return oldValue;
            }
        });
        String term = symbol.substring(3);
        configOverridesByCcy.compute(term, (k, oldValue) -> {
            if (oldValue == null) {
                return new RawPriceConfig(term, vol, spread);
            } else {
                oldValue.setSpread(spread);
                oldValue.setVolatility(vol);
                return oldValue;
            }
        });

        if (pairs.containsKey(symbol)) {
            pairs.get(symbol).setVolatility(vol);
            pairs.get(symbol).setSpread(spread);
        }
    }

    public Set<String> symbols() {
        return pairs.keySet();
    }

    public List<MarketDataTick> generateAll(long now, double dtSeconds) {
        List<MarketDataTick> ticks = new ArrayList<>();
        for (Map.Entry<String, PairModel> entry : pairs.entrySet()) {
            PairModel model = entry.getValue();
            MarketDataTick tick = model.nextTick(now, dtSeconds);
            ticks.add(tick);
            aeronPub.publish(tick);
        }
        return ticks;
    }

    public List<RawPriceConfig> generateAllConfig() {
        return new ArrayList<>(configOverridesByCcy.values());
    }

    public MarketDataTick generate(String symbol, long now, double dtSeconds) {
        PairModel model = pairs.get(symbol);
        if (model == null) {
            throw new IllegalArgumentException("Unknown symbol: " + symbol);
        }
        return model.nextTick(now, dtSeconds);
    }

    @Override
    public int doWork() {
        generateAll(System.nanoTime(), throttle.getDtSeconds());
        return 1;
    }

    @Override
    public String roleName() {
        return "";
    }

    private static class PairModel {
        final String symbol;
        double price, volatility, spread;

        PairModel(String symbol, double price, double vol, double spr) {
            this.symbol = symbol;
            this.price = price;
            this.volatility = vol;
            this.spread = spr;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getVolatility() {
            return volatility;
        }

        public void setVolatility(double volatility) {
            this.volatility = volatility;
        }

        public double getSpread() {
            return spread;
        }

        public void setSpread(double spread) {
            this.spread = spread;
        }

        MarketDataTick nextTick(long now, double dt) {
            double z = ThreadLocalRandom.current().nextGaussian();
            price *= Math.exp(-0.5 * volatility * volatility * dt + volatility * Math.sqrt(dt) * z);
            double spread = price * this.spread / 10000;
            return new MarketDataTick(symbol, price, (price - spread) * 0.5, (price + spread) * 0.5, HolidayCalendar.getValueDate(), now);
        }
    }
}

package play.lab.marketdata.model;

public class RawPriceConfig {
    private final String ccy;
    private double volatility;
    private double spread;

    public RawPriceConfig(String symbol, double vol, double spr) {
        this.ccy = symbol;
        this.volatility = vol;
        this.spread = spr;
    }

    public String getCcy() {
        return ccy;
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
}

package play.lab.pricing.engine.feed;

public class SpotPricer {
    public static double[] calculate(ClientTier tier, double marketBid, double marketAsk, int volume) {
        double mid = (marketBid + marketAsk) / 2.0;
        double volFactor = Math.log10(volume / 1_000_000.0 + 1.0);
        double spreadAdj = tier.spread * (1 + 0.05 * volFactor);
        double markupAdj = tier.markup * (1 + 0.1 * volFactor);
        double skewAdj = tier.skew * volFactor;
        double adj = tier.signal * (markupAdj + skewAdj);
        double bid = mid - (spreadAdj / 2.0) - adj;
        double ask = mid + (spreadAdj / 2.0) + adj;
        return new double[]{bid, ask};
    }
}

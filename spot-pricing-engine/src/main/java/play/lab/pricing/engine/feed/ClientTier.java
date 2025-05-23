package play.lab.pricing.engine.feed;

import pub.lab.trading.common.model.ClientTierLevel;

public class ClientTier {
    double spread;
    double markup;
    double skew;
    double signal;
    private ClientTierLevel clientTierLevel;

    public ClientTier(ClientTierLevel clientTierLevel, double spread, double markup, double skew, double signal) {
        this.clientTierLevel = clientTierLevel;
        this.spread = spread;
        this.markup = markup;
        this.skew = skew;
        this.signal = signal;
    }
}

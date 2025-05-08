package play.lab.pricing.engine.feed;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.model.sbe.QuoteMessageDecoder;
import pub.lab.trading.common.config.StreamId;
import pub.lab.trading.common.lifecycle.Worker;
import pub.lab.trading.common.model.ClientTierLevel;
import pub.lab.trading.common.model.pricing.QuoteView;
import pub.lab.trading.common.model.pricing.QuoteWriter;

import java.util.EnumMap;

public class MarketDataConsumer implements Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataConsumer.class);

    private final Subscription quoteSub;
    private final EnumMap<ClientTierLevel, Publication> marketQuotePublications = new EnumMap<>(ClientTierLevel.class);
    private final EnumMap<ClientTierLevel, ClientTier> clientTierEnumMap = new EnumMap<>(ClientTierLevel.class);
    private final EnumMap<ClientTierLevel, QuoteWriter> clientTierQuoteWriterEnumMap = new EnumMap<>(ClientTierLevel.class);
    private final QuoteView quoteView = new QuoteView();

    public MarketDataConsumer(final Aeron aeron) {
        this.quoteSub = aeron.addSubscription("aeron:ipc", StreamId.RAW_QUOTE.getCode());
        for (ClientTierLevel clientTierLevel : ClientTierLevel.values()) {
            marketQuotePublications.put(clientTierLevel, aeron.addExclusivePublication("aeron:ipc",
                    StreamId.MARKET_QUOTE.getCode() + clientTierLevel.getId())
            );
        }
    }

    @Override
    public void onStart() {
        Worker.super.onStart();
    }

    @Override
    public int doWork() {
        return quoteSub.poll((buf, offset, len, hdr) -> {
            quoteView.wrap(buf, offset);
            double marketBid = quoteView.priceCreationTimestamp();
            double marketAsk;

            for (ClientTierLevel clientTierLevel : ClientTierLevel.values()) {
                QuoteWriter quoteWriter = clientTierQuoteWriterEnumMap.get(clientTierLevel);

                while (quoteView.getRung().hasNext()) {
                    QuoteMessageDecoder.RungDecoder nextRung = quoteView.getRung().next();
                    double mid = (nextRung.bid() + nextRung.ask()) / 2.0;
                    double volFactor = Math.log10(nextRung.volume() / 1_000_000.0 + 1.0);
                    ClientTier clientTier = clientTierEnumMap.get(clientTierLevel);
                    double spreadAdjust = clientTier.spread * (1 + 0.05 * volFactor);
                    double markupAdjust = clientTier.markup * (1 + 0.1 * volFactor);
                    double skewAdjust = clientTier.skew * volFactor;
                    double adjustment = clientTier.signal * (markupAdjust + skewAdjust);
                    double bid = mid - (spreadAdjust / 2.0) - adjustment;
                    double ask = mid + (spreadAdjust / 2.0) + adjustment;

                    LOGGER.info("Received quote : {}", quoteView.priceCreationTimestamp());
                }
            }
        }, 10);
    }

    @Override
    public void onClose() {
        Worker.super.onClose();
    }

    @Override
    public String roleName() {
        return "MarketDataConsumer";
    }
}

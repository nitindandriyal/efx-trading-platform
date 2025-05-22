package pub.lab.trading.common.config.caches;

import org.agrona.collections.Long2ObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.model.sbe.BooleanEnum;
import play.lab.model.sbe.ClientTierConfigMessageDecoder;
import pub.lab.trading.common.lifecycle.ArrayObjectPool;
import pub.lab.trading.common.model.ClientTierLevel;

public class ClientTierConfigCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTierConfigCache.class);

    private final Long2ObjectHashMap<ClientTierConfig> clientTierConfigCache = new Long2ObjectHashMap<>();
    private final ArrayObjectPool<ClientTierConfig> clientTierConfigArrayObjectPool = new ArrayObjectPool<>("clientTierConfigArrayObjectPool", ClientTierConfig::new);

    public ClientTierConfig get(int tierId) {
        return clientTierConfigCache.get(tierId);
    }

    public void update(final ClientTierLevel clientTierLevel, final ClientTierConfig clientTier) {
        clientTierConfigCache.put(clientTierLevel.getId(), clientTier);
    }

    public void update(final ClientTierConfigMessageDecoder clientTierDecoder) {
        if (clientTierConfigCache.containsKey(clientTierDecoder.tierId())) {
            ClientTierConfig config = clientTierConfigCache.get(clientTierDecoder.tierId()).update(
                    clientTierDecoder.tierName(),
                    clientTierDecoder.markupBps(),
                    clientTierDecoder.spreadTighteningFactor(),
                    clientTierDecoder.quoteThrottleMs(),
                    clientTierDecoder.latencyProtectionMs(),
                    clientTierDecoder.quoteExpiryMs(),
                    clientTierDecoder.minNotional(),
                    clientTierDecoder.maxNotional(),
                    clientTierDecoder.pricePrecision(),
                    clientTierDecoder.streamingEnabled() == BooleanEnum.True,
                    clientTierDecoder.limitOrderEnabled() == BooleanEnum.True,
                    clientTierDecoder.accessToCrosses() == BooleanEnum.True,
                    clientTierDecoder.creditLimitUsd(),
                    clientTierDecoder.tierPriority(),
                    clientTierDecoder.tierSkew(),
                    clientTierDecoder.clientTierSkew(),
                    clientTierDecoder.signal()
            );
            LOGGER.debug("Updated clientTierConfigCache :: {}", config);
        } else {
            ClientTierConfig config = clientTierConfigArrayObjectPool.get().init(clientTierDecoder.tierId(),
                    clientTierDecoder.tierName(),
                    clientTierDecoder.markupBps(),
                    clientTierDecoder.spreadTighteningFactor(),
                    clientTierDecoder.quoteThrottleMs(),
                    clientTierDecoder.latencyProtectionMs(),
                    clientTierDecoder.quoteExpiryMs(),
                    clientTierDecoder.minNotional(),
                    clientTierDecoder.maxNotional(),
                    clientTierDecoder.pricePrecision(),
                    clientTierDecoder.streamingEnabled() == BooleanEnum.True,
                    clientTierDecoder.limitOrderEnabled() == BooleanEnum.True,
                    clientTierDecoder.accessToCrosses() == BooleanEnum.True,
                    clientTierDecoder.creditLimitUsd(),
                    clientTierDecoder.tierPriority(),
                    clientTierDecoder.tierSkew(),
                    clientTierDecoder.clientTierSkew(),
                    clientTierDecoder.signal()
            );
            clientTierConfigCache.put(config.tierId(), config);
            LOGGER.debug("Added clientTierConfigCache :: {}", config);
        }
    }
}

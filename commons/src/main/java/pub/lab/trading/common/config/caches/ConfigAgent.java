package pub.lab.trading.common.config.caches;

import io.aeron.Aeron;
import io.aeron.Subscription;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.model.sbe.*;
import pub.lab.trading.common.config.StreamId;
import pub.lab.trading.common.lifecycle.ArrayObjectPool;

import static pub.lab.trading.common.config.AeronConfigs.CONFIG_CHANNEL;

public class ConfigAgent implements Agent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigAgent.class);

    private final Subscription subscription;
    private final CurrencyConfigMessageDecoder currencyDecoder;
    private final ClientTierConfigMessageDecoder clientTierDecoder;
    private final ArrayObjectPool<CurrencyConfig> currencyConfigArrayObjectPool = new ArrayObjectPool<>("currencyConfigArrayObjectPool", CurrencyConfig::new);
    private final ArrayObjectPool<ClientTierConfig> clientTierConfigArrayObjectPool = new ArrayObjectPool<>("clientTierConfigArrayObjectPool", ClientTierConfig::new);
    private final ConfigLoadCompleteMessageDecoder completeDecoder;
    private final UnsafeBuffer buffer;
    // caches
    private final Long2ObjectHashMap<CurrencyConfig> currencyCache = new Long2ObjectHashMap<>();
    private final Long2ObjectHashMap<ClientTierConfig> clientTierConfigCache = new Long2ObjectHashMap<>();
    private volatile boolean isInitialLoadComplete;

    public ConfigAgent(Aeron aeron) {
        this.subscription = aeron.addSubscription(CONFIG_CHANNEL, StreamId.CONFIG_STREAM.getCode());
        this.currencyDecoder = new CurrencyConfigMessageDecoder();
        this.clientTierDecoder = new ClientTierConfigMessageDecoder();
        this.completeDecoder = new ConfigLoadCompleteMessageDecoder();
        this.buffer = new UnsafeBuffer(new byte[256]);
        this.isInitialLoadComplete = false;
    }

    @Override
    public String roleName() {
        return "ConfigProcessor";
    }

    @Override
    public int doWork() {
        return subscription.poll((buf, offset, length, header) -> {
            buffer.putBytes(0, buf, offset, length);
            MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
            headerDecoder.wrap(buffer, 0);
            int templateId = headerDecoder.templateId();

            if (templateId == CurrencyConfigMessageDecoder.TEMPLATE_ID) {
                updateCurrencyConfig(headerDecoder);
            } else if (templateId == ClientTierConfigMessageDecoder.TEMPLATE_ID) {
                updateClientTierConfig(headerDecoder);
            } else if (templateId == ConfigLoadCompleteMessageDecoder.TEMPLATE_ID) {
                if (!isInitialLoadComplete) {
                    completeDecoder.wrapAndApplyHeader(buffer, 0, headerDecoder);
                    long timestamp = completeDecoder.timestamp();
                    System.out.println("Initial config load complete at timestamp: " + timestamp);
                    isInitialLoadComplete = true;
                }
            } else {
                System.out.println("Unknown config message templateId: " + templateId);
            }
        }, 10);
    }

    private void updateClientTierConfig(MessageHeaderDecoder headerDecoder) {
        clientTierDecoder.wrapAndApplyHeader(buffer, 0, headerDecoder);
        if (clientTierConfigCache.containsKey(currencyDecoder.id())) {
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
                    clientTierDecoder.tierPriority()
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
                    clientTierDecoder.tierPriority()
            );
            clientTierConfigCache.put(config.tierPriority(), config);
            LOGGER.debug("Added clientTierConfigCache :: {}", config);
        }
    }

    private void updateCurrencyConfig(MessageHeaderDecoder headerDecoder) {
        currencyDecoder.wrapAndApplyHeader(buffer, 0, headerDecoder);
        if (currencyCache.containsKey(currencyDecoder.id())) {
            CurrencyConfig config = currencyCache.get(currencyDecoder.id()).update(
                    currencyDecoder.symbol(),
                    currencyDecoder.spotPrecision(),
                    currencyDecoder.forwardPrecision(),
                    currencyDecoder.amountPrecision()
            );
            LOGGER.debug("Updated currencyCache :: {}", config);
        } else {
            CurrencyConfig config = currencyConfigArrayObjectPool.get().init(currencyDecoder.id(),
                    currencyDecoder.symbol(),
                    currencyDecoder.spotPrecision(),
                    currencyDecoder.forwardPrecision(),
                    currencyDecoder.amountPrecision()
            );
            currencyCache.put(config.id(), config);
            LOGGER.debug("Added currencyCache :: {}", config);
        }
    }

    @Override
    public void onClose() {
        subscription.close();
    }
}
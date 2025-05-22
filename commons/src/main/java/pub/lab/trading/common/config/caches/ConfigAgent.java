package pub.lab.trading.common.config.caches;

import io.aeron.Aeron;
import io.aeron.Subscription;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.model.sbe.ClientTierConfigMessageDecoder;
import play.lab.model.sbe.ConfigLoadCompleteMessageDecoder;
import play.lab.model.sbe.CurrencyConfigMessageDecoder;
import play.lab.model.sbe.MessageHeaderDecoder;
import pub.lab.trading.common.config.StreamId;
import pub.lab.trading.common.lifecycle.Worker;
import pub.lab.trading.common.model.ClientTierLevel;

import static pub.lab.trading.common.config.AeronConfigs.CONFIG_CHANNEL;

public class ConfigAgent implements Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigAgent.class);

    private final Subscription subscription;
    private final CurrencyConfigMessageDecoder currencyDecoder;
    private final ClientTierConfigMessageDecoder clientTierDecoder;
    private final ConfigLoadCompleteMessageDecoder completeDecoder;
    private final UnsafeBuffer buffer;

    // caches
    private final CurrencyConfigCache currencyConfigCache = new CurrencyConfigCache();
    private final ClientTierConfigCache clientTierConfigCache = new ClientTierConfigCache();

    private volatile boolean isInitialLoadComplete;

    public ConfigAgent(Aeron aeron) {
        this.subscription = aeron.addSubscription(CONFIG_CHANNEL, StreamId.CONFIG_STREAM.getCode());
        this.currencyDecoder = new CurrencyConfigMessageDecoder();
        this.clientTierDecoder = new ClientTierConfigMessageDecoder();
        this.completeDecoder = new ConfigLoadCompleteMessageDecoder();
        this.buffer = new UnsafeBuffer(new byte[256]);
        this.isInitialLoadComplete = false;

        defaultLoad();
    }

    private void defaultLoad() {
        ClientTierConfig clientTierConfig = new ClientTierConfig();
        short defaultValue = 1;
        clientTierConfig.init(
                ClientTierLevel.GOLD.getId(),
                clientTierConfig.tierName(),
                1.0,
                1.0,
                1L,
                1L,
                1L,
                1.0,
                1.0,
                defaultValue,
                true,
                true,
                true,
                500_000_000,
                defaultValue,
                1.0,
                1.0,
                1.0
        );
        clientTierConfigCache.update(ClientTierLevel.GOLD, clientTierConfig);

        clientTierConfig.init(
                ClientTierLevel.SILVER.getId(),
                clientTierConfig.tierName(),
                1.5,
                1.5,
                1L,
                1L,
                1L,
                1.0,
                1.0,
                defaultValue,
                true,
                true,
                true,
                500_000_000,
                defaultValue,
                1.5,
                1.5,
                1.5
        );
        clientTierConfigCache.update(ClientTierLevel.SILVER, clientTierConfig);

    }

    @Override
    public String roleName() {
        return "ConfigProcessor";
    }

    public CurrencyConfigCache getCurrencyConfigCache() {
        return currencyConfigCache;
    }

    public ClientTierConfigCache getClientTierConfigCache() {
        return clientTierConfigCache;
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
                    LOGGER.info("Initial config load complete at timestamp: {}", timestamp);
                    isInitialLoadComplete = true;
                }
            } else {
                LOGGER.warn("Unknown config message templateId: {}", templateId);
            }
        }, 10);
    }

    private void updateClientTierConfig(MessageHeaderDecoder headerDecoder) {
        clientTierDecoder.wrapAndApplyHeader(buffer, 0, headerDecoder);
        clientTierConfigCache.update(clientTierDecoder);
    }

    private void updateCurrencyConfig(MessageHeaderDecoder headerDecoder) {
        currencyDecoder.wrapAndApplyHeader(buffer, 0, headerDecoder);
        currencyConfigCache.update(currencyDecoder);
    }

    @Override
    public void onClose() {
        subscription.close();
    }
}
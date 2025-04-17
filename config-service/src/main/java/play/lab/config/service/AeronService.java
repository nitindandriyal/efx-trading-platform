package play.lab.config.service;

import io.aeron.Aeron;
import io.aeron.FragmentAssembler;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;
import org.agrona.BufferUtil;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.NoOpIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import pub.lab.trading.AeronConfigs;
import pub.lab.trading.StreamId;
import pub.lab.trading.model.config.ClientTierFlyweight;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static pub.lab.trading.AeronConfigs.CONTROL_REQUEST_CHANNEL;
import static pub.lab.trading.AeronConfigs.CONTROL_RESPONSE_CHANNEL;

public class AeronService {

    private final List<ClientTierFlyweight> cache = new ArrayList<>();
    private final Aeron aeron;
    private final AeronArchive archive;
    private final long publicationId;
    private final ClientTierFlyweight flyweight = new ClientTierFlyweight();

    public AeronService() {
        String aeronDir = System.getProperty("aeron.base.path") + AeronConfigs.LIVE_DIR;
        aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
        archive = AeronArchive.connect(
                new AeronArchive.Context()
                        .aeron(aeron)
                        .controlRequestChannel(CONTROL_REQUEST_CHANNEL)
                        .controlResponseChannel(CONTROL_RESPONSE_CHANNEL));
        publicationId = archive.startRecording(AeronConfigs.CONFIG_CHANNEL, StreamId.CONFIG_STREAM.getCode(), SourceLocation.LOCAL);
    }

    public void sendTier(int tierId, String tierName, double markupBps, double spreadTighteningFactor,
                         long quoteThrottleMs, long latencyProtectionMs, long quoteExpiryMs,
                         double minNotional, double maxNotional, byte pricePrecision,
                         boolean streamingEnabled, boolean limitOrderEnabled, boolean accessToCrosses,
                         double creditLimitUsd, byte tierPriority) {
        try (Publication publication = aeron.addPublication(AeronConfigs.CONFIG_CHANNEL, StreamId.CONFIG_STREAM.getCode())) {
            IdleStrategy idleStrategy = new NoOpIdleStrategy();
            MutableDirectBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(ClientTierFlyweight.messageSize()));

            flyweight.wrap(buffer, 0)
                    .initMessage()
                    .setTierId(tierId)
                    .setTierName(tierName)
                    .setMarkupBps(markupBps)
                    .setSpreadTighteningFactor(spreadTighteningFactor)
                    .setQuoteThrottleMs(quoteThrottleMs)
                    .setLatencyProtectionMs(latencyProtectionMs)
                    .setQuoteExpiryMs(quoteExpiryMs)
                    .setMinNotional(minNotional)
                    .setMaxNotional(maxNotional)
                    .setPricePrecision(pricePrecision)
                    .setStreamingEnabled(streamingEnabled)
                    .setLimitOrderEnabled(limitOrderEnabled)
                    .setAccessToCrosses(accessToCrosses)
                    .setCreditLimitUsd(creditLimitUsd)
                    .setTierPriority(tierPriority);

            while (publication.offer(buffer, 0, ClientTierFlyweight.messageSize()) < 0) {
                idleStrategy.idle();
            }

            synchronized (cache) {
                ClientTierFlyweight cachedFlyweight = new ClientTierFlyweight();
                MutableDirectBuffer cacheBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(ClientTierFlyweight.messageSize()));
                cacheBuffer.putBytes(0, buffer, 0, ClientTierFlyweight.messageSize());
                cachedFlyweight.wrap(cacheBuffer, 0);
                cache.add(cachedFlyweight);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send tier: " + e.getMessage(), e);
        }
    }

    public List<ClientTierFlyweight> replayTiers() {
        synchronized (cache) {
            cache.clear();
        }

        String replayChannel = "aeron:ipc?alias=tiers-replay";
        long recordingId = 0;
        IdleStrategy idleStrategy = new NoOpIdleStrategy();
        AtomicBoolean isConnected = new AtomicBoolean(true);

        try (Subscription subscription = archive.replay(
                recordingId, 0, Long.MAX_VALUE, replayChannel, StreamId.CONFIG_STREAM.getCode())) {
            FragmentAssembler fragmentAssembler = new FragmentAssembler(
                    (buffer, offset, length, header) -> {
                        try {
                            if (length != ClientTierFlyweight.messageSize()) {
                                System.err.println("Invalid message size: " + length);
                                return;
                            }
                            flyweight.wrap(buffer, offset);
                            flyweight.validate();

                            ClientTierFlyweight cachedFlyweight = new ClientTierFlyweight();
                            MutableDirectBuffer cacheBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(ClientTierFlyweight.messageSize()));
                            cacheBuffer.putBytes(0, buffer, offset, ClientTierFlyweight.messageSize());
                            cachedFlyweight.wrap(cacheBuffer, 0);

                            synchronized (cache) {
                                cache.add(cachedFlyweight);
                            }
                        } catch (Exception e) {
                            System.err.println("Error decoding tier: " + e.getMessage());
                        }
                    });

            while (isConnected.get()) {
                int fragments = subscription.poll(fragmentAssembler, 10);
                if (fragments == 0) {
                    idleStrategy.idle();
                }
                if (!subscription.isConnected()) {
                    isConnected.set(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Error replaying tiers: " + e.getMessage());
        }

        synchronized (cache) {
            return new ArrayList<>(cache);
        }
    }

    public List<ClientTierFlyweight> getCachedTiers() {
        synchronized (cache) {
            return new ArrayList<>(cache);
        }
    }

    public void shutdown() {
        try {
            archive.stopRecording(publicationId);
            archive.close();
            aeron.close();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
}
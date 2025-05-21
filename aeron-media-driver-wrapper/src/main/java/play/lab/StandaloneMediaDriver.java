package play.lab;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.lab.trading.common.config.AeronConfigs;

public class StandaloneMediaDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneMediaDriver.class);

    public static void main(String[] args) {

        MediaDriver.Context context = new MediaDriver.Context()
                .aeronDirectoryName(AeronConfigs.AERON_LIVE_DIR)
                .threadingMode(ThreadingMode.SHARED)
                .warnIfDirectoryExists(true)
                .termBufferSparseFile(true); // for Windows compatibility

        MediaDriver driver = MediaDriver.launch(context);
        LOGGER.info("🚀 Launching Aeron MediaDriver: {} ", context.aeronDirectory());

        LOGGER.info("📡 MediaDriver running. Ctrl+C to terminate...");
        new ShutdownSignalBarrier().await();

        LOGGER.info("🛑 Shutting down MediaDriver...");
        driver.close();

        LOGGER.info("✅ MediaDriver shutdown complete.");
    }
}

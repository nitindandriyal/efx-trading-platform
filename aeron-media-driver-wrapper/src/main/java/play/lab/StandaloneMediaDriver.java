package play.lab;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.SigIntBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class StandaloneMediaDriver {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneMediaDriver.class);
    public static void main(String[] args) {
        System.setProperty("aeron.event.logger", "true");
        System.setProperty("aeron.event.codes", "ALL");
        System.setProperty("aeron.event.log.console", "true");
        String aeronDir = System.getProperty("aeron.base.path") + "/trading";

        MediaDriver.Context context = new MediaDriver.Context()
                .aeronDirectoryName(aeronDir)
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.DEDICATED)
                .warnIfDirectoryExists(true)
                .termBufferSparseFile(true); // for Windows compatibility

        LOGGER.info("🚀 Launching Aeron MediaDriver: {} ", aeronDir);
        MediaDriver driver = MediaDriver.launch(context);

        LOGGER.info("📡 MediaDriver running. Ctrl+C to terminate...");
        new SigIntBarrier().await();

        LOGGER.info("🛑 Shutting down MediaDriver...");
        driver.close();

        // Optional: Delete the Aeron dir after exit
        File dir = new File(System.getProperty("java.io.tmpdir"), aeronDir);
        if (dir.exists()) {
            LOGGER.info("🧹 Cleaning up: " + dir.getAbsolutePath());
            deleteDir(dir);
        }

        LOGGER.info("✅ MediaDriver shutdown complete.");
    }

    private static void deleteDir(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File file : children) {
                if (file.isDirectory()) deleteDir(file);
                else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}

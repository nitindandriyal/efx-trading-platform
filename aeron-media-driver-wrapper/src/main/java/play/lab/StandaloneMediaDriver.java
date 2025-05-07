package play.lab;

import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.ArchivingMediaDriver;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.lab.trading.common.config.AeronConfigs;

import java.io.File;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class StandaloneMediaDriver {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneMediaDriver.class);

    public static void main(String[] args) {
        startMediaDriverWithArchive();
    }

    private static void startMediaDriverWithArchive() {
        String aeronDir = System.getProperty("aeron.base.path") + AeronConfigs.LIVE_DIR;
        String aeronArchiveDir = System.getProperty("aeron.base.path") + AeronConfigs.ARCHIVE_DIR;

        MediaDriver.Context mediaDriverContext = new MediaDriver.Context()
                .aeronDirectoryName(aeronDir)
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.DEDICATED)
                .warnIfDirectoryExists(true)
                .termBufferSparseFile(true); // for Windows compatibility

        Archive.Context archiveContext = new Archive.Context()
                .archiveDir(new File(aeronArchiveDir)) // Set directory for archive recordings
                .threadingMode(ArchiveThreadingMode.DEDICATED)
                .deleteArchiveOnStart(true) // Clean archive directory on start
                .controlChannel("aeron:udp?endpoint=localhost:18010") // IPC channel for archive control requests
                .replicationChannel("aeron:ipc?endpoint=archive-replication"); // IPC channel for archive replication

        LOGGER.info("🚀 Launching Aeron MediaDriver: {} ", aeronDir);
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        try (ArchivingMediaDriver archive = ArchivingMediaDriver.launch(mediaDriverContext, archiveContext)) {
            LOGGER.info("📡 MediaDriver running. Ctrl+C to terminate...");
            barrier.await();
            LOGGER.info("🛑 Shutting down MediaDriver...");

            archive.close();

            // Optional: Delete the Aeron dir after exit
            File dir = new File(System.getProperty("java.io.tmpdir"), aeronDir);
            if (dir.exists()) {
                LOGGER.info("🧹 Cleaning up: {}", dir.getAbsolutePath());
                deleteDir(dir);
            }

            LOGGER.info("✅ MediaDriver shutdown complete.");
        }
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

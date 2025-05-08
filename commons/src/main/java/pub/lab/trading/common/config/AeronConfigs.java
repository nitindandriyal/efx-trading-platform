package pub.lab.trading.common.config;

public class AeronConfigs {

    public static final String LIVE_DIR = "/live-data";
    public static final String ARCHIVE_DIR = "/archive-data";
    public static final String CONTROL_REQUEST_CHANNEL = "aeron:udp?endpoint=localhost:18010";
    public static final String CONTROL_RESPONSE_CHANNEL = "aeron:udp?endpoint=localhost:0";
    public static final String REPLICATION_CHANNEL = "aeron:ipc?endpoint=archive-replication";
    public static final String CONFIG_CHANNEL = "aeron:ipc?endpoint=config-service";

    private AeronConfigs() {
        // configs
    }

}
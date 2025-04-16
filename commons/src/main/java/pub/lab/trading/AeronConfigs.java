package pub.lab.trading;

public class AeronConfigs {

    private AeronConfigs() {
        // configs
    }

    public static final String LIVE_DIR = "/live-data";
    public static final String ARCHIVE_DIR = "/archive-data";
    public static final String AERON_UDP_CONTROL_REQUEST_CHANNEL = "aeron:udp?endpoint=localhost:18010";

}
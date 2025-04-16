package play.lab;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;

public class App {
    public static void main(String[] args) {
        String aeronDir = System.getProperty("aeron.base.path") + "/trading";
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
        Subscription quoteSub = aeron.addSubscription("aeron:ipc", 10);
        Subscription hbSub = aeron.addSubscription("aeron:ipc", 20);
        Publication hbPub = aeron.addExclusivePublication("aeron:ipc", 20);

        AeronIpcPoller poller = new AeronIpcPoller(quoteSub, hbSub, hbPub, 42, 10_000);
        poller.init();

        // Close Aeron resources
        hbPub.close();
        hbSub.close();
        quoteSub.close();
        aeron.close();
    }
}

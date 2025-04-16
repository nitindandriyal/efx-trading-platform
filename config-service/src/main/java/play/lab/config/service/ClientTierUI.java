package play.lab.config.service;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;
import org.agrona.concurrent.UnsafeBuffer;
import pub.lab.trading.AeronConfigs;
import pub.lab.trading.StreamId;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static pub.lab.trading.AeronConfigs.AERON_UDP_CONTROL_REQUEST_CHANNEL;

@Route("")
public class ClientTierUI extends VerticalLayout {

    private final Grid<String> grid = new Grid<>();
    private final List<String> cache = new ArrayList<>();
    private Publication publication;

    public ClientTierUI() {
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);

        TextField name = new TextField("Tier Name");
        TextField markup = new TextField("Markup (bps)");
        TextField tighten = new TextField("Tightening Factor");
        Checkbox stream = new Checkbox("Streaming Enabled");

        Button add = new Button("Add Tier", e -> {
            String record = name.getValue() + "," + markup.getValue() + "," + tighten.getValue() + "," + stream.getValue();
            cache.add(record);
            publishToArchive(record);
            refreshGrid();
            Notification.show("Tier added and archived.");
        });

        HorizontalLayout form = new HorizontalLayout(name, markup, tighten, stream, add);
        add(new H2("Client Tier Manager"), form, grid);

        setupArchive();
        replayArchive();
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(cache);
        grid.addColumn(r -> r).setHeader("Client Tier Records");
    }

    private void setupArchive() {
        String aeronDir = System.getProperty("aeron.base.path") + AeronConfigs.LIVE_DIR;

        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
        AeronArchive archive = AeronArchive.connect(
                new AeronArchive.Context().aeron(aeron)
                        .controlRequestChannel(AERON_UDP_CONTROL_REQUEST_CHANNEL)
                        .controlResponseChannel("aeron:udp?endpoint=localhost:0")
        );
        publication = aeron.addPublication("aeron:ipc", StreamId.CONFIG_STREAM.getCode());
        archive.startRecording("aeron:ipc", 1001, SourceLocation.LOCAL);
    }

    private void publishToArchive(String data) {
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(256));
        buffer.putBytes(0, data.getBytes());
        while (publication.offer(buffer, 0, data.length()) < 0) {
            // backpressure handling
        }
    }

    private void replayArchive() {
        // Simulated archive replay - In a real setup, you'd use AeronArchive.replay() with a subscription
        // For now, we're assuming cache is initially empty and only builds during app lifetime
    }
}

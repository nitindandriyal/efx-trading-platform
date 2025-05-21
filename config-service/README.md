# Config Service for FX Trading Platform

The `config-service` is a core component of
the [FX Trading Platform](https://github.com/nitindandriyal/fx-trading-platform), responsible for managing and
distributing configuration data for the platform's services, such as the pricing engine and order execution systems. It
provides a user-friendly **Vaadin GUI** for administrators to create, update, and delete configurations, stores these
configurations in **Aeron archives**, and ensures reliable delivery to other services via snapshots on startup and
real-time updates thereafter.

## Features

- **Vaadin GUI**: Intuitive web-based interface for managing configurations, including currency pairs (e.g., EUR/USD)
  and client tiers (e.g., GOLD, SILVER).
- **Aeron Archives**: Persists configuration data in Aeron archives for durability and fault tolerance.
- **Snapshot Replay**: On service startup, replays configuration snapshots to initialize services like the pricing
  engine.
- **Real-Time Updates**: Publishes configuration updates to services via Aeron's high-performance messaging.
- **SBE Messaging**: Uses Simple Binary Encoding (SBE) for efficient configuration messages, defined in
  `QuoteSchema.xml`.
- **Scalability**: Designed to handle high-frequency updates with low latency, suitable for FX trading environments.

## Architecture

The `config-service` integrates with the FX trading platform as follows:

1. **Configuration Management**:
    - Admins use the Vaadin GUI to define configurations, such as:
        - `CurrencyConfig`: Currency pair ID, symbol, spot/forward/amount precision.
        - `ClientTierConfig`: Tier ID, name, markup, spread, throttle settings, and more.
    - Configurations are validated and encoded into SBE messages (`CurrencyConfigMessage`, `ClientTierConfigMessage`).

2. **Storage**:
    - Configurations are saved in **Aeron archives**, ensuring durability and enabling replay for fault recovery.
    - Archives are stored locally or in a distributed setup, depending on configuration.

3. **Distribution**:
    - On startup, the service replays a snapshot of all configurations to subscribing services (e.g., `Pricer`) via
      Aeron channel `aeron:ipc?term-buffer-length=33554432` (stream ID: `2000`).
    - Real-time updates are published as new SBE messages when configurations change.
    - A `ConfigLoadCompleteMessage` signals the end of the initial snapshot.

4. **Integration**:
    - Consumes by services like `Pricer` (via `CurrencyConfigCache`), which subscribe to the config channel to
      initialize and update their caches.
    - Supports fault-tolerant design with Aeron’s replay mechanism.

## Prerequisites

- **Java**: 17 or higher
- **Maven**: 3.8.x
- **Aeron**: `io.aeron:aeron-all:1.42.0`
- **Vaadin**: `com.vaadin:vaadin:24.x` (for GUI)
- **Agrona**: `org.agrona:agrona:1.19.0` (for SBE and buffers)
- **SBE Tool**: `uk.co.real-logic:sbe-tool:1.28.0` (for message generation)
- **Docker** (optional): For running in a containerized environment

## Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/nitindandriyal/fx-trading-platform.git
   cd fx-trading-platform/config-service
   ```

2. **Generate SBE Classes**:
   The service uses SBE messages defined in `src/main/resources/QuoteSchema.xml`. Generate Java classes:
   ```bash
   java -jar sbe-all-1.28.0.jar src/main/resources/QuoteSchema.xml
   ```
   Ensure `sbe-all-1.28.0.jar` is in your local Maven repository or downloaded.

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Start Aeron Media Driver**:
   The service uses Aeron for messaging. Run the media driver:
   ```bash
   java -Daeron.counters.enabled=true -cp aeron-all-1.42.0.jar io.aeron.driver.MediaDriver
   ```
   Ensure `aeron-all-1.42.0.jar` is available (included via Maven).

5. **Run the Config Service**:
   ```bash
   java -jar target/config-service-1.0-SNAPSHOT.jar
   ```
   The Vaadin GUI will be accessible at `http://localhost:8080` (default port).

## Usage

1. **Access the Vaadin GUI**:
    - Open `http://localhost:8080` in a web browser.
    - Log in with admin credentials (default: `admin`/`password`, configurable in `application.properties`).

2. **Manage Configurations**:
    - **Currency Configurations**:
        - Add/edit currency pairs (e.g., `id=1`, `symbol=EUR/USD`, `spotPrecision=5`).
        - Fields are validated to ensure non-negative precisions and valid symbols.
    - **Client Tier Configurations**:
        - Define tiers (e.g., `tierId=1`, `tierName=GOLD`, `markupBps=20.0`).
        - Configure pricing parameters like `spreadTighteningFactor`, `quoteThrottleMs`, and `skew`.
    - Save changes to persist in Aeron archives and publish to services.

3. **Monitor Updates**:
    - Configuration changes are published to `aeron:ipc?term-buffer-length=33554432` (stream ID: `2000`).
    - Subscribing services (e.g., `Pricer`) receive updates in real-time.
    - Use Aeron’s archive replay tool to inspect archived configurations:
      ```bash
      java -cp aeron-all-1.42.0.jar io.aeron.archive.ArchiveTool list <archive-dir> <stream-id>
      ```

4. **Startup Snapshot**:
    - On startup, the service replays all configurations as a snapshot, followed by a `ConfigLoadCompleteMessage`.
    - Services wait for this message (via `CurrencyConfigCache#waitForInitialLoad`) before processing quotes.

## Configuration

The service is configured via `src/main/resources/application.properties`:

```properties
# Vaadin server
server.port=8080

# Aeron configuration
aeron.channel=aeron:ipc?term-buffer-length=33554432
aeron.stream.id=2000

# Archive directory
aeron.archive.dir=/path/to/archive

# Admin credentials
vaadin.admin.username=admin
vaadin.admin.password=password
```

- **Aeron Archive**: Set `aeron.archive.dir` to a persistent storage location.
- **Security**: Update `vaadin.admin.username` and `vaadin.admin.password` for production use.

## Integration with FX Trading Platform

The `config-service` is designed to work with other platform components:

- **Pricer**: Subscribes to configuration updates via `CurrencyConfigCache` (stream ID: `2000`). Configurations
  initialize currency and tier caches for quote transformations.
- **Order Execution**: Uses `ClientTierConfig` fields like `minNotional`, `maxNotional`, and `creditLimitUsd` for order
  validation.
- **Schema**: All services share `QuoteSchema.xml` for consistent SBE messaging.

To integrate:

1. Ensure all services use the same `QuoteSchema.xml` (version 6 or higher).
2. Configure services to subscribe to `aeron:ipc?term-buffer-length=33554432` (stream ID: `2000`).
3. Replay archived configurations during service initialization.

## Development

### Dependencies

- **Maven Dependencies** (see `pom.xml`):
    - `io.aeron:aeron-all:1.42.0`
    - `org.agrona:agrona:1.19.0`
    - `uk.co.real-logic:sbe-tool:1.28.0`
    - `com.vaadin:vaadin:24.x`

- **Build Configuration**:
    - The `pom.xml` includes the SBE plugin to generate message classes during the `generate-sources` phase.
    - Use the provided workaround (timestamp/checksum check) to skip SBE generation if `QuoteSchema.xml` is unchanged.

### Running Locally

1. Start the Aeron media driver (see Setup).
2. Run the service in development mode:
   ```bash
   mvn spring-boot:run
   ```
3. Access the GUI at `http://localhost:8080`.

### Testing

- **Unit Tests**:
    - Located in `src/test/java`.
    - Test configuration validation and SBE message encoding/decoding.
    - Run with:
      ```bash
      mvn test
      ```

- **Integration Tests**:
    - Simulate Aeron message publishing and verify snapshot replay.
    - Requires a running media driver and archive.

- **GUI Testing**:
    - Manually test the Vaadin interface or use automated tools like Selenium.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For questions or support, contact the maintainer:

- **Nitin Dandriyal**: [GitHub](https://github.com/nitindandriyal)

---
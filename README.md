# FX Trading Platform

A modular, real-time FX trading platform built with Java, Spring Boot, and Vaadin.  
It supports tiered pricing, latency smoothing, and customizable risk controls — ideal for simulating and deploying institutional-grade FX pricing engines.

---

## 🚀 Features

- **Tiered Pricing Engine** – Generate client-specific quotes based on credit limits, inventory, and business rules.
- **Latency Smoothing** – Protect against latency arbitrage by smoothing quote updates.
- **Quote Throttling** – Control the frequency of quote updates to manage system load.
- **Vaadin UI** – Interactive frontend for monitoring and adjusting pricing parameters.
- **Spring Boot Backend** – Robust backend services for market data ingestion and pricing logic.
- **Extensible Architecture** – Modular design for integrating OMS, analytics, and execution.

---

## 🧱 Architecture

The platform is structured into the following Maven modules:

- `market-data` – Handles market data feed ingestion and preprocessing.
- `pricing-engine` – Core logic for quote construction and tiered pricing.
- `ui` – Vaadin-based frontend for user interaction and simulation control.
- `common` – Shared utilities, config models, and data objects.

---

## 🛠️ Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6+

### Build & Run

```bash
# Clone the repo
git clone https://github.com/nitindandriyal/efx-trading-platform.git
cd efx-trading-platform

# Build the entire project
mvn clean install

# Run the UI module (or root Spring Boot launcher if defined)
cd market-data
mvn spring-boot:run
![image](https://github.com/user-attachments/assets/7c120d5c-aa26-4af2-9a86-fd64c9d24d90)


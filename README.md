eFX Trading Platform
A modular, real-time FX trading platform built with Java, Spring Boot, and Vaadin. It supports tiered pricing, latency smoothing, and customizable risk controls. Ideal for simulating and deploying institutional-grade FX pricing engines.​
GitHub
GitHub

🚀 Features
Tiered Pricing Engine: Generate client-specific quotes based on credit limits, inventory, and business rules.

Latency Smoothing: Protect against latency arbitrage by smoothing quote updates.

Quote Throttling: Control the frequency of quote updates to manage system load.

Vaadin UI: Interactive frontend for monitoring and adjusting pricing parameters.

Spring Boot Backend: Robust backend services for market data ingestion and pricing logic.

Extensible Architecture: Easily integrate additional modules like order management or analytics.​

🧱 Architecture
The platform is structured into the following modules:​

market-data: Handles market data feeds and preprocessing.

pricing-engine: Core logic for generating and adjusting FX quotes.

ui: Vaadin-based frontend for user interaction.

common: Shared utilities and configurations.​
GitHub
+2
YouTube
+2
GitHub
+2
GitHub

🛠️ Getting Started
Prerequisites
Java 17 or higher

Maven 3.6+​

Build and Run
Clone the repository:​

bash
Copy
Edit
git clone https://github.com/nitindandriyal/efx-trading-platform.git
cd efx-trading-platform
Build the project:​

bash
Copy
Edit
mvn clean install
Run the application:​

bash
Copy
Edit
mvn spring-boot:run
Access the UI at:​
GitHub
+6
GitHub
+6
GitHub
+6

arduino
Copy
Edit
http://localhost:8080
⚙️ Configuration
Adjust application settings in src/main/resources/application.properties:​

properties
Copy
Edit
# Server port
server.port=8080

# Logging level
logging.level.root=INFO
For advanced configurations, refer to the Spring Boot and Vaadin documentation.​

📂 Project Structure
plaintext
Copy
Edit
efx-trading-platform/
├── market-data/
├── pricing-engine/
├── ui/
├── common/
├── pom.xml
└── README.md
🤝 Contributing
Contributions are welcome! Please fork the repository and submit a pull request.​

📄 License
This project is licensed under the MIT License. See the LICENSE file for details.

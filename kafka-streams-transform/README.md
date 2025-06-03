# Kafka Streams Example: Basic Transformation

This is a minimal Kafka Streams application that reads from `example-topic`, transforms each message value to uppercase, and writes the result to `example-topic-transformed`.

## Prerequisites
- Java 8 or higher
- Maven
- Running Kafka cluster (see `docker-compose.yaml` in this repo)

## How to Run

1. Start Kafka using Docker Compose (from the root of this repo):
   ```sh
   docker-compose up -d
   ```

2. Build the Kafka Streams app:
   ```sh
   cd kafka-streams-transform
   mvn clean package
   ```

3. Run the app:
   ```sh
   mvn exec:java -Dexec.mainClass="com.example.BasicTransformApp"
   ```

4. Produce messages to `example-topic` (see `producer.kafka`).

5. Consume from `example-topic-transformed` to see the transformed data.

---

You can modify the transformation logic in `BasicTransformApp.java` as needed.


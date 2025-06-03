package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Properties;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.kstream.ValueTransformerWithKeySupplier;
import org.apache.kafka.streams.processor.ProcessorContext;

public class BasicTransformApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "basic-transform-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> source = builder.stream("example-topic");

        // wrap the value in a JSON object
        KStream<String, String> wrapped = source.transformValues(
            (ValueTransformerWithKeySupplier<String, String, String>) () -> new ValueTransformerWithKey<String, String, String>() {
                private ProcessorContext context;
                private final ObjectMapper mapper = new ObjectMapper();

                @Override
                public void init(ProcessorContext context) {
                    this.context = context;
                }

                @Override
                public String transform(String readOnlyKey, String value) {
                    try {
                        JsonNode payload = mapper.readTree(value);
                        ObjectNode envelope = mapper.createObjectNode();

                        envelope.put("ts", Instant.now().toEpochMilli());
                        envelope.put("v", 2);
                        envelope.put("op", "i");
                        envelope.put("ns", "kafka.raw-events");
                        envelope.set("o", payload);

                        // Add headers to the envelope
                        ObjectNode headersNode = mapper.createObjectNode();
                        Headers headers = context.headers();
                        headers.forEach(header -> headersNode.put(header.key(), new String(header.value())));
                        envelope.set("headers", headersNode);

                        // print the headers for debugging
                        headers.forEach(header -> System.out.println("Header: " + header.key() + " = " + new String(header.value())));

                        ObjectNode meta = mapper.createObjectNode();
                        meta.put("source", "kafka");
                        envelope.set("meta", meta);

                        return mapper.writeValueAsString(envelope);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void close() {}
            }
        );

        // write the transformed stream to stdout
        // transformed.foreach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));

        wrapped.to("example-topic-transformed", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}

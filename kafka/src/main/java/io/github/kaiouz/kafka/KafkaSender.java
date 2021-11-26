package io.github.kaiouz.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class KafkaSender<K, V> implements Closeable {

    private final KafkaProducer<K, V> kafkaProducer;

    private KafkaSender(Map<String, Object> configs) {
        kafkaProducer = new KafkaProducer<>(configs);
    }

    public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
        return kafkaProducer.send(record);
    }

    public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
        return kafkaProducer.send(record, callback);
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }

    public static <K, V> Builder<K, V> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<K, V> {
        private final Map<String, Object> config = new HashMap<>();

        public Builder<K, V> bootstrapServers(List<String> servers) {
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", servers));
            return this;
        }

        public Builder<K, V> keySerializer(String serializer) {
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer);
            return this;
        }

        public Builder<K, V> keySerializer(Class<? extends Serializer<K>> serializer) {
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer);
            return this;
        }

        public Builder<K, V> valueSerializer(String serializer) {
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);
            return this;
        }

        public Builder<K, V> valueSerializer(Class<? extends Serializer<V>> serializer) {
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);
            return this;
        }

        public Builder<K, V> acks(String ack) {
            config.put(ProducerConfig.ACKS_CONFIG, ack);
            return this;
        }

        public Builder<K, V> bufferMemory(long size) {
            config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, size);
            return this;
        }

        public Builder<K, V> compressionType(String type) {
            config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, type);
            return this;
        }

        public Builder<K, V> retries(int times) {
            config.put(ProducerConfig.RETRIES_CONFIG, times);
            return this;
        }

        public Builder<K, V> batchSize(int size) {
            config.put(ProducerConfig.BATCH_SIZE_CONFIG, size);
            return this;
        }

        public Builder<K, V> lingerMs(long ms) {
            config.put(ProducerConfig.LINGER_MS_CONFIG, ms);
            return this;
        }

        public Builder<K, V> clientId(String clientId) {
            config.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            return this;
        }

        public Builder<K, V> maxBlockMs(long ms) {
            config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, ms);
            return this;
        }

        public Builder<K, V> maxRequestSize(int size) {
            config.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, size);
            return this;
        }

        public Builder<K, V> maxInFlightRequestsPerConnection(int size) {
            config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, size);
            return this;
        }

        public Builder<K, V> requestTimeoutMs(int ms) {
            config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, ms);
            return this;
        }

        public Builder<K, V> partitioner(String partitioner) {
            config.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, partitioner);
            return this;
        }

        public Builder<K, V> partitioner(Class<? extends Partitioner> partitioner) {
            config.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, partitioner);
            return this;
        }

        public KafkaSender<K, V> build() {
            return new KafkaSender<>(config);
        }
    }
}

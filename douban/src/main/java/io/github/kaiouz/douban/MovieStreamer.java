package io.github.kaiouz.douban;

import com.google.gson.Gson;
import io.github.kaiouz.kafka.KafkaSender;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MovieStreamer implements Closeable {

    private final KafkaSender<String, Movie> kafkaSender;

    public static final String MOVIE_TOPIC = "movies";

    public MovieStreamer(List<String> brokers) {
        kafkaSender = KafkaSender.<String, Movie>newBuilder()
                .bootstrapServers(brokers)
                .clientId("movie_sender")
                .acks("1")
                .retries(3)
                .keySerializer(StringSerializer.class)
                .valueSerializer(JsonSerializer.class)
                .build();
    }

    public void send(Movie movie) {
        kafkaSender.send(new ProducerRecord<>(MOVIE_TOPIC, movie.getMovieId(), movie), (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void close() {
        kafkaSender.close();
    }

    public static class JsonSerializer implements Serializer<Movie> {

        private final Gson gson = new Gson();

        @Override
        public byte[] serialize(String topic, Movie data) {
            return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
        }

    }
}

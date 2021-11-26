package io.github.kaiouz.douban;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;

public class MovieStreamerTest {

    @Test
    public void sendTest() throws IOException {
        try (MovieStreamer streamer = new MovieStreamer(ImmutableList.of("192.168.15.6:9092"));
             MovieLoader movieLoader = new MovieLoader("/Users/zoukai/Downloads/moviedata-10m/movies.csv")) {

            Movie movie = movieLoader.next();

            streamer.send(movie);
        }
    }

}

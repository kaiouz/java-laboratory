package io.github.kaiouz.douban;

import org.junit.Test;

import java.io.IOException;

public class MovieLoaderTest {


    @Test
    public void loadMovie() throws IOException {
        MovieLoader loader = new MovieLoader("/Users/zoukai/Downloads/moviedata-10m/movies.csv");

        Movie movie = loader.next();
        while (movie != null) {
            System.out.println(movie);
            movie = null;
        }
    }


}

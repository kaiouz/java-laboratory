package io.github.kaiouz.douban;

import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Collections;

public class MovieRepositoryTest {


    @Test
    public void saveMovie() throws IOException {
        MovieRepository movieRepository = new MovieRepository();
        try (Jedis jedis = new Jedis("127.0.0.1", 6379);
             MovieLoader movieLoader = new MovieLoader("/Users/zoukai/Downloads/moviedata-10m/movies.csv")) {
            movieRepository.saveMovie(jedis, movieLoader.next());
        }
    }

    @Test
    public void searchMovie() {
        MovieRepository movieRepository = new MovieRepository();
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {

            Pageable pageable = new Pageable(1, 10);

            while (pageable != null) {
                Pagination<Movie> movies = movieRepository.search(jedis,
                        Collections.singletonList(Collections.singleton("死亡")),
                        Collections.emptyList(), pageable);
                for (Movie movie : movies.getData()) {
                    System.out.println(movie);
                }
                pageable = movies.hasNextPage() ? movies.nextPage() : null;
            }

        }
    }

}

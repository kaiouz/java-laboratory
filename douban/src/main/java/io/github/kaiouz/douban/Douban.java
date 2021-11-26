package io.github.kaiouz.douban;

import com.google.common.collect.ImmutableList;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class Douban implements Closeable {

    private final JedisPool jedisPool;
    private final MovieRepository movieRepository;
    private final MovieStreamer movieStreamer;

    public Douban() {
        jedisPool = new JedisPool();
        movieRepository = new MovieRepository();
        movieStreamer = new MovieStreamer(ImmutableList.of("127.0.0.1:9092", "127.0.0.1:9093"));
    }

    @Override
    public void close() {
        movieStreamer.close();
        jedisPool.close();
    }

    private void loadMovies(String path) throws IOException {
        try (Jedis jedis = jedisPool.getResource();
             MovieLoader movieLoader = new MovieLoader(path)) {

            Movie movie = movieLoader.next();
            int count = 0;
            while (movie != null) {
                movieRepository.saveMovie(jedis, movie);
                count++;
                System.out.print("\r完成: " + count);
                movie = movieLoader.next();
            }
        }
    }

    private Pagination<Movie> searchMovies(String pattern, Pageable pageable) {
        List<Set<String>> intersect = new ArrayList<>();
        List<String> difference = new ArrayList<>();
        for (String item : pattern.split("=")) {
            if (item.startsWith("|")) {
                intersect.get(intersect.size() - 1).add(item.substring(1));
            } else if (item.startsWith("-")) {
                difference.add(item.substring(1));
            } else {
                intersect.add(new HashSet<>());
                intersect.get(intersect.size() - 1).add(item);
            }
        }

        try (Jedis jedis = jedisPool.getResource()) {
            Pagination<Movie> pagination = movieRepository.search(jedis, intersect, difference, pageable);
            for (Movie movie : pagination.getData()) {
                System.out.println(movie);
            }
            System.out.printf("总页数: %d, 页码: %d, 显示数量: %d\n", pagination.getTotalPage(), pagination.getPage(), pagination.getSize());
            return pagination;
        }
    }

    public static void main(String[] args) {
        try (Douban douban = new Douban()) {

            Scanner scanner = new Scanner(System.in);
            for (; ; ) {

                try {
                    System.out.print("douban >");
                    String cmd = scanner.next();
                    switch (cmd) {
                        case "load":
                            String path = scanner.next();
                            douban.loadMovies(path);
                            break;
                        case "search":
                            String pattern = scanner.next();
                            int page = scanner.nextInt();
                            int size = scanner.nextInt();
                            Pagination<Movie> pagination = douban.searchMovies(pattern, new Pageable(page, size));

                            boolean search = true;
                            while (search) {
                                System.out.print("search >");
                                String searchCmd = scanner.next();
                                switch (searchCmd) {
                                    case "next":
                                        pagination = douban.searchMovies(pattern, pagination.nextPage());
                                        break;
                                    case "prev":
                                        pagination = douban.searchMovies(pattern, pagination.prePage());
                                        break;
                                    case "q":
                                        search = false;
                                        break;
                                }
                            }
                            break;
                        case "exit":
                            System.exit(0);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

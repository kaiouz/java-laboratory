package io.github.kaiouz.douban;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.SortingParams;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MovieRepository {

    private static final String MOVIE_KEY_FORMAT = "mov:%s";
    private static final String INDEX_KEY_FORMAT = "idx:%s";
    private static final String SEARCH_KEY_FORMAT = "srch:%s";

    private static String movieKey(String movieId) {
        return String.format(MOVIE_KEY_FORMAT, movieId);
    }

    private static String indexKey(String token) {
        return String.format(INDEX_KEY_FORMAT, token);
    }

    public void saveMovies(Jedis jedis, Iterable<Movie> movies) {
        for (Movie movie : movies) {
            saveMovie(jedis, movie);
        }
    }

    public void saveMovie(Jedis jedis, Movie movie) {
        Pipeline pipeline = jedis.pipelined();
        // 存储 movie hash
        pipeline.hset(movieKey(movie.getMovieId()), movieToMap(movie));

        // 电影名进行分词索引
        tokenize(movie.getName(), token -> {
            pipeline.sadd(indexKey(token), movie.getMovieId());
        });

        // 电影别名进行分词索引
        if (movie.getAlias() != null) {
            for (String alias : movie.getAlias()) {
                tokenize(alias, token -> {
                    pipeline.sadd(indexKey(token), movie.getMovieId());
                });
            }
        }

        // 电影简介进行分词索引
        if (!Strings.isNullOrEmpty(movie.getStoryline())) {
            tokenize(movie.getStoryline(), token -> {
                pipeline.sadd(indexKey(token), movie.getMovieId());
            });
        }

        // 电影演员进行索引
        if (movie.getActors() != null) {
            for (String actor : movie.getActors()) {
                // 名字超过5个字, 就分词索引
                if (actor.length() > 5) {
                    tokenize(actor, token -> {
                        pipeline.sadd(indexKey(token), movie.getMovieId());
                    });
                } else {
                    pipeline.sadd(indexKey(actor), movie.getMovieId());
                }
            }
        }

        // 电影导演进行索引
        if (movie.getDirectors() != null) {
            for (String director : movie.getDirectors()) {
                // 名字超过5个字, 就分词索引
                if (director.length() > 5) {
                    tokenize(director, token -> {
                        pipeline.sadd(indexKey(token), movie.getMovieId());
                    });
                } else {
                    pipeline.sadd(indexKey(director), movie.getMovieId());
                }
            }
        }

        // 电影分类进行索引
        if (movie.getGenres() != null) {
            for (String genre : movie.getGenres()) {
                pipeline.sadd(indexKey(genre), movie.getMovieId());
            }
        }

        // 电影标签进行索引
        if (movie.getTags() != null) {
            for (String tag : movie.getTags()) {
                if (tag.length() > 5) {
                    tokenize(tag, token -> {
                        pipeline.sadd(indexKey(token), movie.getMovieId());
                    });
                } else {
                    pipeline.sadd(indexKey(tag), movie.getMovieId());
                }
            }
        }

        pipeline.sync();
    }

    public Pagination<Movie> search(Jedis jedis, List<Set<String>> and, List<String> difference, Pageable pageable) {
        StringBuilder sb = new StringBuilder();
        for (Set<String> set : and) {
            sb.append('+');
            for (String s : set) {
                sb.append('|').append(s);
            }
        }
        for (String dff : difference) {
            sb.append('-');
            sb.append(dff);
        }

        String sortResultKey = String.format(SEARCH_KEY_FORMAT, sb.toString().hashCode());

        Long exist = jedis.expire(sortResultKey, 300);
        Pipeline pipeline = jedis.pipelined();
        if (exist != 1) {
            // 先求并集
            List<String> intersect = new ArrayList<>(and.size());
            for (Set<String> set : and) {
                if (set.size() == 1) {
                    intersect.add(String.format(INDEX_KEY_FORMAT, set.iterator().next()));
                } else {
                    String key = String.format(SEARCH_KEY_FORMAT, searchUUID());
                    pipeline.sunionstore(key, set.toArray(new String[0]));
                    pipeline.expire(key, 30);
                    intersect.add(key);
                }
            }

            String resultKey;
            // 再求交集
            if (intersect.size() == 1) {
                resultKey = intersect.get(0);
            } else {
                String key = String.format(SEARCH_KEY_FORMAT, searchUUID());
                pipeline.sinterstore(key, intersect.toArray(new String[0]));
                pipeline.expire(key, 30);
                resultKey = key;
            }

            // 去除多余的结果
            if (!difference.isEmpty()) {
                String key = String.format(SEARCH_KEY_FORMAT, searchUUID());
                List<String> list = new ArrayList<>(difference);
                list.add(resultKey);
                list.addAll(difference);
                pipeline.sdiffstore(key, list.toArray(new String[0]));
                pipeline.expire(key, 30);
                resultKey = key;
            }

            SortingParams params = new SortingParams()
                    .by(String.format(MOVIE_KEY_FORMAT + "->score", "*"))
                    .desc();
            pipeline.sort(resultKey, params, sortResultKey);
            pipeline.expire(sortResultKey, 300);
        }

        Response<Long> totalRes = pipeline.llen(sortResultKey);
        Response<List<String>> movieIdsRes = pipeline.lrange(sortResultKey, pageable.getStart(), pageable.getEnd());
        pipeline.sync();

        List<String> movieIds = movieIdsRes.get();

        List<Movie> movieList = Collections.emptyList();

        if (!movieIds.isEmpty()) {
            // 获取电影信息
            pipeline = jedis.pipelined();
            for (String movieId : movieIds) {
                pipeline.hgetAll(String.format(MOVIE_KEY_FORMAT, movieId));
            }
            List<Object> movies = pipeline.syncAndReturnAll();
            movieList = movies.stream().map(it -> mapToMovie((Map<String, String>) it)).collect(Collectors.toList());
        }

        return new Pagination<>(pageable.getPage(), pageable.getSize(), totalRes.get().intValue(), movieList);
    }

    private String searchUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void tokenize(String text, Consumer<String> consumer) {
        IndexTokenizer.segment(text).stream()
                .filter(term -> term.word.length() > 1 && term.nature.firstChar() == 'n')
                .map(it -> it.word)
                .forEach(consumer);
    }

    private Map<String, String> movieToMap(Movie movie) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(16);
        map.put("moveId", movie.getMovieId());
        map.put("name", movie.getName());
        if (movie.getAlias() != null) {
            map.put("alias", array2String(movie.getAlias()));
        }
        if (movie.getActors() != null) {
            map.put("actors", array2String(movie.getActors()));
        }
        if (movie.getDirectors() != null) {
            map.put("directors", array2String(movie.getDirectors()));
        }
        map.put("score", Double.toString(movie.getScore()));
        map.put("votes", Integer.toString(movie.getVotes()));
        if (movie.getGenres() != null) {
            map.put("genres", array2String(movie.getGenres()));
        }
        if (movie.getLanguages() != null) {
            map.put("languages", array2String(movie.getLanguages()));
        }
        map.put("minutes", Integer.toString(movie.getMinutes()));
        if (movie.getRegions() != null) {
            map.put("regions", array2String(movie.getRegions()));
        }
        if (movie.getReleaseDate() != null) {
            map.put("releaseDate", Long.toString(movie.getReleaseDate().toEpochDay()));
        }
        if (movie.getStoryline() != null) {
            map.put("storyline", movie.getStoryline());
        }
        if (movie.getTags() != null) {
            map.put("tags", array2String(movie.getTags()));
        }
        return map;
    }

    private Movie mapToMovie(Map<String, String> map) {
        Movie movie = new Movie();
        movie.setMovieId(map.get("moveId"));
        movie.setName(map.get("name"));

        if (map.containsKey("alias")) {
            movie.setAlias(string2Array(map.get("alias")));
        }
        if (map.containsKey("actors")) {
            movie.setAlias(string2Array(map.get("actors")));
        }
        if (map.containsKey("directors")) {
            movie.setDirectors(string2Array(map.get("directors")));
        }

        if (map.containsKey("genres")) {
            movie.setGenres(string2Array(map.get("genres")));
        }
        if (map.containsKey("languages")) {
            movie.setLanguages(string2Array(map.get("languages")));
        }
        if (map.containsKey("regions")) {
            movie.setRegions(string2Array(map.get("regions")));
        }
        if (map.containsKey("tags")) {
            movie.setTags(string2Array(map.get("tags")));
        }

        movie.setScore(Double.parseDouble(map.get("score")));
        movie.setVotes(Integer.parseInt(map.get("votes")));
        movie.setMinutes(Integer.parseInt(map.get("minutes")));
        movie.setStoryline(map.get("storyline"));

        if (map.containsKey("releaseDate")) {
            movie.setReleaseDate(LocalDate.ofEpochDay(Long.parseLong(map.get("releaseDate"))));
        }
        return movie;
    }

    private String array2String(String[] array) {
        return String.join(",", array);
    }

    private String[] string2Array(String text) {
        return text.split(",");
    }
}

package io.github.kaiouz.douban;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieLoader implements AutoCloseable {

    private final BufferedReader reader;

    public MovieLoader(String path) throws IOException {
        reader = Files.newBufferedReader(Paths.get(path));
        // 抛弃掉第一行
        reader.readLine();
    }

    public Movie next() throws IOException {
        String line = reader.readLine();
        return line == null ? null : parseMovie(line);
    }

    public List<Movie> next(int count) throws IOException {
        List<Movie> movies = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line != null) {
                movies.add(parseMovie(line));
            } else {
                break;
            }
        }

        return movies;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private Movie parseMovie(String text) {
        Movie movie = new Movie();

        String[] fields = text.split("(?<=\"),(?=\")");

        // id
        movie.setMovieId(trimQuote(fields[0]));
        // name
        movie.setName(trimQuote(fields[1]));
        // alias
        movie.setAlias(splitSlash(trimQuote(fields[2])));
        // actors
        movie.setActors(splitSlash(trimQuote(fields[3])));
        // directors
        movie.setDirectors(splitSlash(trimQuote(fields[5])));
        // score
        movie.setScore(parseDouble(trimQuote(fields[6])));
        // votes
        movie.setVotes((int) parseDouble(trimQuote(fields[7])));
        // genres
        movie.setGenres(splitSlash(trimQuote(fields[8])));
        // languages
        movie.setLanguages(splitSlash(trimQuote(fields[10])));
        // minutes
        movie.setMinutes(parseInt(trimQuote(fields[11])));
        // regions
        movie.setRegions(splitSlash(trimQuote(fields[13])));
        // release date
        movie.setReleaseDate(parseDate(trimQuote(fields[14])));
        // storyline
        movie.setStoryline(trimQuote(fields[16]));
        // tags
        movie.setTags(splitSlash(trimQuote(fields[17])));

        return movie;
    }

    private LocalDate parseDate(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return null;
        } else {
            return LocalDate.parse(text);
        }
    }

    private int parseInt(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return 0;
        } else {
            return Integer.parseInt(text);
        }
    }

    private double parseDouble(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return 0;
        } else {
            return Double.parseDouble(text);
        }
    }

    private String trimQuote(String text) {
        if (text.length() <= 2) {
            return null;
        }
        return text.substring(1, text.length() - 1);
    }

    private String[] splitSlash(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return null;
        } else {
            return text.split("\\s*/\\s*");
        }
    }


}

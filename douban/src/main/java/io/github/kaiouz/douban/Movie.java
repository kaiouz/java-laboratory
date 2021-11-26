package io.github.kaiouz.douban;

import java.time.LocalDate;
import java.util.Arrays;

public class Movie {
    private String movieId;
    private String name;
    private String[] alias;
    private String[] actors;
    private String[] directors;
    private double score;
    private int votes;
    private String[] genres;
    private String[] languages;
    private int minutes;
    private String[] regions;
    private LocalDate releaseDate;
    private String storyline;
    private String[] tags;
//    private String[] actorIds;
//    private String[] directorIds;


    public Movie() {
    }


    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public String[] getActors() {
        return actors;
    }

    public void setActors(String[] actors) {
        this.actors = actors;
    }

    public String[] getDirectors() {
        return directors;
    }

    public void setDirectors(String[] directors) {
        this.directors = directors;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String[] getRegions() {
        return regions;
    }

    public void setRegions(String[] regions) {
        this.regions = regions;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getStoryline() {
        return storyline;
    }

    public void setStoryline(String storyline) {
        this.storyline = storyline;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId='" + movieId + '\'' +
                ", name='" + name + '\'' +
                ", alias=" + Arrays.toString(alias) +
                ", actors=" + Arrays.toString(actors) +
                ", directors=" + Arrays.toString(directors) +
                ", score=" + score +
                ", votes=" + votes +
                ", genres=" + Arrays.toString(genres) +
                ", languages=" + Arrays.toString(languages) +
                ", minutes=" + minutes +
                ", regions=" + Arrays.toString(regions) +
                ", releaseDate=" + releaseDate +
                ", storyline='" + storyline + '\'' +
                ", tags=" + Arrays.toString(tags) +
                '}';
    }
}

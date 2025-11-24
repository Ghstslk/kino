package ru.test.spring_task.kino.dto;

public class FilmDto {
    private Long FilmId;
    private String FilmName;
    private Integer year;
    private Double rating;

    public FilmDto() {
    }

    public FilmDto(Long filmId, String filmName, Integer year, Double rating) {
        FilmId = filmId;
        FilmName = filmName;
        this.year = year;
        this.rating = rating;
    }

    public Long getFilmId() {
        return FilmId;
    }

    public void setFilmId(Long filmId) {
        FilmId = filmId;
    }

    public String getFilmName() {
        return FilmName;
    }

    public void setFilmName(String filmName) {
        FilmName = filmName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}

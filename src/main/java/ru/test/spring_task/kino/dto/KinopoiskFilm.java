package ru.test.spring_task.kino.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KinopoiskFilm {
    @JsonProperty("kinopoiskId")
    private Long kinopoiskId;

    @JsonProperty("nameRu")
    private String nameRu;

    @JsonProperty("year")
    private int year;

    @JsonProperty("ratingKinopoisk")
    private double ratingKinopoisk;

    // Геттеры и сеттеры
    public Long getKinopoiskId() {
        return kinopoiskId;
    }

    public void setKinopoiskId(Long kinopoiskId) {
        this.kinopoiskId = kinopoiskId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getRatingKinopoisk() {
        return ratingKinopoisk;
    }

    public void setRatingKinopoisk(double ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }
}

package ru.test.spring_task.kino.dto;

public class FilmSearchRequest {
    private String filmName;
    private Integer yearFrom;
    private Integer yearTo;
    private Double ratingFrom;
    private Double ratingTo;
    private int page = 0;
    private int size = 20;
    private String sortBy = "filmName";
    private String sortDirection = "asc";

    public String getFilmName() {
        return filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        this.yearFrom = yearFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public void setYearTo(Integer yearTo) {
        this.yearTo = yearTo;
    }

    public Double getRatingFrom() {
        return ratingFrom;
    }

    public void setRatingFrom(Double ratingFrom) {
        this.ratingFrom = ratingFrom;
    }

    public Double getRatingTo() {
        return ratingTo;
    }

    public void setRatingTo(Double ratingTo) {
        this.ratingTo = ratingTo;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}

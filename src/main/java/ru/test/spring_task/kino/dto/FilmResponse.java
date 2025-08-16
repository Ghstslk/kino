package ru.test.spring_task.kino.dto;

import ru.test.spring_task.kino.models.Film;

import java.util.List;

public class FilmResponse {
    private List<Film> films;
    private int currentPage;
    private int totalPages;
    private long totalItems;

    public FilmResponse(List<Film> films, int currentPage, int totalPages, long totalItems) {
        this.films = films;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    // Геттеры
    public List<Film> getFilms() {
        return films;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }
}

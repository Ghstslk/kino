package ru.test.spring_task.kino.dto;


import java.util.List;

public class KinopoiskResponse {
    private int total;
    private int totalPages;
    private List<KinopoiskFilm> items;


    // Геттеры и сеттеры
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<KinopoiskFilm> getItems() {
        return items;
    }

    public void setItems(List<KinopoiskFilm> items) {
        this.items = items;
    }
}


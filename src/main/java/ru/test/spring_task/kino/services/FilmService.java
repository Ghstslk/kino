package ru.test.spring_task.kino.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.test.spring_task.kino.dto.KinopoiskFilm;
import ru.test.spring_task.kino.dto.KinopoiskResponse;
import ru.test.spring_task.kino.models.Film;
import ru.test.spring_task.kino.repositories.FilmRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final RestTemplate restTemplate;
    private final FilmRepository filmRepository;

    @Value("${kinopoisk.api.url}")
    private String baseUrl;

    @Autowired
    public FilmService(RestTemplate restTemplate, FilmRepository filmRepository) {
        this.restTemplate = restTemplate;
        this.filmRepository = filmRepository;
    }

    public void fetchAndSaveFilms() {
        int page = 1;
        int maxPages = 20;
        Set<Long> allFilmIds = new HashSet<>();
        List<Film> allFilmsToSave = new ArrayList<>();

        try {
            while (page <= maxPages) {
                String apiUrl = baseUrl + "?page=" + page;
                System.out.println("Fetching page: " + page);

                ResponseEntity<KinopoiskResponse> response = restTemplate.getForEntity(
                        apiUrl,
                        KinopoiskResponse.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    KinopoiskResponse body = response.getBody();
                    List<KinopoiskFilm> items = body.getItems();

                    if (items != null && !items.isEmpty()) {
                        // Удаляем дубликаты в рамках текущей страницы
                        List<KinopoiskFilm> uniqueItems = removeDuplicates(items, KinopoiskFilm::getKinopoiskId);

                        // Проверяем уникальность в рамках всех страниц
                        List<KinopoiskFilm> newItems = uniqueItems.stream()
                                .filter(f -> !allFilmIds.contains(f.getKinopoiskId()))
                                .collect(Collectors.toList());

                        // Добавляем ID в общий набор
                        newItems.forEach(f -> allFilmIds.add(f.getKinopoiskId()));

                        // Конвертируем в сущности
                        List<Film> filmsToAdd = newItems.stream()
                                .map(this::convertToFilmEntity)
                                .collect(Collectors.toList());

                        allFilmsToSave.addAll(filmsToAdd);

                        System.out.println("Fetched " + newItems.size() + " unique films from page " + page);

                        // Проверяем, есть ли следующая страница
                        if (page >= body.getTotalPages() || items.size() < 20) {
                            break;
                        }
                    } else {
                        System.out.println("Page " + page + " is empty. Stopping.");
                        break;
                    }
                } else {
                    System.out.println("Failed to fetch page " + page + ". Status: " + response.getStatusCode());
                    break;
                }

                page++;

                // Добавляем задержку
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Сохраняем фильмы с проверкой на уникальность в базе
            if (!allFilmsToSave.isEmpty()) {
                saveFilmsWithDuplicateCheck(allFilmsToSave);
            } else {
                System.out.println("No new films to save");
            }

        } catch (Exception e) {
            System.err.println("Error fetching films: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Удаление дубликатов в списке по ключу
    private <T> List<T> removeDuplicates(List<T> list, Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return list.stream()
                .filter(item -> seen.add(keyExtractor.apply(item)))
                .collect(Collectors.toList());
    }

    // Сохранение с проверкой дубликатов в базе
    private void saveFilmsWithDuplicateCheck(List<Film> films) {
        // Собираем все filmId
        Set<Long> filmIds = films.stream()
                .map(Film::getFilmId)
                .collect(Collectors.toSet());

        // Получаем существующие ID из базы
        Set<Long> existingIds = filmRepository.findExistingFilmIds(filmIds);

        // Фильтруем только новые фильмы
        List<Film> newFilms = films.stream()
                .filter(f -> !existingIds.contains(f.getFilmId()))
                .collect(Collectors.toList());

        if (newFilms.isEmpty()) {
            System.out.println("All films already exist in database");
            return;
        }

        System.out.println("Saving " + newFilms.size() + " new films...");

        // Сохраняем небольшими пакетами
        int batchSize = 50;
        int totalSaved = 0;

        for (int i = 0; i < newFilms.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, newFilms.size());
            List<Film> batch = newFilms.subList(i, toIndex);

            try {
                filmRepository.saveAll(batch);
                totalSaved += batch.size();
                System.out.println("Saved batch: " + batch.size() + " films");
            } catch (DataIntegrityViolationException e) {
                // Если возникли дубликаты, сохраняем по одному
                System.out.println("Batch save failed, saving one by one...");
                saveOneByOne(batch);
                totalSaved += batch.size();
            }
        }

        System.out.println("Total saved: " + totalSaved + " films");
    }

    // Сохранение по одному фильму
    private void saveOneByOne(List<Film> films) {
        int saved = 0;
        int duplicates = 0;

        for (Film film : films) {
            try {
                filmRepository.save(film);
                saved++;
            } catch (DataIntegrityViolationException e) {
                System.out.println("Duplicate filmId skipped: " + film.getFilmId());
                duplicates++;
            }
        }

        System.out.println("Saved: " + saved + ", Duplicates skipped: " + duplicates);
    }

    private Film convertToFilmEntity(KinopoiskFilm apiFilm) {
        Film film = new Film();
        film.setFilmId(apiFilm.getKinopoiskId());
        film.setFilmName(apiFilm.getNameRu());
        film.setYear(apiFilm.getYear());
        film.setRating(apiFilm.getRatingKinopoisk());
        return film;
    }
}
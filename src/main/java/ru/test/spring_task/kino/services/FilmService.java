package ru.test.spring_task.kino.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.test.spring_task.kino.dto.*;
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
    private final FilmMapper filmMapper;


    @Value("${kinopoisk.api.url}")
    private String baseUrl;

    @Autowired
    public FilmService(RestTemplate restTemplate, FilmRepository filmRepository, FilmMapper filmMapper) {
        this.restTemplate = restTemplate;
        this.filmRepository = filmRepository;
        this.filmMapper = filmMapper;
    }

    public Page<FilmDto> searchFilms(FilmSearchRequest request) {
        Specification<Film> spec = FilmSpecifications.withFilters(request);

        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Film> filmsPage = filmRepository.findAll(spec, pageable);

        return filmsPage.map(filmMapper::toDto);
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

            // Сохраняем фильмы
            if (!allFilmsToSave.isEmpty()) {
                saveFilms(allFilmsToSave);
            } else {
                System.out.println("No films to save");
            }

        } catch (Exception e) {
            System.err.println("Error fetching films: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> List<T> removeDuplicates(List<T> list, Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return list.stream()
                .filter(item -> seen.add(keyExtractor.apply(item)))
                .collect(Collectors.toList());
    }

    private void saveFilms(List<Film> films) {
        int batchSize = 50;
        int totalSaved = 0;
        int duplicates = 0;

        for (int i = 0; i < films.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, films.size());
            List<Film> batch = films.subList(i, toIndex);

            try {
                // Проверяем существование фильмов
                Set<Long> filmIds = batch.stream()
                        .map(Film::getFilmId)
                        .collect(Collectors.toSet());

                Set<Long> existingIds = filmRepository.findExistingFilmIds(filmIds);

                // Фильтруем только новые фильмы
                List<Film> newFilms = batch.stream()
                        .filter(f -> !existingIds.contains(f.getFilmId()))
                        .collect(Collectors.toList());

                if (!newFilms.isEmpty()) {
                    filmRepository.saveAll(newFilms);
                    totalSaved += newFilms.size();
                    System.out.println("Saved batch of " + newFilms.size() + " films");
                }

                duplicates += (batch.size() - newFilms.size());

            } catch (Exception e) {
                System.err.println("Error saving batch: " + e.getMessage());
                // Сохраняем по одному при ошибке
                saveOneByOne(batch);
                totalSaved += batch.size();
            }
        }

        System.out.println("Total saved: " + totalSaved + " films, duplicates: " + duplicates);
    }

    private void saveOneByOne(List<Film> films) {
        int saved = 0;
        int duplicates = 0;

        for (Film film : films) {
            try {
                // Проверяем, существует ли фильм
                if (!filmRepository.existsByFilmId(film.getFilmId())) {
                    filmRepository.save(film);
                    saved++;
                } else {
                    duplicates++;
                }
            } catch (Exception e) {
                System.err.println("Failed to save film " + film.getFilmId() + ": " + e.getMessage());
            }
        }

        System.out.println("Saved: " + saved + ", Duplicates: " + duplicates);
    }

    public Film convertToFilmEntity(KinopoiskFilm apiFilm) {
        FilmDto filmDto = new FilmDto();
        filmDto.setFilmId(apiFilm.getKinopoiskId());
        filmDto.setFilmName(apiFilm.getNameRu());
        filmDto.setYear(apiFilm.getYear());
        filmDto.setRating(apiFilm.getRatingKinopoisk());

        return filmMapper.toEntity(filmDto);
    }
}
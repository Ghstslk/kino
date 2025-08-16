package ru.test.spring_task.kino.controllers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.test.spring_task.kino.dto.FilmResponse;
import ru.test.spring_task.kino.models.Film;
import ru.test.spring_task.kino.repositories.FilmRepository;
import ru.test.spring_task.kino.services.FilmService;
import ru.test.spring_task.kino.services.FilmSpecifications;

@RestController
@RequestMapping("/api/films")
public class FilmController {

    private final FilmService filmService;
    private final FilmRepository filmRepository;

    public FilmController(FilmService filmService, FilmRepository filmRepository) {
        this.filmService = filmService;
        this.filmRepository = filmRepository;
    }

    @GetMapping
    public ResponseEntity<FilmResponse> searchFilms(
            @RequestParam(value = "order", required = false, defaultValue = "RATING") String order,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "ratingFrom", required = false) Double ratingFrom,
            @RequestParam(value = "ratingTo", required = false) Double ratingTo,
            @RequestParam(value = "yearFrom", required = false) Integer yearFrom,
            @RequestParam(value = "yearTo", required = false) Integer yearTo,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        // Создаем спецификацию на основе фильтров
        Specification<Film> spec = FilmSpecifications.withFilters(
                type,
                ratingFrom,
                ratingTo,
                yearFrom,
                yearTo
        );

        // Определяем сортировку
        Sort sort = createSort(order);

        // Создаем объект пагинации (страницы начинаются с 1 в запросе, но в Pageable с 0)
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // Выполняем запрос с пагинацией и сортировкой
        Page<Film> pageResult = filmRepository.findAll(spec, pageable);

        // Создаем ответ
        FilmResponse response = new FilmResponse(
                pageResult.getContent(),
                page,
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    private Sort createSort(String order) {
        if (order == null || order.isBlank()) {
            return Sort.unsorted();
        }

        switch (order.toUpperCase()) {
            case "YEAR":
                return Sort.by(Sort.Direction.DESC, "year");
            case "FILM_NAME":
                return Sort.by(Sort.Direction.ASC, "filmName");
            case "ID":
                return Sort.by(Sort.Direction.ASC, "filmId");
            case "RATING":
            default:
                return Sort.by(Sort.Direction.DESC, "rating");
        }
    }

}
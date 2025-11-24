package ru.test.spring_task.kino.controllers;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.test.spring_task.kino.dto.FilmDto;
import ru.test.spring_task.kino.dto.FilmResponse;
import ru.test.spring_task.kino.dto.FilmSearchRequest;
import ru.test.spring_task.kino.services.FilmService;

@RestController
@RequestMapping("/api/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ResponseEntity<FilmResponse> searchFilms(@ModelAttribute FilmSearchRequest request) {
        Page<FilmDto> pageResult = filmService.searchFilms(request);

        FilmResponse response = new FilmResponse(
                pageResult.getContent(),
                request.getPage(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }
}
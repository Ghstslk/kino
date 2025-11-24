package ru.test.spring_task.kino;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.test.spring_task.kino.dto.*;
import ru.test.spring_task.kino.models.Film;
import ru.test.spring_task.kino.repositories.FilmRepository;
import ru.test.spring_task.kino.services.FilmService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private FilmMapper filmMapper;

    @InjectMocks
    private FilmService filmService;

    @Value("http://test-api.url")
    private String baseUrl;

    private KinopoiskResponse kinopoiskResponse;
    private List<KinopoiskFilm> kinopoiskFilms;
    private List<Film> films;

    @BeforeEach
    void setUp() {

        filmService = new FilmService(restTemplate, filmRepository, filmMapper);

        kinopoiskFilms = Arrays.asList(
                createKinopoiskFilm(1L, "Фильм 1", 2020, 8.5),
                createKinopoiskFilm(2L, "Фильм 2", 2021, 7.8),
                createKinopoiskFilm(2L, "Фильм 2", 2021, 7.8)
        );

        films = Arrays.asList(
                createFilm(1L, "Фильм 1", 2020, 8.5),
                createFilm(2L, "Фильм 2", 2021, 7.8),
                createFilm(2L, "Фильм 2", 2021, 7.8)
        );

        kinopoiskResponse = new KinopoiskResponse();
        kinopoiskResponse.setItems(kinopoiskFilms);
        kinopoiskResponse.setTotalPages(1);
    }

    private KinopoiskFilm createKinopoiskFilm(Long id, String name, Integer year, Double rating) {
        KinopoiskFilm film = new KinopoiskFilm();
        film.setKinopoiskId(id);
        film.setNameRu(name);
        film.setYear(year);
        film.setRatingKinopoisk(rating);
        return film;
    }

    private Film createFilm(Long filmId, String filmName, Integer year, Double rating) {
        Film film = new Film();
        film.setFilmId(filmId);
        film.setFilmName(filmName);
        film.setYear(year);
        film.setRating(rating);
        return film;
    }

    private FilmDto createFilmDto(Long id, String name, Integer year, Double rating) {
        FilmDto dto = new FilmDto();
        dto.setFilmId(id);
        dto.setFilmName(name);
        dto.setYear(year);
        dto.setRating(rating);
        return dto;
    }

    @Test
    void fetchAndSaveFilms_Success() {
        // Mock настройки
        when(restTemplate.getForEntity(anyString(), eq(KinopoiskResponse.class)))
                .thenReturn(ResponseEntity.ok(kinopoiskResponse));

        when(filmRepository.findExistingFilmIds(anySet())).thenReturn(new HashSet<>());
        when(filmRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Настройка маппера для конвертации
        when(filmMapper.toEntity(any(FilmDto.class))).thenAnswer(invocation -> {
            FilmDto dto = invocation.getArgument(0);
            Film film = new Film();
            film.setFilmId(dto.getFilmId());
            film.setFilmName(dto.getFilmName());
            film.setYear(dto.getYear());
            film.setRating(dto.getRating());
            return film;
        });

        // Выполнение тестируемого метода
        assertDoesNotThrow(() -> filmService.fetchAndSaveFilms());

        // Проверки
        verify(restTemplate, atLeastOnce()).getForEntity(anyString(), eq(KinopoiskResponse.class));
        verify(filmRepository, atLeastOnce()).findExistingFilmIds(anySet());
        verify(filmRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void searchFilms_WithFilters() {
        // Подготовка данных
        FilmSearchRequest request = new FilmSearchRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("filmName");
        request.setSortDirection("asc");

        Page<Film> filmPage = new PageImpl<>(films);
        Page<FilmDto> filmDtoPage = new PageImpl<>(Arrays.asList(
                createFilmDto(1L, "Фильм 1", 2020, 8.5),
                createFilmDto(2L, "Фильм 2", 2021, 7.8),
                createFilmDto(2L, "Фильм 2", 2021, 7.8)
        ));

        // Mock настройки
        when(filmRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filmPage);
        when(filmMapper.toDto(any(Film.class))).thenAnswer(invocation -> {
            Film film = invocation.getArgument(0);
            FilmDto dto = new FilmDto();
            dto.setFilmId(film.getFilmId());
            dto.setFilmName(film.getFilmName());
            dto.setYear(film.getYear());
            dto.setRating(film.getRating());
            return dto;
        });

        // Выполнение
        Page<FilmDto> result = filmService.searchFilms(request);

        // Проверки
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        verify(filmRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(filmMapper, times(3)).toDto(any(Film.class));
    }

    @Test
    void convertToFilmEntity_ValidData() {
        // Подготовка данных
        KinopoiskFilm kinopoiskFilm = createKinopoiskFilm(123L, "Тестовый фильм", 2022, 8.9);
        FilmDto filmDto = createFilmDto(123L, "Тест", 2022, 8.9);
        Film expectedFilm = createFilm(123L, "Тест", 2022, 8.9);

        // Mock настройки
        when(filmMapper.toEntity(any(FilmDto.class))).thenReturn(expectedFilm);

        // Выполнение
        Film result = filmService.convertToFilmEntity(kinopoiskFilm);

        // Проверки
        assertNotNull(result);
        assertEquals(123L, result.getFilmId());
        assertEquals("Тестовый фильм", result.getFilmName());
        assertEquals(2022, result.getYear());
        assertEquals(8.9, result.getRating());
    }
}
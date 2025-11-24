package ru.test.spring_task.kino;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.test.spring_task.kino.models.Film;
import ru.test.spring_task.kino.repositories.FilmRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class FilmIntegrationTest {

    @Autowired
    private FilmRepository filmRepository;

    @Test
    void saveAndFindFilm_WithH2Database_ShouldWorkCorrectly() {
        Film film = new Film();
        film.setFilmId(999999L);
        film.setFilmName("Тестовый фильм");
        film.setYear(2024);
        film.setRating(8.5);

        Film savedFilm = filmRepository.save(film);
        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());

        assertTrue(foundFilm.isPresent());
        assertEquals("Тестовый фильм", foundFilm.get().getFilmName());
        assertEquals(2024, foundFilm.get().getYear());
        assertEquals(8.5, foundFilm.get().getRating());
    }

    @Test
    void findExistingFilmIds_WithH2Database_ShouldReturnCorrectIds() {
        Film film1 = new Film();
        film1.setFilmId(100001L);
        film1.setFilmName("Фильм 1");
        filmRepository.save(film1);
        Film film2 = new Film();
        film2.setFilmId(100002L);
        film2.setFilmName("Фильм 2");
        filmRepository.save(film2);

        var existingIds = filmRepository.findExistingFilmIds(
                java.util.Set.of(100001L, 100002L, 100003L)
        );

        assertEquals(2, existingIds.size());
        assertTrue(existingIds.contains(100001L));
        assertTrue(existingIds.contains(100002L));
        assertFalse(existingIds.contains(100003L));
    }
}
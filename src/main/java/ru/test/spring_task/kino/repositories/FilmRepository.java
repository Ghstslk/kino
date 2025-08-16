package ru.test.spring_task.kino.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.test.spring_task.kino.models.Film;

import java.util.Set;

public interface FilmRepository extends JpaRepository<Film, Long>, JpaSpecificationExecutor<Film> {
    @Query("SELECT f.filmId FROM Film f WHERE f.filmId IN :filmIds")
    Set<Long> findExistingFilmIds(@Param("filmIds") Set<Long> filmIds);
}
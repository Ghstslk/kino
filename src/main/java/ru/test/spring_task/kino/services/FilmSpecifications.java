package ru.test.spring_task.kino.services;

import org.springframework.data.jpa.domain.Specification;
import ru.test.spring_task.kino.dto.FilmSearchRequest;
import ru.test.spring_task.kino.models.Film;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class FilmSpecifications {

    public static Specification<Film> withFilters(FilmSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getFilmName() != null) {
                predicates.add(cb.like(cb.lower(root.get("filmName")),
                        "%" + request.getFilmName().toLowerCase() + "%"));
            }
            if (request.getYearFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("year"), request.getYearFrom()));
            }
            if (request.getYearTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("year"), request.getYearTo()));
            }
            if (request.getRatingFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.getRatingFrom()));
            }
            if (request.getRatingTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), request.getRatingTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

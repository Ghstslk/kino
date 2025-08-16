package ru.test.spring_task.kino.services;

import org.springframework.data.jpa.domain.Specification;
import ru.test.spring_task.kino.models.Film;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class FilmSpecifications {

    public static Specification<Film> withFilters(
            String type,
            Double ratingFrom,
            Double ratingTo,
            Integer yearFrom,
            Integer yearTo) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по типу (если будет использоваться)
            // if (type != null && !type.isBlank()) {
            //     predicates.add(criteriaBuilder.equal(root.get("type"), type));
            // }

            // Фильтр по рейтингу (от)
            if (ratingFrom != null) {
                predicates.add(criteriaBuilder.ge(root.get("rating"), ratingFrom));
            }

            // Фильтр по рейтингу (до)
            if (ratingTo != null) {
                predicates.add(criteriaBuilder.le(root.get("rating"), ratingTo));
            }

            // Фильтр по году выпуска (от)
            if (yearFrom != null) {
                predicates.add(criteriaBuilder.ge(root.get("year"), yearFrom));
            }

            // Фильтр по году выпуска (до)
            if (yearTo != null) {
                predicates.add(criteriaBuilder.le(root.get("year"), yearTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

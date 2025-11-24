package ru.test.spring_task.kino.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.test.spring_task.kino.models.Film;

@Mapper(componentModel = "spring")
public interface FilmMapper {
    FilmMapper INSTANCE = Mappers.getMapper(FilmMapper.class);

    @Mapping(source = "filmId", target = "filmId")
    @Mapping(source = "filmName", target = "filmName")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "year",target = "year")
    FilmDto toDto(Film film);

    @Mapping(source = "filmId", target = "filmId")
    @Mapping(source = "filmName", target = "filmName")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "year",target = "year")
    Film toEntity(FilmDto filmDto);
}

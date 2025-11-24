CREATE TABLE mybd.films
(
    id        BIGINT auto_increment NOT NULL,
    film_id   BIGINT NULL UNIQUE,
    film_name VARCHAR(255) NULL,
    year      INT    NOT NULL,
    rating DOUBLE NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_film_id (film_id)
);

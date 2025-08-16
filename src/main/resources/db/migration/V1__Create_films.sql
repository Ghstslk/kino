CREATE TABLE films
(
    id        BIGINT NOT NULL,
    film_id   BIGINT NULL UNIQUE,
    film_name VARCHAR(255) NULL,
    year      INT    NOT NULL,
    rating DOUBLE NOT NULL,
    CONSTRAINT pk_film PRIMARY KEY (id)
);


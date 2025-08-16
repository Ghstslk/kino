package ru.test.spring_task.kino;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import ru.test.spring_task.kino.models.Film;
import ru.test.spring_task.kino.services.FilmService;

@SpringBootApplication
public class KinoApplication {

	@Autowired
	private FilmService filmService;

	public static void main(String[] args) {SpringApplication.run(KinoApplication.class, args);}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		try {
			System.out.println("Starting film data import...");
			filmService.fetchAndSaveFilms();
			System.out.println("Film data import completed successfully");
		} catch (Exception e) {
			System.err.println("Error during film data import: " + e.getMessage());
			e.printStackTrace();
		}
	}
}

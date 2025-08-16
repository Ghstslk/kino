package ru.test.spring_task.kino.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Value("${kinopoisk.api.token}")
    private String apiToken;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Добавляем интерсепторы
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(authInterceptor());
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

    private ClientHttpRequestInterceptor authInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().add("X-API-KEY", apiToken);
            request.getHeaders().add("Accept", "application/json");
            return execution.execute(request, body);
        };
    }
}
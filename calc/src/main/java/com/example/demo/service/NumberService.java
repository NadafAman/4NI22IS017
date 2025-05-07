package com.example.demo.service;
import com.example.demo.model.NumberResponse;
import com.example.demo.utill.SlidingWindow;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NumberService {

    private final RestTemplate restTemplate;
    private final SlidingWindow slidingWindow;
    private final String baseUrl;
    private final long timeout;

    public NumberService(@Value("${window.size}") int size,
                         @Value("${thirdparty.baseurl}") String baseUrl,
                         @Value("${timeout.ms}") long timeout) {
        this.restTemplate = new RestTemplate();
        this.slidingWindow = new SlidingWindow(size);
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }

    public NumberResponse fetchAndCalculate(String idType) {
        String url = baseUrl + "/" + resolveType(idType);
        List<Integer> prevState = slidingWindow.getCurrentState();
        List<Integer> fetched = fetchFromApi(url);
        slidingWindow.addNumbers(fetched);
        List<Integer> currState = slidingWindow.getCurrentState();
        double avg = slidingWindow.calculateAverage();

        return new NumberResponse(prevState, currState, fetched, avg);
    }

    private List<Integer> fetchFromApi(String url) {
        try {
            long start = System.currentTimeMillis();
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            long duration = System.currentTimeMillis() - start;

            if (duration > timeout || !response.getStatusCode().is2xxSuccessful()) {
                log.warn("Slow or failed response from 3rd party");
                return List.of();
            }

            Map<String, List<Integer>> body = response.getBody();
            return body.getOrDefault("numbers", List.of());
        } catch (Exception e) {
            log.error("Error calling API: {}", e.getMessage());
            return List.of();
        }
    }

    private String resolveType(String id) {
        return switch (id) {
            case "e" -> "even";
            case "f" -> "fibo";
            case "p" -> "primes";
            case "r" -> "rand";
            default -> throw new IllegalArgumentException("Invalid ID");
        };
    }
}

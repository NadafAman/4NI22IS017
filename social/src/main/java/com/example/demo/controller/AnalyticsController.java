package com.example.demo.controller;

import com.example.demo.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getTopUsers() {
        return ResponseEntity.ok(analyticsService.getTopUsersByCommentedPosts());
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(@RequestParam String type) {
        return ResponseEntity.ok(analyticsService.getPosts(type));
    }
}

package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://20.244.56.144/evaluation-service";

    public List<Map<String, Object>> getTopUsersByCommentedPosts() {
        Map<String, Object> userResponse = restTemplate.getForObject(BASE_URL + "/users", Map.class);
        Map<String, String> users = (Map<String, String>) userResponse.get("users");
        Map<Integer, Integer> commentCountByUser = new HashMap<>();

        for (String userIdStr : users.keySet()) {
            try {
                int userId = Integer.parseInt(userIdStr);
                List<Map<String, Object>> posts = restTemplate.getForObject(BASE_URL + "/users/" + userId + "/posts", List.class);
                int commentCount = 0;
                for (Map<String, Object> post : posts) {
                    Integer postId = (Integer) post.get("id");
                    List<Map<String, Object>> comments = restTemplate.getForObject(BASE_URL + "/posts/" + postId + "/comments", List.class);
                    commentCount += comments.size();
                }
                commentCountByUser.put(userId, commentCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return commentCountByUser.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .map(entry -> Map.of(
                        "userId", entry.getKey(),
                        "userName", users.get(String.valueOf(entry.getKey())),
                        "commentCount", entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPosts(String type) {
        Map<String, Object> userResponse = restTemplate.getForObject(BASE_URL + "/users", Map.class);
        Map<String, String> users = (Map<String, String>) userResponse.get("users");
        List<Map<String, Object>> allPosts = new ArrayList<>();

        for (String userIdStr : users.keySet()) {
            try {
                List<Map<String, Object>> posts = restTemplate.getForObject(BASE_URL + "/users/" + userIdStr + "/posts", List.class);
                allPosts.addAll(posts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ("latest".equalsIgnoreCase(type)) {
            return allPosts.stream()
                    .sorted((a, b) -> ((Integer) b.get("id")) - ((Integer) a.get("id")))
                    .limit(5)
                    .collect(Collectors.toList());
        } else if ("popular".equalsIgnoreCase(type)) {
            Map<Map<String, Object>, Integer> postToCommentCount = new HashMap<>();

            for (Map<String, Object> post : allPosts) {
                try {
                    Integer postId = (Integer) post.get("id");
                    List<Map<String, Object>> comments = restTemplate.getForObject(BASE_URL + "/posts/" + postId + "/comments", List.class);
                    postToCommentCount.put(post, comments.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int maxComments = postToCommentCount.values().stream().max(Integer::compare).orElse(0);

            return postToCommentCount.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxComments)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else {
            return List.of(Map.of("error", "Invalid type. Use 'latest' or 'popular'."));
        }
    }
}

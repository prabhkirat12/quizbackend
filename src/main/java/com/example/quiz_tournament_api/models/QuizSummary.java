package com.example.quiz_tournament_api.models;

public class QuizSummary {
    private Long id;          // ID of the quiz
    private String title;     // Title of the quiz
    private String status;    // Status of the quiz (e.g., ACTIVE, UPCOMING, PAST)

    // Default constructor (required by JPA and Hibernate)
    public QuizSummary() {}

    // Parameterized constructor for the custom query
    public QuizSummary(Long id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

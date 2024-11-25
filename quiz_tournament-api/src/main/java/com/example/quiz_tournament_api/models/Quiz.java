package com.example.quiz_tournament_api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int category; // Assuming category is represented by an integer code

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String createdBy; // The username of the admin who created the quiz

    @Column(nullable = false)
    private int likes = 0; // Tracks the number of likes for the quiz, default to 0

    @Column(nullable = false)
    private int dislikes = 0; // Tracks the number of dislikes for the quiz, default to 0

    @JsonIgnore // Prevent circular references during serialization
    @ManyToMany
    @JoinTable(
            name = "quiz_participants", // Table for mapping participants
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants; // List of users who participated in the quiz

    @JsonIgnore // Prevent circular references during serialization
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserLikes> userLikes; // List of UserLikes for managing likes/dislikes

    // Constructors
    public Quiz() {}

    public Quiz(String title, int questionCount, int category, String difficulty, LocalDateTime startDate, LocalDateTime endDate, String createdBy, int likes, int dislikes, List<User> participants, List<UserLikes> userLikes) {
        this.title = title;
        this.questionCount = questionCount;
        this.category = category;
        this.difficulty = difficulty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.likes = likes;
        this.dislikes = dislikes;
        this.participants = participants;
        this.userLikes = userLikes;
    }

    // Getters and Setters
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

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<UserLikes> getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(List<UserLikes> userLikes) {
        this.userLikes = userLikes;
    }

    // Additional Methods for Incrementing and Decrementing Likes/Dislikes
    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public void incrementDislikes() {
        this.dislikes++;
    }

    public void decrementDislikes() {
        if (this.dislikes > 0) {
            this.dislikes--;
        }
    }

}

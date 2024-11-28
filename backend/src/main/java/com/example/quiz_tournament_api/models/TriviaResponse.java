package com.example.quiz_tournament_api.models;

import java.util.List;

public class TriviaResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public static class Result {
        private String question;
        private String correct_answer;
        private List<String> incorrect_answers;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getCorrectAnswer() {
            return correct_answer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correct_answer = correctAnswer;
        }

        public List<String> getIncorrectAnswers() {
            return incorrect_answers;
        }

        public void setIncorrectAnswers(List<String> incorrectAnswers) {
            this.incorrect_answers = incorrectAnswers;
        }
    }
}

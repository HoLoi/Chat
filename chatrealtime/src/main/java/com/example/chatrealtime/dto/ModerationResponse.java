package com.example.chatrealtime.dto;

public class ModerationResponse {
    private String label;
    private Double score;
    private String action;

    public ModerationResponse() {
    }

    public ModerationResponse(String label, Double score, String action) {
        this.label = label;
        this.score = score;
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

package com.example.chatrealtime.dto;

public class ModerationRequest {
    private String text;
    private Integer userId;
    private Integer roomId;

    public ModerationRequest() {
    }

    public ModerationRequest(String text, Integer userId, Integer roomId) {
        this.text = text;
        this.userId = userId;
        this.roomId = roomId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
}

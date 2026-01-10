package com.example.chatrealtime.model;

public class Room {
    private int id;
    private String name;
    private String lastMessage;
    private int unreadCount;
    private String imageUrl;

    public Room(int id, String name, String lastMessage, String imageUrl) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.unreadCount = 0;
        this.imageUrl = imageUrl;
    }

    // Getter & Setter cho id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter & Setter cho name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter & Setter cho lastMessage
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getImageUrl() { return imageUrl; }

    // ✅ Getter & Setter cho unreadCount
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    // ✅ Tăng số lượng tin chưa đọc
    public void incrementUnread() {
        this.unreadCount++;
    }

    // ✅ Reset tin chưa đọc về 0
    public void resetUnread() {
        this.unreadCount = 0;
    }
}

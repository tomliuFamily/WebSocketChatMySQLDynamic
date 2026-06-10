package com.codebyx.chat.model;

import java.time.LocalDateTime;

public class ChatMessage {

    private long id;
    private String username;
    private String message;
    private LocalDateTime sentAt;

    public ChatMessage() {
    }

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public ChatMessage(long id, String username, String message, LocalDateTime sentAt) {
        this.id = id;
        this.username = username;
        this.message = message;
        this.sentAt = sentAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}

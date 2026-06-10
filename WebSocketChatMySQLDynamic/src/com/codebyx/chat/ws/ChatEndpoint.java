package com.codebyx.chat.ws;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.codebyx.chat.dao.ChatMessageDao;
import com.codebyx.chat.model.ChatMessage;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat/{username}")
public class ChatEndpoint {

    private static final Map<String, Session> ONLINE_USERS = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ChatMessageDao CHAT_MESSAGE_DAO = new ChatMessageDao();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String rawUsername) {
        String username = createUniqueUsername(rawUsername);

        session.getUserProperties().put("username", username);
        ONLINE_USERS.put(username, session);

        sendHistoryToCurrentUser(session);
        sendToSession(session, buildSystemMessage("連線成功，你的名稱是：" + username));
        broadcast(buildSystemMessage(username + " 進入聊天室"));
        broadcastUserList();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String username = (String) session.getUserProperties().get("username");

        if (username == null || message == null) {
            return;
        }

        String text = message.trim();
        if (text.isEmpty()) {
            return;
        }

        ChatMessage chatMessage = new ChatMessage(username, text);
        CHAT_MESSAGE_DAO.save(chatMessage);

        broadcast(buildChatMessage(username, text, LocalDateTime.now()));
    }

    @OnClose
    public void onClose(Session session) {
        String username = (String) session.getUserProperties().get("username");

        if (username != null) {
            ONLINE_USERS.remove(username);
            broadcast(buildSystemMessage(username + " 離開聊天室"));
            broadcastUserList();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
        if (session != null) {
            sendToSession(session, buildSystemMessage("伺服器發生錯誤：" + escapeJson(throwable.getMessage())));
        }
    }

    private static void sendHistoryToCurrentUser(Session session) {
        List<ChatMessage> history = new ArrayList<>();
        try {
            history = CHAT_MESSAGE_DAO.findRecent(20);
        } catch (Exception e) {
            sendToSession(session, buildSystemMessage("讀取歷史紀錄失敗，請確認 MySQL 與 JDBC Driver 設定"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"history\",");
        sb.append("\"messages\":[");

        for (int i = 0; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{")
              .append("\"username\":\"").append(escapeJson(msg.getUsername())).append("\",")
              .append("\"message\":\"").append(escapeJson(msg.getMessage())).append("\",")
              .append("\"sentAt\":\"").append(formatDateTime(msg.getSentAt())).append("\"")
              .append("}");
        }

        sb.append("]}");
        sendToSession(session, sb.toString());
    }

    private static synchronized String createUniqueUsername(String rawUsername) {
        String base = sanitize(rawUsername);
        String candidate = base;
        int index = 1;

        while (ONLINE_USERS.containsKey(candidate)) {
            candidate = base + "_" + index++;
        }

        return candidate;
    }

    private static String sanitize(String rawUsername) {
        if (rawUsername == null) {
            return "訪客";
        }

        String value = rawUsername.trim();
        if (value.isEmpty()) {
            return "訪客";
        }

        value = value.replaceAll("[^\\p{L}\\p{N}_\\-\\u4e00-\\u9fff]", "");
        return value.isEmpty() ? "訪客" : value;
    }

    private static void broadcast(String json) {
        for (Session session : ONLINE_USERS.values()) {
            sendToSession(session, json);
        }
    }

    private static void broadcastUserList() {
        List<String> users = new ArrayList<>(ONLINE_USERS.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"users\",");
        sb.append("\"users\":[");

        for (int i = 0; i < users.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escapeJson(users.get(i))).append("\"");
        }

        sb.append("]}");
        broadcast(sb.toString());
    }

    private static String buildSystemMessage(String message) {
        return "{"
                + "\"type\":\"system\","
                + "\"time\":\"" + currentTime() + "\","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}";
    }

    private static String buildChatMessage(String username, String message, LocalDateTime sentAt) {
        return "{"
                + "\"type\":\"chat\","
                + "\"time\":\"" + formatDateTime(sentAt) + "\","
                + "\"username\":\"" + escapeJson(username) + "\","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}";
    }

    private static String currentTime() {
        return formatDateTime(LocalDateTime.now());
    }

    private static String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return TIME_FORMATTER.format(time);
    }

    private static void sendToSession(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        synchronized (session) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n");
    }
}

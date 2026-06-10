package com.codebyx.chat.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.codebyx.chat.model.ChatMessage;
import com.codebyx.chat.util.DBUtil;

public class ChatMessageDao {

    public void save(ChatMessage chatMessage) {
        String sql = "INSERT INTO chat_message(username, message) VALUES(?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, chatMessage.getUsername());
            ps.setString(2, chatMessage.getMessage());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("儲存聊天紀錄失敗", e);
        }
    }

    public List<ChatMessage> findRecent(int limit) {
        String sql = "SELECT id, username, message, sent_at " +
                     "FROM chat_message " +
                     "ORDER BY id DESC " +
                     "LIMIT ?";

        List<ChatMessage> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String message = rs.getString("message");
                    Timestamp ts = rs.getTimestamp("sent_at");
                    LocalDateTime sentAt = ts == null ? null : ts.toLocalDateTime();

                    list.add(new ChatMessage(id, username, message, sentAt));
                }
            }

            Collections.reverse(list);
            return list;

        } catch (Exception e) {
            throw new RuntimeException("讀取聊天紀錄失敗", e);
        }
    }
}

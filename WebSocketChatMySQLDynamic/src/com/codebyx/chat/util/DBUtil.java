package com.codebyx.chat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/websocket_chat_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("找不到 MySQL JDBC Driver，請確認 mysql-connector-j 已放入 WEB-INF/lib", e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

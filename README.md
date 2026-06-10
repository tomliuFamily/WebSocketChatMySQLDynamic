# WebSocketChatMySQLDynamic

Dynamic Web Project + MySQL 聊天紀錄版 WebSocket 聊天室範例

## 環境
- JDK 17
- Eclipse IDE for Enterprise Java and Web Developers 2025-03
- Apache Tomcat 10.1
- MySQL 8.0
- Jakarta WebSocket

## 專案特色
- Eclipse Dynamic Web Project
- 使用 `jakarta.websocket.*`
- WebSocket 即時聊天室
- 訊息寫入 MySQL
- 新連線使用者自動載入最近 20 筆聊天紀錄

## 專案結構
```text
WebSocketChatMySQLDynamic
├─ src
│  └─ com/codebyx/chat
│     ├─ dao/ChatMessageDao.java
│     ├─ model/ChatMessage.java
│     ├─ util/DBUtil.java
│     └─ ws/ChatEndpoint.java
├─ WebContent
│  ├─ index.jsp
│  └─ WEB-INF
│     ├─ lib/README-driver.txt
│     ├─ sql/chat_message.sql
│     └─ web.xml
├─ .project
├─ .classpath
├─ .settings
└─ README.md
```

## 先做資料庫
執行：
`WebContent/WEB-INF/sql/chat_message.sql`

建立：
- database: `websocket_chat_db`
- table: `chat_message`

## 設定 DBUtil
請修改：
`src/com/codebyx/chat/util/DBUtil.java`

預設是：

```java
private static final String URL = "jdbc:mysql://localhost:3306/websocket_chat_db?useSSL=false&serverTimezone=Asia/Taipei&characterEncoding=utf8";
private static final String USER = "root";
private static final String PASSWORD = "1234";
```

請改成你的 MySQL 帳號密碼。

## 加入 MySQL JDBC Driver
本 ZIP **沒有內含 MySQL Connector/J**。

請自行下載 `mysql-connector-j-8.x.x.jar`，然後放到：

`WebContent/WEB-INF/lib/`

放入後：
1. Eclipse 右鍵專案 → `Refresh`
2. 若仍未自動加入，右鍵專案 → `Build Path > Configure Build Path`
3. 手動把該 JAR 加入 Build Path

## Eclipse 匯入方式
1. 解壓縮 ZIP
2. Eclipse → `File > Import > Existing Projects into Workspace`
3. 選解壓後資料夾
4. 匯入 `WebSocketChatMySQLDynamic`

## 設定 Tomcat 10.1
1. `Window > Preferences > Server > Runtime Environments`
2. 新增 `Apache Tomcat v10.1`
3. 指向你的 Tomcat 10.1 目錄
4. JRE 選 JDK 17

## 綁定 Runtime
專案右鍵：
`Properties > Targeted Runtimes`
勾選：
`Apache Tomcat v10.1`

## 啟動方式
1. 在 Servers 視圖建立 Tomcat 10.1 Server
2. 將 `WebSocketChatMySQLDynamic` 加入 Server
3. 啟動後開啟：

```text
http://localhost:8080/WebSocketChatMySQLDynamic/
```

## 測試方式
1. 開兩個瀏覽器視窗
2. 分別輸入不同名稱連線
3. 傳送訊息
4. 重新開新視窗連線，會看到最近 20 筆歷史紀錄

## 注意事項
- Tomcat 10.1 必須使用 `jakarta.websocket.*`
- 不能使用舊版 `javax.websocket.*`
- 若出現 JDBC Driver 錯誤，表示 MySQL Connector/J 尚未放入 `WEB-INF/lib`
- 若出現資料庫連線錯誤，請檢查 `DBUtil.java` 的 URL、帳號、密碼

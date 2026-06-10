<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
<meta charset="UTF-8">
<title>WebSocket 聊天室 + MySQL 聊天紀錄</title>
<style>
    body {
        margin: 0;
        font-family: Arial, "Microsoft JhengHei", sans-serif;
        background: #f3f6fb;
        color: #1f2937;
    }
    .wrap {
        width: 1180px;
        margin: 24px auto;
        display: grid;
        grid-template-columns: 280px 1fr;
        gap: 20px;
    }
    .card {
        background: white;
        border-radius: 14px;
        box-shadow: 0 6px 20px rgba(0,0,0,0.08);
        overflow: hidden;
    }
    .title {
        background: #1d4ed8;
        color: white;
        padding: 16px 20px;
        font-size: 20px;
        font-weight: bold;
    }
    .body {
        padding: 16px;
    }
    label {
        display: block;
        font-weight: bold;
        margin-bottom: 6px;
    }
    input[type="text"] {
        width: 100%;
        box-sizing: border-box;
        padding: 10px 12px;
        border-radius: 8px;
        border: 1px solid #cbd5e1;
        margin-bottom: 12px;
        font-size: 14px;
    }
    button {
        border: none;
        padding: 10px 14px;
        border-radius: 8px;
        color: white;
        cursor: pointer;
        margin-right: 8px;
    }
    .btn-connect { background: #16a34a; }
    .btn-disconnect { background: #dc2626; }
    .btn-send { background: #2563eb; }
    .status {
        margin-top: 12px;
        color: #475569;
        font-size: 14px;
    }
    #users {
        list-style: none;
        padding: 0;
        margin: 12px 0 0 0;
    }
    #users li {
        padding: 8px 10px;
        border-bottom: 1px solid #e5e7eb;
    }
    #chatBox {
        height: 500px;
        overflow-y: auto;
        border: 1px solid #dbe2ea;
        border-radius: 10px;
        background: #fafcff;
        padding: 14px;
        margin-bottom: 12px;
    }
    .msg {
        margin-bottom: 10px;
        line-height: 1.6;
    }
    .system {
        color: #7c3aed;
    }
    .history {
        color: #6b7280;
    }
    .chat {
        color: #111827;
    }
    .send-row {
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 10px;
    }
    .tip {
        margin-top: 10px;
        font-size: 13px;
        color: #64748b;
        line-height: 1.7;
    }
</style>
</head>
<body>
<div class="wrap">
    <div class="card">
        <div class="title">連線設定</div>
        <div class="body">
            <label for="username">使用者名稱</label>
            <input type="text" id="username" value="Allen">

            <button class="btn-connect" onclick="connectChat()">連線</button>
            <button class="btn-disconnect" onclick="disconnectChat()">斷線</button>

            <div class="status" id="status">尚未連線</div>

            <h3>目前在線</h3>
            <ul id="users"></ul>

            <div class="tip">
                本範例特色：<br>
                1. WebSocket 即時聊天室<br>
                2. MySQL 儲存聊天紀錄<br>
                3. 新連線使用者會看到最近 20 筆歷史紀錄
            </div>
        </div>
    </div>

    <div class="card">
        <div class="title">聊天室</div>
        <div class="body">
            <div id="chatBox"></div>

            <div class="send-row">
                <input type="text" id="message" placeholder="輸入訊息後按 Enter">
                <button class="btn-send" onclick="sendMessage()">送出</button>
            </div>
        </div>
    </div>
</div>

<script>
    let ws = null;
    const ctxPath = '<%= request.getContextPath() %>';

    function setStatus(text) {
        document.getElementById('status').innerText = text;
    }

    function appendMessage(cssClass, text) {
        const box = document.getElementById('chatBox');
        const div = document.createElement('div');
        div.className = 'msg ' + cssClass;
        div.innerText = text;
        box.appendChild(div);
        box.scrollTop = box.scrollHeight;
    }

    function renderUsers(users) {
        const ul = document.getElementById('users');
        ul.innerHTML = '';
        users.forEach(function(user) {
            const li = document.createElement('li');
            li.innerText = user;
            ul.appendChild(li);
        });
    }

    function connectChat() {
        const username = document.getElementById('username').value.trim() || '訪客';

        if (ws && ws.readyState === WebSocket.OPEN) {
            appendMessage('system', '你已經連線中。');
            return;
        }

        const protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
        const url = protocol + location.host + ctxPath + '/chat/' + encodeURIComponent(username);

        ws = new WebSocket(url);

        ws.onopen = function() {
            setStatus('已連線');
            appendMessage('system', 'WebSocket 已連線。');
        };

        ws.onmessage = function(event) {
            const data = JSON.parse(event.data);

            if (data.type === 'system') {
                appendMessage('system', '[' + data.time + '] ' + data.message);
            } else if (data.type === 'history') {
                appendMessage('history', '===== 最近聊天紀錄 =====');
                data.messages.forEach(function(msg) {
                    appendMessage('history', '[' + msg.sentAt + '] ' + msg.username + '：' + msg.message);
                });
                appendMessage('history', '====================');
            } else if (data.type === 'chat') {
                appendMessage('chat', '[' + data.time + '] ' + data.username + '：' + data.message);
            } else if (data.type === 'users') {
                renderUsers(data.users);
            }
        };

        ws.onclose = function() {
            setStatus('已斷線');
            appendMessage('system', 'WebSocket 已斷線。');
            renderUsers([]);
        };

        ws.onerror = function() {
            appendMessage('system', '發生 WebSocket 錯誤，請檢查 Tomcat / MySQL / JDBC Driver 設定。');
        };
    }

    function disconnectChat() {
        if (ws) {
            ws.close();
            ws = null;
        }
    }

    function sendMessage() {
        const input = document.getElementById('message');
        const text = input.value.trim();

        if (!ws || ws.readyState !== WebSocket.OPEN) {
            appendMessage('system', '請先建立連線。');
            return;
        }

        if (text === '') {
            return;
        }

        ws.send(text);
        input.value = '';
        input.focus();
    }

    document.getElementById('message').addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });
</script>
</body>
</html>

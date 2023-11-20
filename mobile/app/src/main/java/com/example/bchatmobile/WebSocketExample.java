package com.example.bchatmobile;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketExample {

    private WebSocket webSocket;
    private WebSocketListener webSocketListener;
    private String token;
    private Integer chat_id;

    public WebSocketExample(String token, Integer chat_id) {
        this.token = token;
        this.chat_id = chat_id;
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void start() {
        OkHttpClient client = new OkHttpClient.Builder().build();

        String url = "ws://194.87.199.70/api/ws/chat/"+chat_id+"?token="+token;

        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, webSocketListener);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Goodbye, World!");
        }
    }
}

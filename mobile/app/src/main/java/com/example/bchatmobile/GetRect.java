package com.example.bchatmobile;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;
import android.os.Looper;

public class GetRect {

    public static void sendGetRequest(final String url, final HttpCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL requestUrl = new URL(url);
                    connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        // Преобразование строки JSON в объект JSON
                        final JSONObject jsonObject = new JSONObject(response.toString());

                        // Вызов обратного вызова в главном потоке (UI thread)
                        if (callback != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResponse(jsonObject);
                                }
                            });
                        }
                    } else {
                        final String errorMessage = "HTTP Error Code: " + responseCode;
                        if (callback != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResponseError(errorMessage);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final String error = e.getMessage();
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponseError(error);
                            }
                        });
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public interface HttpCallback {
        void onResponse(JSONObject response); // Обратный вызов для JSON-ответа
        void onResponseError(String error); // Обратный вызов для ошибок
    }
}

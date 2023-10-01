package com.example.bchatmobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.example.bchatmobile.GetRect;

public class MainActivity extends Activity {
    private TextView jsonTextView;
    private Button sendRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonTextView = findViewById(R.id.jsonTextView);
        sendRequestButton = findViewById(R.id.sendRequestButton);
    }

    public void onSendGetRequestClick(View view) {
        Log.i("MyApp", "Это информационное сообщение");
        sendRequestButton.setEnabled(false); // Заблокировать кнопку во время запроса
        System.setProperty("javax.net.debug", "all");
        GetRect.sendGetRequest("http:/194.87.199.70/api/users/1", new GetRect.HttpCallback() {

            public void onResponse(String response) {
                try {
                    // Парсим строку JSON в объект JSON

                    JSONArray jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 0) {
                        Log.i("MyApp", "хууууй");
                        // Получаем первый JSON-объект из массива
                        JSONObject firstJsonObject = jsonArray.getJSONObject(0);

                        // Обработка JSON-ответа
                        // В этом месте вы можете работать с полученными данными из firstJsonObject
                        final String jsonResult = firstJsonObject.toString();

                        // Отобразить JSON-результат на экране в главном потоке
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                jsonTextView.setText(jsonResult);
                                jsonTextView.setVisibility(View.VISIBLE);
                                sendRequestButton.setEnabled(true); // Разблокировать кнопку после запроса
                            }
                        });
                    } else {
                        showToast("Массив JSON пуст.");
                        sendRequestButton.setEnabled(true); // Разблокировать кнопку после запроса
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("Ошибка при разборе JSON");
                    sendRequestButton.setEnabled(true); // Разблокировать кнопку после запроса
                }
            }

            @Override
            public void onResponseError(String error) {
                // Обработка ошибки (если возникает)
                showToast("Ошибка при запросе: " + error);
                sendRequestButton.setEnabled(true); // Разблокировать кнопку после запроса
            }

            @Override
            public void onResponse(JSONObject jsonObject) {
                // Обработка JSON-ответа
                // В этом месте вы можете работать с полученными данными из jsonObject
                final String jsonResult = jsonObject.toString();

                // Отобразить JSON-результат на экране в главном потоке
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        jsonTextView.setText(jsonResult);
                        jsonTextView.setVisibility(View.VISIBLE);
                        sendRequestButton.setEnabled(true); // Разблокировать кнопку после запроса
                    }
                });
            }
        });
    }



    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onExitButtonClick(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}

package com.example.bchatmobile;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;


public class LoginActivity extends AppCompatActivity {

    private TextView myTextView;
    private TextView myTextViewReg;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Log.d("MyApp", "Token: " + token);
        if (token != null && !token.isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_login);
            myTextView = findViewById(R.id.button2);
            String text = "Забыли пароль?";
            SpannableString spannableString = new SpannableString(text);
            spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
            myTextView.setText(spannableString);
            myTextViewReg = findViewById(R.id.button3);
            String text1 = "Регистрация";
            SpannableString spannableStringReg = new SpannableString(text1);
            spannableStringReg.setSpan(new UnderlineSpan(), 0, text1.length(), 0);
            myTextViewReg.setText(spannableStringReg);
            emailEditText = findViewById(R.id.editTextTextEmailAddress);
            passwordEditText = findViewById(R.id.editTextTextPassword);
        }
    }

    public void onLogButtonClick(View view){
        String baseUrl = "http://194.87.199.70/api/users/login";
        String email = emailEditText.getText().toString();
        String hashed_password = passwordEditText.getText().toString();
        new PerformGetRequestAsyncTask(this).execute(baseUrl, email, hashed_password);
    }


    private class PerformGetRequestAsyncTask extends AsyncTask<String, Void, String> {
        private final Context context;

        public PerformGetRequestAsyncTask(Context context) {
            this.context = context;
        }
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String email = params[1];
            String password = params[2];

            OkHttpClient client = new OkHttpClient();

            FormBody formBody = new FormBody.Builder()
                    .add("email", email)
                    .add("password", password)
                    .build();


            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    String Token = jsonObject.optString("access_token", ""); // Получаем значение access_token
                    String Id = jsonObject.optString("user_id", ""); // Получаем значение user_id

                    Log.d("MyApp", "Token: " + Token);
                    Log.d("MyApp", "id: " + Id);

                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", Token);
                    editor.putString("id", Id);
                    editor.apply();
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    return jsonResponse;
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String errorMessage = "Ошибка при авторизации: " + response.message();
                            if (response.code() == 401) {
                                try {
                                    String responseBody = response.body().string();
                                    errorMessage = "Ошибка при авторизации: " + responseBody;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void onRegButtonClick(View view) {
        Intent intent = new Intent(this, RegActivity.class);
        startActivity(intent);
    }
}
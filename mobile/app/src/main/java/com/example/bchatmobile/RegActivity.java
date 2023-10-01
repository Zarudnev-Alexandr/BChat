package com.example.bchatmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.Dialog;
import java.util.Calendar;
import android.widget.DatePicker;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText NameEditText;
    private EditText SurnameEditText;
    private EditText NicknameEditText;
    private EditText DateOfBirtEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        emailEditText = findViewById(R.id.editTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextPassword);
        NameEditText = findViewById(R.id.editTextName);
        SurnameEditText = findViewById(R.id.editTextSurname);
        NicknameEditText = findViewById(R.id.editTextNickname);
        DateOfBirtEditText = findViewById(R.id.editTextDate2);
        DateOfBirtEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Обработка выбранной даты
                        String formattedMonth = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
                        String formattedDay = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                        String selectedDate = formattedDay + "/" + formattedMonth + "/" + year;
                        DateOfBirtEditText.setText(selectedDate);
                    }
                },
                // Установите текущую дату в качестве значения по умолчанию в диалоге
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        // Показать диалог выбора даты
        datePickerDialog.show();
    }

    public void onRegisterButtonClick(View view) {
        String email = emailEditText.getText().toString();
        String hashed_password = passwordEditText.getText().toString();
        String name = NameEditText.getText().toString();
        String surname = SurnameEditText.getText().toString();
        String nickname = NicknameEditText.getText().toString();
        String date_of_birth = DateOfBirtEditText.getText().toString();
        String formattedDate = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = inputFormat.parse(date_of_birth);

            SimpleDateFormat iso8601Format = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            }
            formattedDate = iso8601Format.format(date);

            // Теперь у вас есть formattedDate в формате ISO 8601, который вы можете отправить на сервер
        } catch (ParseException e) {
            e.printStackTrace();
            // Обработка ошибки при неправильном формате введенной даты
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("email", email);
            postData.put("hashed_password", hashed_password);
            postData.put("name", name);
            postData.put("surname", surname);
            postData.put("nickname", nickname);
            postData.put("date_of_birth", formattedDate);
            postData.put("imageURL", "dedGandon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http:/194.87.199.70/api/users/register";

        RequestBody requestBody = RequestBody.create(postData.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String errorMessage = "Ошибка при регистрации: " + response.message();
                            if (response.code() == 422) { // Код 422 часто используется для "Unprocessable Entity"
                                try {
                                    String responseBody = response.body().string();
                                    errorMessage = "Ошибка при регистрации: " + responseBody;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Toast.makeText(RegActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

    }

    public void onBackButtonClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}
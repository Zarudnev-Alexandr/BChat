package com.example.bchatmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;


public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView surnameTextView;
    private TextView Brday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        surnameTextView = findViewById(R.id.surnameTextView);
        Brday = findViewById(R.id.BrdayTextView);

        Intent intent = getIntent();
        int userId = intent.getIntExtra("userId", 0);

        fetchUserProfile(userId);
    }

    private void fetchUserProfile(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://194.87.199.70/api/users/" + userId)
                .get()
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject userObject = new JSONObject(responseBody);

                        final String name = userObject.getString("name");
                        final String date_of_birth = userObject.getString("date_of_birth");
                        final String surname = userObject.getString("surname");
                        String fullUrl = "http://194.87.199.70/" + userObject.getString("imageURL");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nameTextView.setText(name);
                                surnameTextView.setText(surname);
                                Brday.setText(date_of_birth);
                                Picasso.get().load(fullUrl).error(R.drawable.image).into(profileImageView);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}

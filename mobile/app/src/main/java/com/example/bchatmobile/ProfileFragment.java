package com.example.bchatmobile;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private TextView nameTextView;
    private TextView surnameTextView;
    private TextView birthDateTextView;
    private TextView nicknameTextView;
    private ImageView avatarImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.prof_fragment, container, false);
        Button setAvatarButton = view.findViewById(R.id.setAvatarButton);
        setAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        nameTextView = view.findViewById(R.id.nameTextView);
        surnameTextView = view.findViewById(R.id.surnameTextView);
        birthDateTextView = view.findViewById(R.id.birthDateTextView);
        nicknameTextView = view.findViewById(R.id.nicknameTextView);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");
        String Token = sharedPreferences.getString("token", "");
        Log.d("Penscl", userId);
        Log.d("Penscl", Token);

        new GetUserProfileTask().execute(userId,Token);

        return view;
    }

    private class GetUserProfileTask extends AsyncTask<String, Void, UserProfile> {

        @Override
        protected UserProfile doInBackground(String... params) {

            String userId = params[0];
            String Token = params[1];
            Log.d("MyApp", Token);
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://194.87.199.70/api/users/" + userId)
                    .get()
                    .addHeader("token", Token)
                    .build();
            Log.d("MyApp", "Obj: " + request);
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    JSONObject json = new JSONObject(responseBody);
                    Log.d("MyApp", "Obj: " + json);
                    int id = json.getInt("id");
                    String name = json.getString("name");
                    String surname = json.getString("surname");
                    String birthDate = json.getString("date_of_birth");
                    String nickname = json.getString("nickname");
                    String userAvatar = json.getString("imageURL");

                    UserProfile userProfile = new UserProfile(id,name,surname,birthDate,nickname,userAvatar);
                    userProfile.setName(name);
                    userProfile.setSurname(surname);
                    userProfile.setBirthDate(birthDate);
                    userProfile.setNickname(nickname);
                    userProfile.setUserAvatar(userAvatar);


                    return userProfile;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(UserProfile userProfile) {

            if (userProfile != null) {

                nameTextView.setText("Имя: " + userProfile.getName());
                surnameTextView.setText("Фамилия: " + userProfile.getSurname());
                birthDateTextView.setText("Дата рождения: " + userProfile.getBirthDate());
                nicknameTextView.setText("Никнейм: " + userProfile.getNickname());

                if (userProfile.getUserAvatar() != null && !userProfile.getUserAvatar().isEmpty()) {
                    String fullUrl = "http://194.87.199.70/" + userProfile.getUserAvatar();
                    loadUserAvatar(fullUrl);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            new UploadImageTask().execute();
        }
    }

    private class UploadImageTask extends AsyncTask<Void, Void, Void> {

        private byte[] toByteArray(InputStream inputStream) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            return outputStream.toByteArray();
        }

        private InputStream getInputStreamFromUri(Uri uri) {
            try {
                ContentResolver contentResolver = getContext().getContentResolver();
                return contentResolver.openInputStream(uri);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected Void doInBackground(Void... params) {


            try {


                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String Token = sharedPreferences.getString("token", "");

                InputStream inputStream = getInputStreamFromUri(selectedImageUri);




                byte[] fileBytes = toByteArray(inputStream);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), fileBytes);



                String serverUrl = "http://194.87.199.70/api/users/user-avatar/";

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addPart(MultipartBody.Part.createFormData("image_file", "image.jpg", requestFile))
                        .build();

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .addHeader("token", Token)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                } else {
                    String errorMessage = "Ошибка при регистрации: " + response.message();
                    if (response.code() == 422) {
                        try {
                            String responseBody = response.body().string();
                            errorMessage = "Ошибка при авторизации: " + responseBody;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;


        }


    }

    private void loadUserAvatar(String avatarUrl) {
            Picasso.get()
                    .load(avatarUrl)
                    .error(R.drawable.image)
                    .into(avatarImageView);
    }



}

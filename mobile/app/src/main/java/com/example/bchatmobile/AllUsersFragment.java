package com.example.bchatmobile;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AllUsersFragment  extends Fragment{

    private ListView userListView;
    private List<UserProfile> userList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_users, container, false);

        userListView = view.findViewById(R.id.userListView);

        fetchUserList();

        return view;

    }

    private void fetchUserList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://194.87.199.70/api/users")
                .get()
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    userList = parseUserListFromJson(responseBody);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UserProfileAdapter adapter = new UserProfileAdapter(getContext(), userList);
                            userListView.setAdapter(adapter);

                            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    UserProfile selectedUser = userList.get(position);
                                    int userId = selectedUser.getId();


                                     Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                                     intent.putExtra("userId", userId);
                                     startActivity(intent);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private List<UserProfile> parseUserListFromJson(String json) {
        List<UserProfile> users = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for (JsonElement userElement : jsonArray) {
            JsonObject userObject = userElement.getAsJsonObject();

            int id = userObject.has("id") && !userObject.get("id").isJsonNull() ? userObject.get("id").getAsInt() : 0;
            String nickname = userObject.has("nickname") && !userObject.get("nickname").isJsonNull() ? userObject.get("nickname").getAsString() : "";
            String name = userObject.has("name") && !userObject.get("name").isJsonNull() ? userObject.get("name").getAsString() : "";
            String birthDate = userObject.has("birthDate") && !userObject.get("birthDate").isJsonNull() ? userObject.get("birthDate").getAsString() : "";
            String surname = userObject.has("surname") && !userObject.get("surname").isJsonNull() ? userObject.get("surname").getAsString() : "";
            String imageURL = userObject.has("imageURL") && !userObject.get("imageURL").isJsonNull() ? userObject.get("imageURL").getAsString() : "";

            UserProfile user = new UserProfile(id, name, surname, birthDate, nickname, imageURL);
            users.add(user);
        }

        return users;
    }


}

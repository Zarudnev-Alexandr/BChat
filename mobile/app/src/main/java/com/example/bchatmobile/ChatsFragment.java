package com.example.bchatmobile;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.AdapterView;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ChatsFragment extends Fragment {

    private ListView chatsListView;
    private List<ChatObj> chatList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats, container, false);

        chatsListView = view.findViewById(R.id.chatsListView);

        fetchChatList();

        return view;

    }

    private void fetchChatList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://194.87.199.70/api/users/chats/")
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
                    if (responseBody.contains("не найдено")) return;
                    chatList = parseChatListFromJson(responseBody);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatAdapter adapter = new ChatAdapter(getContext(), chatList);
                            chatsListView.setAdapter(adapter);

//                            chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                                @Override
//                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                                    ChatObj selectedChat = chatList.get(position);
//                                    int chatid = selectedChat.getId();
//
//
//                                    Intent intent = new Intent(getActivity(), UserProfileActivity.class);
//                                    intent.putExtra("chatId", chatid);
//                                    startActivity(intent);
//                                }
//                            });
                        }
                    });
                }
            }
        });
    }

    private List<ChatObj> parseChatListFromJson(String json) {
        List<ChatObj> chats = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for (JsonElement chatElement : jsonArray) {
            JsonObject chatObject = chatElement.getAsJsonObject();

            int id = chatObject.has("id") && !chatObject.get("id").isJsonNull() ? chatObject.get("id").getAsInt() : 0;
            String name = chatObject.has("name") && !chatObject.get("name").isJsonNull() ? chatObject.get("name").getAsString() : "";
            String date = chatObject.has("date") && !chatObject.get("date").isJsonNull() ? chatObject.get("date").getAsString() : "";


            ChatObj chat = new ChatObj(id, name, date);
            chats.add(chat);
        }

        return chats;
    }


}

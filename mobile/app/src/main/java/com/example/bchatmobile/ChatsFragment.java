package com.example.bchatmobile;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private String token;

    private String bob;
    private Integer userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats, container, false);

        chatsListView = view.findViewById(R.id.chatsListView);

        Button createChatButton = view.findViewById(R.id.createChatButton);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
         token = sharedPreferences.getString("token", "");
         userId = Integer.parseInt(sharedPreferences.getString("id", "0"));

        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateChatDialog();
            }
        });

        fetchChatList();

        return view;

    }

    private void fetchChatList() {



        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://194.87.199.70/api/chats/")
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

                            chatsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Handle long-press on a chat item
                                    showChatOptionsDialog(position);
                                    return true; // Return true to consume the long-click event
                                }
                            });

                            chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    ChatObj selectedChat = chatList.get(position);
                                    int chatid = selectedChat.getId();


                                    Intent intent = new Intent(getActivity(), InChatActivity.class);
                                    intent.putExtra("chatId", chatid);
                                    intent.putExtra("token", token);
                                    intent.putExtra("user_id", userId);
                                    startActivity(intent);
                                }
                            });
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


    private void showCreateChatDialog() {
        // Create an AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create New Chat");

        // Inflate the layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_chat, null);
        builder.setView(dialogView);

        EditText editTextChatName = dialogView.findViewById(R.id.editTextChatName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String chatName = editTextChatName.getText().toString();

                if (chatName.isEmpty()) {return;}
                String jsonData = "{ \"name\": \"" + chatName + "\" }";


                String url = "http://194.87.199.70/api/chats/add/";

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonData);

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)  // Use POST method to include request body
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
                            String responseBody = response.body().string();
                            Log.d("mymessage", "Successful response: " + responseBody);
                        } else {
                            Log.e("mymessage", "Unsuccessful response: " + response.code() + " " + response.message());
                        }

                    }
                });
            }});

        //Set up the negative (Cancel) button click listener
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel button clicked, do nothing
                    }
                });

                // Show the dialog
                builder.show();
            }


    private void showChatOptionsDialog(int position) {
        // Get the selected chat
        ChatObj selectedChat = chatList.get(position);
        int chatId = selectedChat.getId();

        // Create an AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chat Options");

        // Set up options for the dialog
        String[] options = {"Leave","Delete"}; // Add your options here
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the option click
                switch (which) {
                    case 0:
                        String url = "http://194.87.199.70/api/chats/"+ chatId +"/leave/";

                        OkHttpClient client = new OkHttpClient();

// Create a request body with JSON data

                        Request request = new Request.Builder()
                                .url(url)
                                .delete()
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
                                    String responseBody = response.body().string();
                                    Log.d("mymessage", "Successful response: " + responseBody);
                                } else {
                                    Log.e("mymessage", "Unsuccessful response: " + response.code() + " " + response.message());
                                }

                            }
                        });

                        break;
                    case 1:
                         url = "http://194.87.199.70/api/chats/"+ chatId +"/delete/";

                         client = new OkHttpClient();

                        // Create a request body with JSON data

                         request = new Request.Builder()
                                .url(url)
                                .delete()
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
                                    String responseBody = response.body().string();
                                    Log.d("mymessage", "Successful response: " + responseBody);
                                } else {
                                    Log.e("mymessage", "Unsuccessful response: " + response.code() + " " + response.message());
                                }

                            }
                        });

                        break;
                }
            }
        });

        // Set up the negative (Cancel) button click listener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Cancel button clicked, do nothing
            }
        });

        // Show the dialog
        builder.show();
    }
        }



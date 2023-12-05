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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
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

    private int currentVisiblePosition;
    private int currentTop;
    private int userId;

    private int currentPage = 0; // начальная страница
    private boolean isLoading = false; // флаг, чтобы избежать одновременных запросов



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats, container, false);

        chatsListView = view.findViewById(R.id.chatsListView);

        Button createChatButton = view.findViewById(R.id.createChatButton);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");
        userId = sharedPreferences.getInt("id", 0);

        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateChatDialog();
            }
        });

        fetchChatList();

            chatsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    // Не требуется в данном случае
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    // Проверяем, что пользователь прокрутил до конца списка
                    if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                        // Загружаем следующую страницу данных
                        fetchChatList();
                    }
                }

            });


        return view;

    }

  private void fetchChatList() {
        // Проверка, чтобы избежать одновременных запросов
        if (isLoading) {
            return;
        }

        isLoading = true;


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://194.87.199.70/api/chats/?limit=20&offset="+ currentPage)
                .get()
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("mymessage", "chats not found");
                isLoading = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isLoading = false;
                currentPage++;
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    if (responseBody.contains("не найдено")) return;
                    List<ChatObj> newChats = parseChatListFromJson(responseBody);

                    int firstVisibleItem = chatsListView.getFirstVisiblePosition();
                    View v = chatsListView.getChildAt(0);
                    currentTop = (v == null) ? 0 : (v.getTop() - chatsListView.getPaddingTop());

                    chatList.addAll(newChats);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatAdapter adapter = new ChatAdapter(getContext(), chatList);
                            chatsListView.setAdapter(adapter);

                            // Восстанавливаем позицию после обновления данных
                            chatsListView.setSelectionFromTop(firstVisibleItem, currentTop);

                            chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Call the function to open the chat activity
                                    openChatActivity(position);
                                }
                            });

                            chatsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Call the function to show the chat options dialog
                                    showChatOptionsDialog(position);
                                    return true;
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void openChatActivity(int position) {
        // Retrieve the chat item from the list based on the position
        ChatObj selectedChat = chatList.get(position);

        // Assuming you have a ChatActivity class, you need to replace it with the actual class name
        Intent intent = new Intent(getContext(), InChatActivity.class);

        // Pass necessary data to the ChatActivity
        intent.putExtra("token", token);
        intent.putExtra("user_id", userId);
        intent.putExtra("chatId", selectedChat.getId()); // Assuming you have a method getId() in ChatObj

        // Start the ChatActivity
        startActivity(intent);
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
                        } else {
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
                                } else {
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
                                } else {
                                }

                            }
                        });

                        break;
                }
            }
        });

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







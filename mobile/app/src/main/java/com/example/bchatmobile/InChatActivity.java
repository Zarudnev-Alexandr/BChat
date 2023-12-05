package com.example.bchatmobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;

public class InChatActivity extends AppCompatActivity {

    private WebSocket webSocket;

    private ListView messagesListView;
    private List<MessageObj> messageList = new ArrayList<>();

    private AlertDialog alertDialog;

    private Boolean emptyResponse = false;

    private Integer limit = 20;

    private List<UserObj> userList;

    private Integer currentPage = 0;

    private boolean isLoading = false; // флаг, чтобы избежать одновременных запросов


    private Integer userId;
    private String token;

    private MessageAdapter adapter;

    private Integer chat_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_chat_fragment);

        messagesListView = findViewById(R.id.messagesList);

        EditText textField = findViewById(R.id.editTextText);

        token = getIntent().getStringExtra("token");
        userId = getIntent().getIntExtra("user_id", 0);
        chat_id = getIntent().getIntExtra("chatId", 0);

        Log.d("mymessage", "userid: " + userId);

        initializeWebSocket();

        Button button = findViewById(R.id.button5);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textToSend = textField.getText().toString();

                textField.setText("");

                if (!textToSend.isEmpty()) {
                    sendMessage(textToSend);
                }
            }
        });

        getAllChatUsers(chat_id);

        adapter = new MessageAdapter(InChatActivity.this, messageList, userId);

        messagesListView.setAdapter(adapter);

        fetchMessages(chat_id);
        messagesListView.setSelection(limit-1);

        messagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Не требуется в данном случае
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Проверяем, что пользователь прокрутил до конца списка
                if (firstVisibleItem == 0) {
                    // Загружаем следующую страницу данных
                    int prev = adapter.getCount();
                    fetchMessages(chat_id);
                    if (!emptyResponse){
                        Log.d("mymessage","set next");
                        messagesListView.setSelection(limit);
                    }
                }
            }

        });


    }

    private void initializeWebSocket() {
        WebSocketExample webSocketExample = new WebSocketExample(token, chat_id);
        webSocketExample.setWebSocketListener(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("mymessage", "WebSocket connection opened");

                InChatActivity.this.webSocket = webSocket;
            }


            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("mymessage", "WebSocket received message: " + text);

                if (text != null ) handleReceivedMessage(text);
            }

            private void handleReceivedMessage(String receivedMessage) {
                try {
                    JSONObject jsonMessage = new JSONObject(receivedMessage);

                    Integer sender_id = jsonMessage.getInt("sender_id");
                    String text = jsonMessage.getString("text");
                    Integer id = jsonMessage.getInt("message_id");
                    String sender = jsonMessage.getString("sender");


                    MessageObj receivedMessageObj = new MessageObj(id, text,sender_id, sender);

                    // Add the received message to the list
                    messageList.add(receivedMessageObj);

                    // Update the UI
                    updateUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void updateUI() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();

                        // Scroll to the last item in the list
                        messagesListView.smoothScrollToPosition(messageList.size() - 1);
                    }
                });
            }


            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("mymessage", "WebSocket connection closed, code=" + code + ", reason=" + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("mymessage", "WebSocket error: " + t.getMessage());
            }
        });

        webSocketExample.start();
    }

    private void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_in_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_some_button:
                showAddItemDialog();
                return true;
            case R.id.action_view_users:
                showViewUsersDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showViewUsersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_user_list_dialog, null);
        builder.setView(dialogView);

        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        titleTextView.setText("Список пользователей в чате");

        ListView userListView = dialogView.findViewById(R.id.userListViewSmall);

        UserListAdapter userListAdapter = new UserListAdapter(this, userList);
        userListView.setAdapter(userListAdapter);

        builder.setNegativeButton("Закрыть", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showAddItemDialog() {
        final AlertDialog[] alertDialog = {null};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить пользователя");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText editTextItemName = dialogView.findViewById(R.id.editTextItemName);
        CheckBox checkBoxIsChecked = dialogView.findViewById(R.id.checkBoxIsChecked);
        Button buttonAddItem = dialogView.findViewById(R.id.buttonAddItem);

        buttonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = editTextItemName.getText().toString();
                boolean isChecked = checkBoxIsChecked.isChecked();

                String admin = isChecked ? "true" : "false";

                if (itemName.isEmpty()) {
                    return;
                }

                alertDialog[0].dismiss();

                int chatId = getIntent().getIntExtra("chatId", 0);

                String userUrl = "http://194.87.199.70/api/users/" + itemName + "/";

                OkHttpClient client = new OkHttpClient();

                Request userRequest = new Request.Builder()
                        .url(userUrl)
                        .get()
                        .addHeader("token", token)
                        .build();

                client.newCall(userRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String firstResponseBody = response.body().string();


                            JsonParser parser = new JsonParser();
                            JsonObject res = parser.parse(firstResponseBody).getAsJsonObject();
                            int userId = res.has("id") && !res.get("id").isJsonNull() ? res.get("id").getAsInt() : 0;


                            if (userId == 0) {
                                return;
                            }


                            String jsonData = "{ \"chat_id\": \"" + chatId + "\", \"user_id\": \"" + userId + "\", \"is_admin\": \"" + admin + "\" }";

                            Log.d("mymessage", "addUserToChat: " + jsonData);

                            String addUrl = "http://194.87.199.70/api/chats/add/user/";

                            OkHttpClient client1 = new OkHttpClient();

                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonData);

                            Request addUserRequest = new Request.Builder()
                                    .url(addUrl)
                                    .post(requestBody)
                                    .addHeader("token", token)
                                    .build();

                            client1.newCall(addUserRequest).enqueue(new Callback() {
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
                        } else {
                            Log.e("mymessage", "Unsuccessful response: " + response.code() + " " + response.message());
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog[0].dismiss();
            }
        });

        // Show the dialog
        alertDialog[0] = builder.create();
        alertDialog[0].show();
    }


    private void fetchMessages(int chatId) {

        if (isLoading) {
            return;
        }

        isLoading = true;

        String url = String.format("http://194.87.199.70/api/messages/get/?chat_id="+chatId+"&limit="+limit+"&offset="+ currentPage);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                Log.e("mymessage", "Unsuccessful response");
                isLoading = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isLoading = false;
                currentPage++;
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if (responseBody.contains("Не обнаружен")) return;
                    if ("[]".equals(responseBody)){
                        emptyResponse = true;
                        return;
                    }
                    Log.d("mymessage", "Successful response: "+ responseBody);

                    // Парсинг нового списка сообщений
                    List<MessageObj> newMessages = parseChatListFromJson(responseBody);
                    Collections.reverse(newMessages);

                    // Добавление новых сообщений к существующему списку
                    messageList.addAll(0, newMessages);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Отрисовка обновленного списка сообщений
                            adapter.notifyDataSetChanged();
//                            messagesListView.setSelection(adapter.getCount() - 1);
                        }
                    });
                }
            }
        });
    }

    private List<MessageObj> parseChatListFromJson(String json) {
        List<MessageObj> chats = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for (JsonElement chatElement : jsonArray) {
            JsonObject chatObject = chatElement.getAsJsonObject();

            int id = chatObject.has("id") && !chatObject.get("id").isJsonNull() ? chatObject.get("id").getAsInt() : 0;
            String text = chatObject.has("text") && !chatObject.get("text").isJsonNull() ? chatObject.get("text").getAsString() : "";
            int sender_id = chatObject.has("sender_id") && !chatObject.get("sender_id").isJsonNull() ? chatObject.get("sender_id").getAsInt() : 0;
            String sender_name = chatObject.has("sender_nickname") && !chatObject.get("sender_nickname").isJsonNull() ? chatObject.get("sender_nickname").getAsString() : null;

            MessageObj chat = new MessageObj(id, text, sender_id, sender_name);
            chats.add(chat);
        }

        return chats;
    }

    private void getAllChatUsers(Integer chatId) {

        String url = "http://194.87.199.70/api/chats/" + chatId  +"/users/";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
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
                    String responseBody = response.body().string();

                    userList = parseUserListFromJson(responseBody);



                } else {
                    Log.e("mymessage", "Unsuccessful response: " + response.code() + " " + response.message());
                }
            }
        });
    }

    private List<UserObj> parseUserListFromJson(String json) {
        List<UserObj> userList = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for (JsonElement userElement : jsonArray) {
            JsonObject userObject = userElement.getAsJsonObject();

            int userId = userObject.has("id") && !userObject.get("id").isJsonNull() ? userObject.get("id").getAsInt() : 0;
            String username = userObject.has("nickname") && !userObject.get("nickname").isJsonNull() ? userObject.get("nickname").getAsString() : "";

            UserObj user = new UserObj(userId, username);
            userList.add(user);
        }

        return userList;
    }
}

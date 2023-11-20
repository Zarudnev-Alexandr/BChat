package com.example.bchatmobile;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<MessageObj> {

    private int currentUserSenderId;  // Assuming you have a way to get the current user's sender ID

    public MessageAdapter(Context context, List<MessageObj> messages, int currentUserSenderId) {
        super(context, 0, messages);
        this.currentUserSenderId = currentUserSenderId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("MessageAdapter", "getView called for position: " + position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_list_item, parent, false);
        }

        MessageObj message = getItem(position);
        LinearLayout bg = convertView.findViewById(R.id.bg);
        TextView messageText = convertView.findViewById(R.id.messageText);
        TextView messageDate = convertView.findViewById(R.id.messageDate);

        if (message != null) {

            String sender = message.getSender();

            if (sender == null) sender = String.valueOf(message.getSender_id());

            messageText.setText(message.getText());
            messageDate.setText(sender);

            // Highlight the background for messages from the current user
            if (message.getSender_id() == this.currentUserSenderId) {
                bg.setBackgroundColor(getContext().getResources().getColor(R.color.colorCurrentUserMessage));
            } else {
                // Reset the background for messages from other users
                bg.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        return convertView;
    }
}

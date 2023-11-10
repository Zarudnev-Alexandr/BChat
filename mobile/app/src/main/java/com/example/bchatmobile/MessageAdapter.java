package com.example.bchatmobile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
public class MessageAdapter extends ArrayAdapter<MessageObj> {

    public MessageAdapter(Context context, List<MessageObj> messages) {
        super(context, 0, messages);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_list_item, parent, false);
        }

        MessageObj message = getItem(position);

        TextView messageText = convertView.findViewById(R.id.messageText);
        TextView messageDate = convertView.findViewById(R.id.messageDate);

        if (message != null) {
            messageText.setText(message.getText());
            messageDate.setText(message.getDate());
        }

        return convertView;
    }
}

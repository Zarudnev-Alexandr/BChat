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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<UserObj> {


    public UserListAdapter(Context context, List<UserObj> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        }


        UserObj message = getItem(position);


        TextView messageText = convertView.findViewById(R.id.usernameTextView);

        if (message != null) {

            String sender = message.getUsername();

            messageText.setText(sender);
        }

        return convertView;
    }
}

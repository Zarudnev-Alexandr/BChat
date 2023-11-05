package com.example.bchatmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UserProfileAdapter extends ArrayAdapter<UserProfile> {
    public UserProfileAdapter(Context context, List<UserProfile> userList) {
        super(context, 0, userList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserProfile user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_profile_item, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView surnameTextView = convertView.findViewById(R.id.surnameTextView);

        nameTextView.setText(user.getName());
        surnameTextView.setText(user.getSurname());

        return convertView;
    }
}


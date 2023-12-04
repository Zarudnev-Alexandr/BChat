package com.example.bchatmobile;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;

        import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatObj> {
    public ChatAdapter(Context context, List<ChatObj> chatList) {
        super(context, 0, chatList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatObj user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.chatName);

        nameTextView.setText(user.getName());


        return convertView;
    }

    public void updateData(List<ChatObj> newData) {
        clear();
        addAll(newData);
        notifyDataSetChanged();
    }
}


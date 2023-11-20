package com.example.bchatmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MembersBootcampAdapter extends ArrayAdapter<BootcampMembers> {
    private OnExpelButtonClickListener onExpelButtonClickListener;
    private boolean isAdmin;

    public interface OnExpelButtonClickListener {
        void onExpelButtonClick(int memberId);
    }

    public MembersBootcampAdapter(Context context, List<BootcampMembers> bootcampMembersList, boolean isAdmin) {
        super(context, 0, bootcampMembersList);
        this.isAdmin = isAdmin;
    }

    public void setOnExpelButtonClickListener(OnExpelButtonClickListener listener) {
        this.onExpelButtonClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BootcampMembers bootcampMember = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.members_list_item, parent, false);
        }

        TextView roleTextView = convertView.findViewById(R.id.roleTextView);
        TextView nicknameTextView = convertView.findViewById(R.id.nicknameTextView);
        Button expelButton = convertView.findViewById(R.id.expelButton);

        roleTextView.setText("Роль: " + bootcampMember.getRole());
        nicknameTextView.setText("Никнейм: " + bootcampMember.getNickname());

        if (isAdmin) {
            expelButton.setVisibility(View.VISIBLE);
            expelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onExpelButtonClickListener != null) {
                        onExpelButtonClickListener.onExpelButtonClick(bootcampMember.getApplicationId());
                    }
                }
            });
        } else {
            expelButton.setVisibility(View.GONE);
        }


        return convertView;
    }
}


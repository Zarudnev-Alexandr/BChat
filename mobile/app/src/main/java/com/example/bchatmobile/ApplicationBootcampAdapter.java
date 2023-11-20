package com.example.bchatmobile;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ApplicationBootcampAdapter extends ArrayAdapter<BootcampApplication> {
    private OnActionClickListener onActionClickListener;


    public interface OnActionClickListener {
        void onAcceptClick(int applicationId,int bootcampId);
        void onRejectClick(int applicationId,int bootcampId);
    }

    public ApplicationBootcampAdapter(Context context, List<BootcampApplication> BootcampApplicationList,OnActionClickListener listener) {
        super(context, 0, BootcampApplicationList);
        this.onActionClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BootcampApplication bootcampApplication = getItem(position);

        if (!"ожидание".equals(bootcampApplication.getRole())) {
            return new View(getContext());
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.application_list_item, parent, false);
        }

        TextView textTextView = convertView.findViewById(R.id.textTextView);
        TextView roleTextView = convertView.findViewById(R.id.roleTextView);
        Button acceptButton = convertView.findViewById(R.id.acceptButton);
        Button rejectButton = convertView.findViewById(R.id.rejectButton);

        textTextView.setText("Текст: " + bootcampApplication.getText());
        roleTextView.setText("Роль: " + bootcampApplication.getRole());

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionClickListener != null) {
                    onActionClickListener.onAcceptClick(bootcampApplication.getId(),bootcampApplication.getBootcampId());
                }
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionClickListener != null) {
                    onActionClickListener.onRejectClick(bootcampApplication.getId(),bootcampApplication.getBootcampId());
                }
            }
        });

        return convertView;
    }
}



package com.example.bchatmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BootcampAdaper extends ArrayAdapter<Bootcamp> {
    private OnApplyButtonClickListener onApplyButtonClickListener;

    public interface OnApplyButtonClickListener {
        void onApplyButtonClick(int bootcampId);
    }

    public BootcampAdaper(Context context, List<Bootcamp> BootcampList, OnApplyButtonClickListener listener) {
        super(context, 0, BootcampList);
        this.onApplyButtonClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bootcamp bootcamp = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bootcamp_prof_item, parent, false);
        }

        TextView budgetTextView = convertView.findViewById(R.id.budgetTextView);
        TextView membersCountTextView = convertView.findViewById(R.id.membersCountTextView);
        TextView addressTextView = convertView.findViewById(R.id.addressTextView);
        TextView startTimeTextView = convertView.findViewById(R.id.startTimeTextView);
        TextView endTimeTextView = convertView.findViewById(R.id.endTimeTextView);
        TextView descriptionTextView = convertView.findViewById(R.id.descriptionTextView);

        Button applyButton = convertView.findViewById(R.id.applyButton);

        budgetTextView.setText("Бюджет: " + bootcamp.getBudget());
        membersCountTextView.setText("Количество участников: " + bootcamp.getMembers_count());
        addressTextView.setText("Адрес: " + bootcamp.getAddress());

        String dateTimeString = bootcamp.getStart_time();
        String[] dateTimeParts = dateTimeString.split("T");
        String date = dateTimeParts[0];
        String time = dateTimeParts[1].replace("Z", "");
        time = time.replaceFirst("^0+(?!$)", "");
        String formattedStartTime = date + " " + time;

        startTimeTextView.setText("Начало: " + formattedStartTime);

        String dateTimeEndString = bootcamp.getEnd_time();
        String[] dateTimeEndParts = dateTimeEndString.split("T");
        String dateEnd = dateTimeEndParts[0];
        String timeEnd = dateTimeEndParts[1].replace("Z", "");
        timeEnd = timeEnd.replaceFirst("^0+(?!$)", "");
        String formattedEndTime = dateEnd + " " + timeEnd;

        endTimeTextView.setText("Окончание: " + formattedEndTime);
        descriptionTextView.setText("Описание: " + bootcamp.getDescription());

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onApplyButtonClickListener != null) {
                    onApplyButtonClickListener.onApplyButtonClick(bootcamp.getId());
                }
            }
        });

        return convertView;
    }


}


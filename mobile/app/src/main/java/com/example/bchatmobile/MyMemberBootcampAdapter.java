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

public class MyMemberBootcampAdapter extends ArrayAdapter<Bootcamp> {
    private OnViewApplicantButtonClickListener onViewApplicantButtonButtonClickListener;
    private OnExitBootcampButtonClickListener onExitBootcampButtonClickListener;

    public interface OnViewApplicantButtonClickListener {
        void onApplicantButtonClick(int bootcampId);
    }

    public interface OnExitBootcampButtonClickListener {
        void onExitBootcampButtonClick(int bootcampId);
    }

    public MyMemberBootcampAdapter(Context context, List<Bootcamp> BootcampList, OnViewApplicantButtonClickListener onViewApplicantButtonButtonClickListener, OnExitBootcampButtonClickListener onExitBootcampButtonClickListener) {
        super(context, 0, BootcampList);
        this.onViewApplicantButtonButtonClickListener = onViewApplicantButtonButtonClickListener;
        this.onExitBootcampButtonClickListener = onExitBootcampButtonClickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bootcamp bootcamp = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_bootcamp_member_item, parent, false);
        }

        TextView budgetTextView = convertView.findViewById(R.id.budgetTextView);
        TextView membersCountTextView = convertView.findViewById(R.id.membersCountTextView);
        TextView addressTextView = convertView.findViewById(R.id.addressTextView);
        TextView startTimeTextView = convertView.findViewById(R.id.startTimeTextView);
        TextView endTimeTextView = convertView.findViewById(R.id.endTimeTextView);
        TextView descriptionTextView = convertView.findViewById(R.id.descriptionTextView);

        Button viewApplicantButton = convertView.findViewById(R.id.viewApplicantButton);
        Button exitBootcampButton = convertView.findViewById(R.id.ExitBootcampButton);


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

        viewApplicantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewApplicantButtonButtonClickListener != null) {
                    onViewApplicantButtonButtonClickListener.onApplicantButtonClick(bootcamp.getId());
                }
            }
        });

        exitBootcampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onExitBootcampButtonClickListener != null) {
                    onExitBootcampButtonClickListener.onExitBootcampButtonClick(bootcamp.getId());
                }
            }
        });

        return convertView;
    }


}


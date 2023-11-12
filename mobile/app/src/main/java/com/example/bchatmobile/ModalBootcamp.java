package com.example.bchatmobile;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import androidx.annotation.NonNull;
import android.widget.TimePicker;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import androidx.fragment.app.DialogFragment;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ModalBootcamp extends DialogFragment{

    private EditText addressEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText budgetEditText;
    private EditText membersCountEditText;
    private EditText descriptionEditText;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.modal_bootcamp);

        addressEditText = dialog.findViewById(R.id.addressEditText);
        startDateEditText = dialog.findViewById(R.id.startDateEditText);
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });
        endDateEditText = dialog.findViewById(R.id.endDateEditText);
        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });

        budgetEditText = dialog.findViewById(R.id.budgetEditText);
        membersCountEditText = dialog.findViewById(R.id.membersCountEditText);
        descriptionEditText = dialog.findViewById(R.id.descriptionEditText);



        Button shakeButton = dialog.findViewById(R.id.shakeButton);
        shakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
                dialog.dismiss();
            }
        });

        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        showTimePickerDialog(isStartDate, selectedDate);
                    }
                },

                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void showTimePickerDialog(final boolean isStartDate, final Calendar selectedDate) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);


                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
                        String selectedDateTime = inputFormat.format(selectedDate.getTime());

                        if (isStartDate) {
                            startDateEditText.setText(selectedDateTime);
                        } else {
                            endDateEditText.setText(selectedDateTime);
                        }
                    }
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }


    public void sendData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Log.d("CHTO", token);
        String address = addressEditText.getText().toString();
        int geoposition_longitude = 0;
        int geoposition_latitude = 0;
        int budget = Integer.parseInt(budgetEditText.getText().toString());
        int members_count = Integer.parseInt(membersCountEditText.getText().toString());
        String start_time = startDateEditText.getText().toString();
        String formattedDate1 = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
            Date date = inputFormat.parse(start_time);

            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));

            formattedDate1 = iso8601Format.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        String end_time = endDateEditText.getText().toString();
        String formattedDate2 = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
            Date date = inputFormat.parse(end_time);

            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));

            formattedDate2 = iso8601Format.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        String description = descriptionEditText.getText().toString();

        JSONObject postData = new JSONObject();
        try {
            postData.put("address", address);
            postData.put("geoposition_longitude", geoposition_longitude);
            postData.put("geoposition_latitude", geoposition_latitude);
            postData.put("start_time", formattedDate1);
            postData.put("end_time", formattedDate2);
            postData.put("budget", budget);
            postData.put("members_count", members_count);
            postData.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http:/194.87.199.70/api/bootcamps/";

        RequestBody requestBody = RequestBody.create(postData.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
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
                    Log.d("SPERMA", "Zalupa");
                    String responseBody = response.body().string();
                    Log.d("GOLOVAchlena", responseBody);
                } else {
                    Log.d("SPERMA1", "Zalupa1");
                    String responseBody = response.body().string();
                    Log.d("ERROR_RESPONSE", responseBody);
                }
            }
        });

    }

}

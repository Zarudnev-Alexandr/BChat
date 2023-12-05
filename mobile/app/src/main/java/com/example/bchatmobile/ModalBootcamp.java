package com.example.bchatmobile;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import androidx.annotation.NonNull;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import androidx.core.content.ContextCompat;
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

    private EditText regionEditText;
    private EditText localityEditText;
    private EditText streetEditText;
    private EditText houseEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText budgetEditText;
    private EditText membersCountEditText;
    private EditText descriptionEditText;
    private EditText visiableAdresEditText;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.modal_bootcamp);

        regionEditText = dialog.findViewById(R.id.regionEditText);
        localityEditText = dialog.findViewById(R.id.localityEditText);
        streetEditText = dialog.findViewById(R.id.streetEditText);
        houseEditText = dialog.findViewById(R.id.houseEditText);
        visiableAdresEditText = dialog.findViewById(R.id.VisibleAddressText);
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
        String region = regionEditText.getText().toString();
        String locality = localityEditText.getText().toString();
        String street = streetEditText.getText().toString();
        String house = houseEditText.getText().toString();
        String visible_address = visiableAdresEditText.getText().toString();
        if (region.isEmpty() || locality.isEmpty() || street.isEmpty() || house.isEmpty() || visible_address.isEmpty()) {
            highlightEmptyField(region, regionEditText);
            highlightEmptyField(locality, localityEditText);
            highlightEmptyField(street, streetEditText);
            highlightEmptyField(house, houseEditText);
            highlightEmptyField(visible_address, visiableAdresEditText);

            return;
        }
        String address = region + " обл, " + "г." + locality + ", " + "ул." +  street + ", " + "д." + house;
        double geoposition_longitude = 0;
        double geoposition_latitude = 0;
        String start_time = startDateEditText.getText().toString();
        if (start_time.isEmpty()) {
            highlightEmptyField(start_time, startDateEditText);
            return;
        }
        String formattedDate1 = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
            Date date = inputFormat.parse(start_time);

            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));

            formattedDate1 = iso8601Format.format(date);

        } catch (ParseException e) {
            highlightErrorField("Выбери дату", startDateEditText);
            return;
        }

        String end_time = endDateEditText.getText().toString();
        if (end_time.isEmpty()) {
            highlightEmptyField(end_time, endDateEditText);
            return;
        }
        String formattedDate2 = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
            Date date = inputFormat.parse(end_time);

            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));

            formattedDate2 = iso8601Format.format(date);

        } catch (ParseException e) {
            highlightErrorField("Выбери дату", endDateEditText);
            return;
        }
        String budgetText = budgetEditText.getText().toString();
        int budget = 0;
        if (!budgetText.isEmpty()) {
            try {
                budget = Integer.parseInt(budgetText);
            } catch (NumberFormatException e) {
                highlightErrorField("Введите число", budgetEditText);
                return;
            }
        }
        else{
            highlightEmptyField(budgetText, budgetEditText);
            return;
        }

        String membersCountText = membersCountEditText.getText().toString();
        int members_count = 0;
        if (!membersCountText.isEmpty()) {
            try {
                members_count = Integer.parseInt(membersCountText);
            } catch (NumberFormatException e) {
                highlightErrorField("Введите число", membersCountEditText);
                return;
            }
        }
        else{
            highlightEmptyField(membersCountText, membersCountEditText);
            return;
        }
        String description = descriptionEditText.getText().toString();

        JSONObject postData = new JSONObject();
        try {
            postData.put("address", address);
            postData.put("visible_address", visible_address);
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
        Log.d("govno",postData.toString() );
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
                    dismiss();
                    String responseBody = response.body().string();
                    Log.d("Xyuatna", responseBody);
                } else {
                    String responseBody = response.body().string();
                    Log.d("Xyuatna1", responseBody);
                }
            }
        });

    }

    private void highlightEmptyField(String fieldValue, EditText editText) {
        if (fieldValue.isEmpty()) {
            // Поле пустое, подсвечиваем его красным цветом
            editText.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            // Устанавливаем слушатель текста для убирания подсветки при следующем вводе
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // Ничего не делаем
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // При изменении текста убираем подсветку
                    editText.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    // Убираем слушатель, чтобы не вызывать его повторно
                    editText.removeTextChangedListener(this);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Ничего не делаем
                }
            });
        }
    }

    private void highlightErrorField(String errorMessage, EditText editText) {
        // Задаем красный цвет для подсветки
        int redColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);

        // Задаем красный цвет для рамки поля ввода
        editText.getBackground().mutate().setColorFilter(redColor, PorterDuff.Mode.SRC_ATOP);

        // Показываем всплывающую подсказку с сообщением об ошибке
        editText.setError(errorMessage);

        // Слушатель для удаления подсветки при вводе данных
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Убираем подсветку и сообщение об ошибке при изменении текста
                editText.getBackground().mutate().setColorFilter(null);
                editText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

}

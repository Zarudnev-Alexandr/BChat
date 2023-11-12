package com.example.bchatmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplyModalFragment extends DialogFragment {

    private static final String ARG_BOOTCAMP_ID = "bootcampId";

    private EditText applyEditText;
    private Button okButton;
    private Button cancelButton;

    public ApplyModalFragment() {
    }

    public static ApplyModalFragment newInstance(int bootcampId) {
        ApplyModalFragment fragment = new ApplyModalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOTCAMP_ID, bootcampId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_apply_modal, null);
        builder.setView(view);

        applyEditText = view.findViewById(R.id.applyEditText);
        okButton = view.findViewById(R.id.shakeButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        int bootcampId = getArguments().getInt(ARG_BOOTCAMP_ID);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String applyText = applyEditText.getText().toString();
                sendData(bootcampId, applyText);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }

    private void sendData(int bootcampId, String applyText) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        String baseUrl = "http://194.87.199.70/api/bootcamps/";
        String url = baseUrl + bootcampId + "/apply/" + "?text=" + applyText;
        Log.d("Bebra1", url);

        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("text", applyText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String requestBodyString = jsonBody.toString();

        RequestBody requestBody = RequestBody.create(requestBodyString, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    dismiss();
                } else {
                    String responseBody = response.body().string();
                    if (isAdded()) {
                        try {
                            JSONObject originalJson = new JSONObject(responseBody);
                            final String detailValue = originalJson.getString("detail");
                            new Handler(requireActivity().getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                    builder.setMessage(detailValue)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dismiss();
                                                }
                                            });
                                    builder.create().show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
        });
    }

}


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
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplicationModalFragment extends DialogFragment {

    private static final String ARG_BOOTCAMP_ID = "bootcampId";

    private Button cancelButton;
    private List<BootcampApplication> BootcampApplicationList = new ArrayList<>();
    private ListView BootcampApplicationListView;


    public ApplicationModalFragment() {
    }

    public static ApplicationModalFragment newInstance(int bootcampId) {
        ApplicationModalFragment fragment = new ApplicationModalFragment();
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
        View view = inflater.inflate(R.layout.modal_all_application, null);
        builder.setView(view);

        BootcampApplicationListView = view.findViewById(R.id.ApplicationListView);

        cancelButton = view.findViewById(R.id.closeApplicationButton);

        int bootcampId = getArguments().getInt(ARG_BOOTCAMP_ID);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        fetchAllApplicationBootcamp(bootcampId);

        return builder.create();
    }

    private void fetchAllApplicationBootcamp(int bootcampId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String userIdString = sharedPreferences.getString("id", "");
        int userId = Integer.parseInt(userIdString);

        String baseUrl = "http://194.87.199.70/api/bootcamps/";
        String url = baseUrl + bootcampId + "/applications/";
        Log.d("Bebra1", url);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    BootcampApplicationList = parseBootcampApplicationsListFromJson(responseBody);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ApplicationBootcampAdapter adapter = new ApplicationBootcampAdapter(getContext(), BootcampApplicationList, new ApplicationBootcampAdapter.OnActionClickListener() {
                                @Override
                                public void onAcceptClick(int ApplicationsId,int BootcampId) {
                                    fetchChangeStatusAccept(ApplicationsId, BootcampId);
                                }

                                @Override
                                public void onRejectClick(int ApplicationsId,int BootcampId) {
                                    fetchChangeStatusReject(ApplicationsId, BootcampId);
                                }
                            }
                            );
                            BootcampApplicationListView.setAdapter(adapter);

                        }
                    });

                } else {

                }
            }
        });
    }

    private List<BootcampApplication> parseBootcampApplicationsListFromJson(String json) {
        List<BootcampApplication> bootcampApplicationsList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject bootcampApplicationObject = jsonArray.getJSONObject(i);

                String text = bootcampApplicationObject.optString("text", "");
                String role = bootcampApplicationObject.optString("role", "");
                int id = bootcampApplicationObject.has("id") ? bootcampApplicationObject.getInt("id") : 0;
                int bootcampId = bootcampApplicationObject.has("bootcamp_id") ? bootcampApplicationObject.getInt("bootcamp_id") : 0;
                int userId = bootcampApplicationObject.has("user_id") ? bootcampApplicationObject.getInt("user_id") : 0;

                BootcampApplication bootcampApplication = new BootcampApplication(text, role, id, bootcampId, userId);
                bootcampApplicationsList.add(bootcampApplication);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bootcampApplicationsList;
    }

    private static String buildChangeStatusUrl(String baseUrl, int bootcampId, int applicationsId, String status) {
        try {
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("/")
                    .append(bootcampId)
                    .append("/applications/")
                    .append(applicationsId)
                    .append("/?status=")
                    .append(URLEncoder.encode(status, StandardCharsets.UTF_8.toString()));

            return urlBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return baseUrl;
        }
    }

    private void fetchChangeStatusAccept(int ApplicationsId,int BootcampId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");


        String baseUrl = "http://194.87.199.70/api/bootcamps";
        String changeStatusUrl = buildChangeStatusUrl(baseUrl, BootcampId, ApplicationsId, "участник");


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(changeStatusUrl)
                .addHeader("token", token)
                .put(RequestBody.create("", null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                } else {
                    String responseBody = response.body().string();
                }
            }
        });
    }

    private void fetchChangeStatusReject(int ApplicationsId,int BootcampId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");


        String baseUrl = "http://194.87.199.70/api/bootcamps";
        String changeStatusUrl = buildChangeStatusUrl(baseUrl, BootcampId, ApplicationsId, "отклонено");

        OkHttpClient client = new OkHttpClient();



        Request request = new Request.Builder()
                .url(changeStatusUrl)
                .addHeader("token", token)
                .put(RequestBody.create("", null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                } else {

                }
            }
        });
    }


}

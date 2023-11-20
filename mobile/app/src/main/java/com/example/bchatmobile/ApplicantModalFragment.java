package com.example.bchatmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplicantModalFragment extends DialogFragment {

    private static final String ARG_BOOTCAMP_ID = "bootcampId";

    private Button cancelButton;
    private List<BootcampMembers> BootcampMemberList = new ArrayList<>();
    private ListView BootcampMembersListView;


    public ApplicantModalFragment() {
    }

    public static ApplicantModalFragment newInstance(int bootcampId) {
        ApplicantModalFragment fragment = new ApplicantModalFragment();
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
        View view = inflater.inflate(R.layout.modal_all_aplicant, null);
        builder.setView(view);

        BootcampMembersListView = view.findViewById(R.id.participantsListView);

        cancelButton = view.findViewById(R.id.closeButton);

        int bootcampId = getArguments().getInt(ARG_BOOTCAMP_ID);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        fetchAllMembersBootcamp(bootcampId);

        return builder.create();
    }

    private void fetchAllMembersBootcamp(int bootcampId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String userIdString = sharedPreferences.getString("id", "");
        int userId = Integer.parseInt(userIdString);

        String baseUrl = "http://194.87.199.70/api/bootcamps/";
        String url = baseUrl + bootcampId + "/members/";


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
                    BootcampMemberList = parseBootcampMembersListFromJson(responseBody);
                    boolean isAdmin = false;

                    for (BootcampMembers member : BootcampMemberList) {
                        if (member.getId() == userId && "админ".equals(member.getRole())) {
                            isAdmin = true;
                            break;
                        }
                    }

                    boolean finalIsAdmin = isAdmin;
                    int finalId = bootcampId;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MembersBootcampAdapter adapter = new MembersBootcampAdapter(getContext(), BootcampMemberList, finalIsAdmin);
                            adapter.setOnExpelButtonClickListener(new MembersBootcampAdapter.OnExpelButtonClickListener() {
                                @Override
                                public void onExpelButtonClick(int applicationId) {
                                       fetchExpelMember(applicationId, finalId);
                                }
                            });
                            BootcampMembersListView.setAdapter(adapter);

                        }
                    });

                } else {

                }
            }
        });
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

    private void fetchExpelMember(int ApplicationsId,int BootcampId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");


        String baseUrl = "http://194.87.199.70/api/bootcamps";
        String changeStatusUrl = buildChangeStatusUrl(baseUrl, BootcampId, ApplicationsId, "отклонено");
        Log.d("Bebra1", changeStatusUrl);

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

    private List<BootcampMembers> parseBootcampMembersListFromJson(String json) {
        List<BootcampMembers> bootcampMembersList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject bootcampMemberObject = jsonArray.getJSONObject(i);

                String role = bootcampMemberObject.optString("role", "");
                String nickname = bootcampMemberObject.optString("nickname", "");
                int id = bootcampMemberObject.has("id") ? bootcampMemberObject.getInt("id") : 0;
                int ApplicationId = bootcampMemberObject.has("application_id") ? bootcampMemberObject.getInt("application_id") : 0;

                BootcampMembers bootcampMember = new BootcampMembers(role, nickname, id, ApplicationId);
                bootcampMembersList.add(bootcampMember);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bootcampMembersList;
    }


}

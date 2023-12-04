package com.example.bchatmobile;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;


public class BootcampFragment extends Fragment {

    private Button addButton;
    private Button AllBootcampButton;
    private Button MyBootcampButton;
    private Button adminBootcampsButton;
    private Button participantBootcampsButton;
    private ListView BootcampListView;
    private List<Bootcamp> BootcampList = new ArrayList<>();
    private int initialMarginTop;
    private boolean marginTopIncreased = false;
    private boolean marginTopDecreased = false;
    private LocationManager locationManager;
    private boolean isFetchingData = false;
    private boolean oneRec = false;
    private boolean onlyAllBootcamp = false;
    private int currentPage = 0;
    private static final int LIMIT = 5;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_bootcamp_fragment, container, false);

        BootcampListView = view.findViewById(R.id.BootcampListView);

        addButton = view.findViewById(R.id.addButton);

        AllBootcampButton = view.findViewById(R.id.AllBootcampButton);

        MyBootcampButton = view.findViewById(R.id.MyBootcampButton);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }


        adminBootcampsButton = view.findViewById(R.id.adminBootcampsButton);
        participantBootcampsButton = view.findViewById(R.id.participantBootcampsButton);

        initialMarginTop = ((ViewGroup.MarginLayoutParams) BootcampListView.getLayoutParams()).topMargin;


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openModal();
            }
        });

        AllBootcampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyAllBootcamp = false;
                currentPage = 0;
                BootcampList.clear();
                BootcampListView.setAdapter(null);
                adminBootcampsButton.setVisibility(View.INVISIBLE);
                participantBootcampsButton.setVisibility(View.INVISIBLE);
                fetchBootCampList();
                decreaseMarginTop();
            }
        });

        MyBootcampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyAllBootcamp = true;
                BootcampList.clear();
                BootcampListView.setAdapter(null);
                fetchMyAdminBootCampList();
                adminBootcampsButton.setVisibility(View.VISIBLE);
                participantBootcampsButton.setVisibility(View.VISIBLE);
                increaseMarginTop();
            }
        });

        adminBootcampsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyAllBootcamp = true;
                BootcampListView.setAdapter(null);
                fetchMyAdminBootCampList();
            }
        });

        participantBootcampsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyAllBootcamp = true;
                BootcampListView.setAdapter(null);
                fetchMyMembersBootCampList();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BootcampListView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (!isFetchingData) {
                        int totalItemCount = BootcampListView.getCount();
                        int lastVisibleItem = BootcampListView.getLastVisiblePosition();

                        if (lastVisibleItem == totalItemCount - 1 && !oneRec && !onlyAllBootcamp) {
                            oneRec = true;
                            Log.d("zzxc", "xzc");
                            fetchBootCampList();
                        } else {
                            oneRec = false;
                        }
                    }
                }
            });
        }

        return view;
    }

    private void startLocationUpdates() {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("userLongitude", (int) (loc.getLongitude() * 1E6));
                editor.putInt("userLatitude", (int) (loc.getLatitude() * 1E6));
                editor.apply();

                // locationManager.removeUpdates(this);
            }

            @Override
            public void onProviderDisabled(String provider) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Для получения местоположения необходимо включить геолокацию. Включить сейчас?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("xxxyyyuu", "fdg");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
        }

    }

    private void decreaseMarginTop() {
        if (!marginTopDecreased) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) BootcampListView.getLayoutParams();
            int newMarginTop = params.topMargin - getResources().getDimensionPixelSize(R.dimen.margin_increment);
            params.topMargin = newMarginTop;
            BootcampListView.setLayoutParams(params);

            marginTopDecreased = true;
            marginTopIncreased = false;
        }
    }


    private void increaseMarginTop() {
        if (!marginTopIncreased) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) BootcampListView.getLayoutParams();
            int newMarginTop = params.topMargin + getResources().getDimensionPixelSize(R.dimen.margin_increment);
            params.topMargin = newMarginTop;
            BootcampListView.setLayoutParams(params);

            marginTopIncreased = true;
            marginTopDecreased = false;
        }
    }

    private void openModal() {
        ModalBootcamp dialogFragment = new ModalBootcamp();
        dialogFragment.show(getFragmentManager(), "AddDataDialogFragment");
    }

    private static String buildUrl(String baseUrl, double userLongitude, double userLatitude, int limit, int offset) {
        try {
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?user_longitude=").append(URLEncoder.encode(String.valueOf(userLongitude), "UTF-8"))
                    .append("&user_latitude=").append(URLEncoder.encode(String.valueOf(userLatitude), "UTF-8"))
                    .append("&limit=").append(URLEncoder.encode(String.valueOf(limit), "UTF-8"))
                    .append("&offset=").append(URLEncoder.encode(String.valueOf(offset), "UTF-8"));

            return urlBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return baseUrl;
        }
    }

    private void fetchMyAdminBootCampList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String baseUrl = "http://194.87.199.70/api/bootcamps/admin";
        int userLongitude = sharedPreferences.getInt("userLongitude", 0);
        int userLatitude = sharedPreferences.getInt("userLatitude", 0);
        Log.d("xcx", String.valueOf(userLongitude));
        Log.d("xcx", String.valueOf(userLatitude));
        double userLongitudeDegrees = userLongitude / 1E6;
        double userLatitudeDegrees = userLatitude / 1E6;
        int limit = 100;
        int offset = 0;

        String url = buildUrl(baseUrl, userLongitudeDegrees, userLatitudeDegrees, limit, offset);

        Log.d("xcx", url);


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
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
                    BootcampList = parseBootcampListFromJson(responseBody);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyBootcampAdapter adapter = new MyBootcampAdapter(getContext(), BootcampList, new MyBootcampAdapter.OnViewApplicantButtonClickListener() {
                                @Override
                                public void onApplicantButtonClick(int bootcampId) {
                                    openApplicantModal(bootcampId);
                                }
                            },  new MyBootcampAdapter.OnViewApplicationsButtonClickListener() {
                                @Override
                                public void onViewApplicationsButtonClick(int bootcampId) {
                                    openApplicationsModal(bootcampId);
                                }
                            });

                            BootcampListView.setAdapter(adapter);

                        }
                    });
                }
            }
        });


    }

    private void fetchMyMembersBootCampList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String baseUrl = "http://194.87.199.70/api/bootcamps/member";
        int userLongitude = sharedPreferences.getInt("userLongitude", 0);
        int userLatitude = sharedPreferences.getInt("userLatitude", 0);
        double userLongitudeDegrees = userLongitude / 1E6;
        double userLatitudeDegrees = userLatitude / 1E6;
        int limit = 100;
        int offset = 0;

        String url = buildUrl(baseUrl, userLongitudeDegrees, userLatitudeDegrees, limit, offset);
        Log.d("ggg",url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
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
                    BootcampList = parseBootcampListFromJson(responseBody);
                    Log.d("cvb", responseBody);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyMemberBootcampAdapter adapter = new MyMemberBootcampAdapter(getContext(), BootcampList, new MyMemberBootcampAdapter.OnViewApplicantButtonClickListener() {
                                @Override
                                public void onApplicantButtonClick(int bootcampId) {
                                    openApplicantModal(bootcampId);
                                }
                            }, new MyMemberBootcampAdapter.OnExitBootcampButtonClickListener() {
                                @Override
                                public void onExitBootcampButtonClick(int bootcampId) {
                                    fetchExitBootcamp(bootcampId);
                                }
                            });

                            BootcampListView.setAdapter(adapter);

                        }
                    });
                }
                else{
                    String responseBody = response.body().string();
                    Log.d("cv1", responseBody);
                }
            }
        });


    }


    private void fetchExitBootcamp(int bootcampId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String baseUrl = "http://194.87.199.70/api/bootcamps/leave/";

        String url = baseUrl + bootcampId + "/";


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("X", "Успех");
                }
            }
        });


    }




    private void fetchBootCampList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String baseUrl = "http://194.87.199.70/api/bootcamps";
        int userLongitude = sharedPreferences.getInt("userLongitude", 0);
        int userLatitude = sharedPreferences.getInt("userLatitude", 0);
        double userLongitudeDegrees = userLongitude / 1E6;
        double userLatitudeDegrees = userLatitude / 1E6;
        String url = buildUrl(baseUrl, userLongitudeDegrees, userLatitudeDegrees, LIMIT, currentPage);
        Log.d("scroll", url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("token", token)
                .build();

        isFetchingData = true;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isFetchingData = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isFetchingData = false;

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    List<Bootcamp> newBootcampList = parseBootcampListFromJson(responseBody);

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (newBootcampList.isEmpty()) {
                                Toast.makeText(getContext(), "Больше нет данных", Toast.LENGTH_SHORT).show();
                            } else {
                                int currentVisiblePosition = BootcampListView.getFirstVisiblePosition();
                                View v = BootcampListView.getChildAt(0);
                                int currentTop = (v == null) ? 0 : (v.getTop() - BootcampListView.getPaddingTop());
                                BootcampList.addAll(newBootcampList);
                                BootcampAdaper adapter = new BootcampAdaper(getContext(), BootcampList, new BootcampAdaper.OnApplyButtonClickListener() {
                                    public void onApplyButtonClick(int bootcampId) {
                                        openApplyModal(bootcampId);
                                    }
                                });
                                BootcampListView.setAdapter(adapter);
                                BootcampListView.setSelectionFromTop(currentVisiblePosition, currentTop);
                                currentPage++;
                            }
                        }
                    });
                }
            }
        });
    }

    private void openApplyModal(int bootcampId) {
        ApplyModalFragment dialogFragment = ApplyModalFragment.newInstance(bootcampId);
        dialogFragment.show(getFragmentManager(), "ApplyModalFragment");
    }

    private void openApplicantModal(int bootcampId) {
        ApplicantModalFragment dialogFragment = ApplicantModalFragment.newInstance(bootcampId);
        dialogFragment.show(getFragmentManager(), "ApplicantModalFragment");
    }

    private void openApplicationsModal(int bootcampId) {
        ApplicationModalFragment dialogFragment = ApplicationModalFragment.newInstance(bootcampId);
        dialogFragment.show(getFragmentManager(), "ApplicationModalFragment");
    }



    private List<Bootcamp> parseBootcampListFromJson(String json) {
        List<Bootcamp> bootcamps = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for (JsonElement bootcampElement : jsonArray) {
            JsonObject bootcampObject = bootcampElement.getAsJsonObject();

            int id = bootcampObject.has("id") && !bootcampObject.get("id").isJsonNull() ? bootcampObject.get("id").getAsInt() : 0;
            String address = bootcampObject.has("address") && !bootcampObject.get("address").isJsonNull() ? bootcampObject.get("address").getAsString() : "";
            String visible_address = bootcampObject.has("visible_address") && !bootcampObject.get("visible_address").isJsonNull() ? bootcampObject.get("visible_address").getAsString() : "";
            int geopositionLongitude = bootcampObject.has("geoposition_longitude") && !bootcampObject.get("geoposition_longitude").isJsonNull() ? bootcampObject.get("geoposition_longitude").getAsInt() : 0;
            int geopositionLatitude = bootcampObject.has("geoposition_latitude") && !bootcampObject.get("geoposition_latitude").isJsonNull() ? bootcampObject.get("geoposition_latitude").getAsInt() : 0;
            String startTime = bootcampObject.has("start_time") && !bootcampObject.get("start_time").isJsonNull() ? bootcampObject.get("start_time").getAsString() : "";
            String endTime = bootcampObject.has("end_time") && !bootcampObject.get("end_time").isJsonNull() ? bootcampObject.get("end_time").getAsString() : "";
            int budget = bootcampObject.has("budget") && !bootcampObject.get("budget").isJsonNull() ? bootcampObject.get("budget").getAsInt() : 0;
            int membersCount = bootcampObject.has("members_count") && !bootcampObject.get("members_count").isJsonNull() ? bootcampObject.get("members_count").getAsInt() : 0;
            int current_members_count = bootcampObject.has("current_members_count") && !bootcampObject.get("current_members_count").isJsonNull() ? bootcampObject.get("current_members_count").getAsInt() : 0;
            String description = bootcampObject.has("description") && !bootcampObject.get("description").isJsonNull() ? bootcampObject.get("description").getAsString() : "";

            Bootcamp bootcamp = new Bootcamp(id, budget, membersCount, current_members_count, address, visible_address, startTime, endTime, description, geopositionLongitude, geopositionLatitude);
            bootcamps.add(bootcamp);
        }

        return bootcamps;
    }

}

package com.example.bchatmobile;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.example.bchatmobile.GetRect;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    public void onAllUsersBtnClick(View view) {

        AllUsersFragment fragment3 = new AllUsersFragment();


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment3)
                .addToBackStack(null)
                .commit();
    }

    public void onProfBtnClick(View view) {

        ProfileFragment fragment1 = new ProfileFragment();

        // Запуск транзакции фрагментов для замены текущего фрагмента фрагментом 1
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment1)
                .addToBackStack(null)  // Добавьте транзакцию в стек возврата, если необходимо
                .commit();
    }




    public void onExitButtonClick(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("id");
        editor.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}

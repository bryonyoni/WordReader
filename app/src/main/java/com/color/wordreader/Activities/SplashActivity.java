package com.color.wordreader.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.color.wordreader.R;
import com.color.wordreader.Services.DatabaseManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(new DatabaseManager(getApplicationContext()).isDarkThemeEnabled()){
            setContentView(R.layout.activity_splash_dark);
        }else{
            setContentView(R.layout.activity_splash);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }
}

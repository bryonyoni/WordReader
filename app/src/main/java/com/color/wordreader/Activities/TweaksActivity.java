package com.color.wordreader.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.color.wordreader.R;
import com.color.wordreader.Services.DatabaseManager;

import java.text.NumberFormat;
import java.util.Locale;

public class TweaksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(new DatabaseManager(getApplicationContext()).isDarkThemeEnabled()){
            setContentView(R.layout.activity_tweaks_dark);
        }else{
            setContentView(R.layout.activity_tweaks);
        }

        NumberFormat format = NumberFormat.getInstance(Locale.US);

        TextView wordCountTextView = findViewById(R.id.wordCountTextView);
        long count = new DatabaseManager(getApplicationContext()).getReadCount();
        wordCountTextView.setText(String.format("%s words", format.format(count)));


        TextView rankTextView = findViewById(R.id.rankTextView);

        if(count< 1000L){
            rankTextView.setText("Beginner.");
        }else if(count< 10000L){
            rankTextView.setText("Amature.");
        }else if(count< 100000L){
            rankTextView.setText("Pro-Amateur.");
        }else if(count< 1000000L){
            rankTextView.setText("Pro.");
        }else if(count< 10000000L){
            rankTextView.setText("Master.");
        }else if(count< 100000000L){
            rankTextView.setText("Super-Human.");
        }else if(count< 1000000000L){
            rankTextView.setText("Titan.");
        }else{
            rankTextView.setText("God.");
        }
    }
}

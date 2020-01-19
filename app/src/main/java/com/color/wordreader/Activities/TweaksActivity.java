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
        setContentView(R.layout.activity_tweaks);

        NumberFormat format = NumberFormat.getInstance(Locale.US);

        TextView wordCountTextView = findViewById(R.id.wordCountTextView);
        int count = new DatabaseManager(getApplicationContext()).getReadCount();
        wordCountTextView.setText(String.format("%s words", format.format(count)));
    }
}

package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

/** Screen 5: Settings (expiring-soon highlight toggle saved in SharedPreferences). */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        final SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        Switch swExpiring = findViewById(R.id.swExpiring);
        swExpiring.setChecked(prefs.getBoolean("highlight_expiring", true));
        swExpiring.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("highlight_expiring", checked).apply());

        NavHelper.setup(this, R.id.nav_settings);
    }
}

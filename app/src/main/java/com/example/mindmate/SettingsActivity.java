package com.example.mindmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private Spinner themeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        themeSpinner = findViewById(R.id.themeSpinner);

        // Load saved theme preference
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int savedTheme = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); // Default to system mode

        // Set up Spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.theme_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        // Set the Spinner selection based on the saved theme
        switch (savedTheme) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeSpinner.setSelection(1); // Dark Theme
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeSpinner.setSelection(0); // Light Theme
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                themeSpinner.setSelection(2); // System Default (Auto)
                break;
        }

        // Set up listener for theme selection
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; // Default to system mode

                // Apply theme based on Spinner selection
                if (position == 0) {
                    mode = AppCompatDelegate.MODE_NIGHT_NO; // Light theme
                } else if (position == 1) {
                    mode = AppCompatDelegate.MODE_NIGHT_YES; // Dark theme
                }

                // Apply the selected theme
                AppCompatDelegate.setDefaultNightMode(mode);

                // Save the selected theme in shared preferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("theme", mode);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if no selection is made
            }
        });
    }
}

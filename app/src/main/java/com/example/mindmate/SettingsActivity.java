package com.example.mindmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat darkModeSwitch; // Changed to SwitchCompat
    private boolean isDarkMode = false; // To track dark mode status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Back button setup
        ImageButton backButton = findViewById(R.id.back_button2back);
        backButton.setOnClickListener(v -> finish());

        // Initialize the switch
        darkModeSwitch = findViewById(R.id.switchDarkMode);

        // Set the switch's current status based on the saved state (in this case, it defaults to false)
        darkModeSwitch.setChecked(isDarkMode);

        // Set listener for switch change
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change background color to dark mode color (dark green in this case)
                findViewById(R.id.constraintLayout).setBackgroundColor(getResources().getColor(R.color.dark_green));
                Toast.makeText(SettingsActivity.this, "Switched to Dark Mode", Toast.LENGTH_SHORT).show();
                isDarkMode = true;
            } else {
                // Change background color to light mode color (light yellow)
                findViewById(R.id.constraintLayout).setBackgroundColor(getResources().getColor(R.color.light_yellow));
                Toast.makeText(SettingsActivity.this, "Switched to Light Mode", Toast.LENGTH_SHORT).show();
                isDarkMode = false;
            }
        });
    }
}

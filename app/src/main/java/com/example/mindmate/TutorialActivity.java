package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {

    private Button buttonCloseTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Initialize the button
        buttonCloseTutorial = findViewById(R.id.buttonCloseTutorial);

        // Set up the button's click listener
        buttonCloseTutorial.setOnClickListener(v -> {
            // Redirect to LoginActivity and finish the tutorial activity
            startActivity(new Intent(TutorialActivity.this, LoginActivity.class));
            finish();  // Close the TutorialActivity
        });
    }
}

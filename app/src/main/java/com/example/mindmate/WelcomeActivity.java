package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Fake loading delay (if needed)
        new Handler().postDelayed(() -> {
            // Check if the user is logged in
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // If user is logged in, navigate to HomeActivity
                intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                Toast.makeText(WelcomeActivity.this, "You are automatically logged in!", Toast.LENGTH_SHORT).show();

            } else {
                // If user is not logged in, navigate to LoginActivity
                intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Closes WelcomeActivity so user can't go back to it
        }, 500); // 500 milliseconds = 0.5 seconds
    }
}

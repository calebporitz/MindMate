package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SearchingActivity extends AppCompatActivity {
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);  // Ensure this is your correct layout with the button

        setupNavigation();
        setupMatchButton();

        locationHelper = new LocationHelper(this);
    }

    private void setupNavigation() {
        // Profile Button: navigates to ProfileActivity.
        ImageButton navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Chat List Button: navigates to ChatListActivity (chat overview screen).
        ImageButton navChatList = findViewById(R.id.nav_chat); // Ensure this button exists in your layout.
        navChatList.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        });
    }

    private void setupMatchButton() {
        Button matchButton = findViewById(R.id.buttonMatch);
        matchButton.setOnClickListener(v -> {
            locationHelper.requestLocation(location -> {
                if (location != null) {
                    Intent intent = new Intent(this, MatchFoundActivity.class);
                    intent.putExtra("latitude", location.getLatitude());
                    intent.putExtra("longitude", location.getLongitude());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SearchingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        setupNavigation();
    }

    private void setupNavigation() {
        // Profile Button: navigates to ProfileActivity.
        ImageButton navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // NEW: Chat List Button: navigates to ChatListActivity (chat overview screen).
        ImageButton navChatList = findViewById(R.id.nav_chat); // Ensure this button exists in your layout.
        navChatList.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        });
    }
}

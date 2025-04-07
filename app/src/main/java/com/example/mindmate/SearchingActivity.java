package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SearchingActivity extends AppCompatActivity {

    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        setupNavigation();
        locationHelper = new LocationHelper(this);
    }

    private void setupNavigation() {

        // Settings button
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // BOTTOM NAVIGATION
        // Chat List Button: navigates to ChatListActivity (chat overview).
        ImageButton navChatList = findViewById(R.id.nav_chat);
        navChatList.setOnClickListener(v -> {
            Intent intent = new Intent(SearchingActivity.this, ChatListActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Profile Button: navigates to ProfileActivity.
        ImageButton navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SearchingActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Combined Match/Direct Chat Button.
        Button matchChatButton = findViewById(R.id.buttonMatch);
        matchChatButton.setOnClickListener(v -> {

            // Ensure locationHelper has a method to get the location
            locationHelper.requestLocation(location -> {
                if (location != null) {
                    double userLatitude = location.getLatitude();
                    double userLongitude = location.getLongitude();
                    // Format the toast message with the values
                    Toast.makeText(SearchingActivity.this,
                            String.format("Looking for a match near your location:\nLatitude: %.6f, Longitude: %.6f", userLatitude, userLongitude),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SearchingActivity.this, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
                }
            });


            // Retrieve the selected course and checkbox values
            Spinner spinnerCourses = findViewById(R.id.spinnerCourses);
            String selectedCourse = spinnerCourses.getSelectedItem().toString();

            CheckBox checkBoxSameYear = findViewById(R.id.checkBoxSameYear);
            boolean isSameYear = checkBoxSameYear.isChecked();

            // Check if the conditions are met (Course = "Calculus II" and Same Year = false)
            if ("Calculus II".equals(selectedCourse) && !isSameYear) {
                // Conditions are met, proceed with matching

                // fake delay
                new Handler().postDelayed(() -> {
                    String matchedUserId = getMatchedUserId();
                    if (matchedUserId != null) {
                        // If a match is found, open ChatActivity directly.
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            Toast.makeText(SearchingActivity.this, "Not logged in", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String currentUserId = user.getUid();
                        String chatId = ChatUtils.generateChatId(currentUserId, matchedUserId);
                        Intent intent = new Intent(SearchingActivity.this, ChatActivity.class);
                        intent.putExtra("CHAT_ID", chatId);
                        intent.putExtra("OTHER_USER_ID", matchedUserId);
                        startActivity(intent);
                    } else {
                        // No match exists: perform location-based matching and launch MatchFoundActivity.
                        locationHelper.requestLocation(location -> {
                            if (location != null) {
                                Intent intent = new Intent(SearchingActivity.this, MatchFoundActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SearchingActivity.this, "Failed to retrieve location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }, 1000);
            } else {
                // Show a fake delay before showing the toast
                new Handler().postDelayed(() -> {
                    Toast.makeText(SearchingActivity.this,
                            "Couldn't find a match with current Preferences\nEither wait for longer or change Preferences",
                            Toast.LENGTH_LONG).show();
                }, 1500);
            }
        });
    }

    /**
     * For testing purposes, this method returns null to simulate no existing match.
     * In production, implement your matching logic here to return a matched user UID if available.
     */
    private String getMatchedUserId() {
        return null; // Simulate no match found so MatchFoundActivity is triggered.
        // If you want to simulate a direct match, return a hardcoded UID like:
        // return "pZmRfh37jKa9C83xinTzOl1QaDr1";
    }
}

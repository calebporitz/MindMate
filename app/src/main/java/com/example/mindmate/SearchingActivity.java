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

        // Chat Button: launches a direct chat with a hardcoded receiver (for testing).
        ImageButton navChat = findViewById(R.id.nav_chat);
        navChat.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String currentUserId = user.getUid();
            // Hardcoded receiver's UID for testing; make sure this UID exists.
            String otherUserId = "ejBQBjEKJwgqUohgqoYFpaY4J7m2";
            // Generate a unique chat ID for this one-to-one conversation.
            String chatId = ChatUtils.generateChatId(currentUserId, otherUserId);

            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CHAT_ID", chatId);
            intent.putExtra("OTHER_USER_ID", otherUserId);
            startActivity(intent);
        });

        // NEW: Chat List Button: navigates to ChatListActivity (chat overview screen).
        ImageButton navChatList = findViewById(R.id.nav_chat_list); // Ensure this button exists in your layout.
        navChatList.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        });
    }
}

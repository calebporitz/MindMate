package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MatchFoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchfound);  // Layout that shows a "Match Found!" message

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String currentUserId = user.getUid();
        // For testing, use a hardcoded matched user's UID.
        String matchedUserId = "pZmRfh37jKa9C83xinTzOl1QaDr1";
        String chatId = ChatUtils.generateChatId(currentUserId, matchedUserId);

        // Wait for 2 seconds, then launch ChatActivity.
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MatchFoundActivity.this, ChatActivity.class);
            intent.putExtra("CHAT_ID", chatId);
            intent.putExtra("OTHER_USER_ID", matchedUserId);
            startActivity(intent);
            finish();  // Close MatchFoundActivity so user cannot go back.
        }, 2000);  // 2000 milliseconds = 2 seconds
    }
}

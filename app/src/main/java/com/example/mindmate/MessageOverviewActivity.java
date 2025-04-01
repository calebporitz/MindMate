package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageOverviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageButton profileButton, homeButton, messButton;

    // For simplicity, using Message as a model for conversation previews.
    private List<Message> conversationList;
    private MessageAdapter conversationAdapter;

    // Firebase reference for conversation overviews.
    // In a complete app, you would structure and store conversation summaries separately.
    private DatabaseReference conversationsRef;

    // Replace with dynamic FirebaseAuth current user as needed.
    private final String currentUsername = "User1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_overview);

        // Find views
        recyclerView = findViewById(R.id.message_list_view);
        profileButton = findViewById(R.id.profile_button);
        homeButton = findViewById(R.id.home_button);
        messButton = findViewById(R.id.mess_button);

        // Setup RecyclerView and adapter for conversation list
        conversationList = new ArrayList<>();
        conversationAdapter = new MessageAdapter(conversationList, currentUsername);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(conversationAdapter);

        // Load conversation previews from Firebase.
        // (For a real app, use a proper Conversation model and node.)
        conversationsRef = FirebaseDatabase.getInstance().getReference("conversations");
        conversationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                conversationList.clear();
                for (DataSnapshot convoSnapshot : snapshot.getChildren()) {
                    Message convo = convoSnapshot.getValue(Message.class);
                    if (convo != null) {
                        conversationList.add(convo);
                    }
                }
                conversationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors if necessary
            }
        });

        // Navigation button listeners
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageOverviewActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageOverviewActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        messButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Already here – no action needed.
            }
        });

        // Set item click listener to open ChatActivity.
        conversationAdapter.setOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // You can pass conversation-specific data via extras if needed.
                Intent intent = new Intent(MessageOverviewActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
    }
}

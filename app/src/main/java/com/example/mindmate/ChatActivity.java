package com.example.mindmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
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

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private DatabaseReference chatRef;
    private final String currentUsername = "User1"; // Replace with dynamic user info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        recyclerView = findViewById(R.id.message_list_view);

        // Set up Firebase reference
        chatRef = FirebaseDatabase.getInstance().getReference("chats");

        // Set up RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUsername);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Sending messages
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgText = messageInput.getText().toString().trim();
                if (!TextUtils.isEmpty(msgText)) {
                    Message message = new Message(currentUsername, msgText);
                    chatRef.push().setValue(message);
                    messageInput.setText("");
                }
            }
        });

        // Listening for new messages
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    Message msg = msgSnapshot.getValue(Message.class);
                    if (msg != null) {
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }
}

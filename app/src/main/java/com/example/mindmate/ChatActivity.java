package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView; // Add this import
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private FirebaseFirestore db;

    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private TextView chatPartnerNameTextView;  // Add this reference for the name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get IDs from intent
        chatId = getIntent().getStringExtra("CHAT_ID");
        otherUserId = getIntent().getStringExtra("OTHER_USER_ID");

        if (chatId == null || otherUserId == null) {
            Toast.makeText(this, "Invalid chat session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = user.getUid();

        db = FirebaseFirestore.getInstance();

        initializeFirestore();
        setupUI();
        setupMessageListener();
        fetchOtherUserName();  // Fetch the other user's name
    }

    // Fetch the full name of the other user from Firestore
    private void fetchOtherUserName() {
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null) {
                            chatPartnerNameTextView.setText(fullName); // Set the name in the TextView
                        }
                    } else {
                        chatPartnerNameTextView.setText("Unknown User"); // Fallback if user doesn't exist
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    // If chat document does not exist, create one.
    private void initializeFirestore() {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Chat newChat = new Chat(currentUserId, otherUserId, "");
                chatRef.set(newChat)
                        .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Create chat failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupUI() {
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        recyclerView = findViewById(R.id.message_list_view);

        // Set the reference for the TextView that will display the other user's name
        chatPartnerNameTextView = findViewById(R.id.chat_partner_name);  // Initialize the TextView reference

        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> sendMessage());

        // Set up the back button to go back to the chat overview
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());  // Close the current activity and return to the previous one
    }

    // Send a message with the current timestamp.
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        Message message = new Message(currentUserId, messageText, otherUserId, Timestamp.now());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                    updateLastMessage(messageText);
                })
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Send failed", Toast.LENGTH_SHORT).show());
    }

    // Update the chat document with the last sent message.
    private void updateLastMessage(String messageText) {
        db.collection("chats").document(chatId)
                .update("lastMessage", messageText)
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    // Listen for messages ordered by timestamp.
    private void setupMessageListener() {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Listen failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Message message = doc.toObject(Message.class);
                            messages.add(message);
                        }
                    }
                    messageAdapter.updateMessages(messages);
                });
    }
}

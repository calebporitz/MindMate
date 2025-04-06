package com.example.mindmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

    // New TextView to display the chat partner's name
    private TextView chatPartnerNameTextView;

    private String chatId;
    private String currentUserId;
    private String otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve chatId and otherUserId from Intent extras.
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

        // Setup UI elements
        setupUI();
        // Fetch and display the chat partner's name.
        fetchAndDisplayChatPartnerName();

        initializeFirestore();
        setupMessageListener();
    }

    // Sets up UI elements and RecyclerView.
    private void setupUI() {
        // Assume your activity_chat.xml now contains a TextView with id "chat_partner_name"
        chatPartnerNameTextView = findViewById(R.id.chat_partner_name);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        recyclerView = findViewById(R.id.message_list_view);

        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    // Fetch the chat partner's full name from Firestore and display it.
    private void fetchAndDisplayChatPartnerName() {
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        chatPartnerNameTextView.setText(fullName);
                    } else {
                        chatPartnerNameTextView.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e ->
                        chatPartnerNameTextView.setText("Error Loading Name")
                );
    }

    // If the chat document does not exist, create one.
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

    // Sends a message with the current timestamp.
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

    // Updates the chat document's lastMessage field.
    private void updateLastMessage(String messageText) {
        db.collection("chats").document(chatId)
                .update("lastMessage", messageText)
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    // Sets up a real-time listener on the messages subcollection, ordered by timestamp.
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

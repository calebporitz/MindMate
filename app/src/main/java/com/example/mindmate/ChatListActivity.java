package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements ChatAdapter.OnChatClickListener {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadChats();
    }

    // Sets up the RecyclerView and attaches the ChatAdapter.
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.chatRecyclerView);
        adapter = new ChatAdapter(chatList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // Query Firestore for chats where the current user is either user1 or user2.
    private void loadChats() {
        db.collection("chats")
                .whereEqualTo("user1", currentUserId)
                .addSnapshotListener((snapshot, e) -> processChats(snapshot, e));

        db.collection("chats")
                .whereEqualTo("user2", currentUserId)
                .addSnapshotListener((snapshot, e) -> processChats(snapshot, e));
    }

    // Processes the snapshot from Firestore, updates the chatList, and fetches the other user's name.
    private void processChats(QuerySnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) return;
        if (snapshot == null) return;
        for (DocumentChange dc : snapshot.getDocumentChanges()) {
            Chat chat = dc.getDocument().toObject(Chat.class);
            String otherUserId = chat.getUser1().equals(currentUserId)
                    ? chat.getUser2() : chat.getUser1();
            db.collection("users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String fullName = userDoc.getString("fullName");
                            chat.setOtherUserName(fullName);
                        } else {
                            chat.setOtherUserName("Unknown");
                        }
                        switch (dc.getType()) {
                            case ADDED:
                                chatList.add(chat);
                                break;
                            case MODIFIED:
                                updateExistingChat(chat);
                                break;
                            default:
                                break;
                        }
                        adapter.notifyDataSetChanged();
                    });
        }
    }

    // Updates an existing chat in the chatList.
    private void updateExistingChat(Chat updatedChat) {
        for (int i = 0; i < chatList.size(); i++) {
            Chat chat = chatList.get(i);
            if (chat.getUser1().equals(updatedChat.getUser1()) &&
                    chat.getUser2().equals(updatedChat.getUser2())) {
                chatList.set(i, updatedChat);
                break;
            }
        }
    }

    // When a chat item is clicked, open ChatActivity with the correct extras.
    @Override
    public void onChatClick(Chat chat) {
        String otherUserId = chat.getUser1().equals(currentUserId) ? chat.getUser2() : chat.getUser1();
        String chatId = ChatUtils.generateChatId(currentUserId, otherUserId);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        intent.putExtra("OTHER_USER_ID", otherUserId);
        startActivity(intent);
    }
}

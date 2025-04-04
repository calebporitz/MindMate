package com.example.mindmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class ChatOverviewFragment extends Fragment implements ChatAdapter.OnChatClickListener {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_overview, container, false);

        // Get current user ID from FirebaseAuth
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        setupRecyclerView(view);
        loadChats();

        return view;
    }

    // Sets up the RecyclerView with a LinearLayoutManager and attaches the ChatAdapter.
    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.chatRecyclerView);
        adapter = new ChatAdapter(chatList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

    // Process the snapshot from Firestore, updating the chatList.
    private void processChats(QuerySnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) return;
        if (snapshot == null) return;
        for (DocumentChange dc : snapshot.getDocumentChanges()) {
            // Convert Firestore document into a Chat object.
            Chat chat = dc.getDocument().toObject(Chat.class);
            // Determine the other user's ID based on currentUserId.
            String otherUserId = chat.getUser1().equals(currentUserId)
                    ? chat.getUser2()
                    : chat.getUser1();
            // Fetch the other user's full name from the "users" collection.
            db.collection("users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            // Assuming the user document has a "fullName" field.
                            String fullName = userDoc.getString("fullName");
                            chat.setOtherUserName(fullName);
                        } else {
                            chat.setOtherUserName("Unknown");
                        }
                        // Handle DocumentChange types (ADDED, MODIFIED).
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
                        // Notify adapter to refresh the list.
                        adapter.notifyDataSetChanged();
                    });
        }
    }

    // Updates an existing chat in the list.
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

    // When a chat item is clicked, determine the other user's ID, generate the chat ID,
    // and open ChatActivity with the appropriate extras.
    @Override
    public void onChatClick(Chat chat) {
        String otherUserId = chat.getUser1().equals(currentUserId) ? chat.getUser2() : chat.getUser1();
        String chatId = ChatUtils.generateChatId(currentUserId, otherUserId);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        intent.putExtra("OTHER_USER_ID", otherUserId);
        startActivity(intent);
    }
}

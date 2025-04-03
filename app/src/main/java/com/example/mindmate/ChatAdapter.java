package com.example.mindmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    // Interface to handle clicks on a chat item.
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    private List<Chat> chatList;
    private OnChatClickListener listener;

    public ChatAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    // Inflates the layout for a single chat item.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    // Binds a Chat object's data to the views.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        // Set the other user's full name and the last message.
        holder.otherUserName.setText(chat.getOtherUserName());
        holder.lastMessage.setText(chat.getLastMessage());
        // Handle item click.
        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // ViewHolder class holds references to the views for each chat item.
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView otherUserName;
        TextView lastMessage;

        ViewHolder(View itemView) {
            super(itemView);
            // Assume your item_chat.xml layout has two TextViews with these IDs.
            otherUserName = itemView.findViewById(R.id.otherUserName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}

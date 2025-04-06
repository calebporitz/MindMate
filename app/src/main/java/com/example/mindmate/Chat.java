package com.example.mindmate;

public class Chat {
    private String user1;
    private String user2;
    private String lastMessage;

    // Not persisted in Firestore, just used for display in the chat overview.
    private String otherUserName;

    public Chat() {} // Needed for Firestore deserialization

    public Chat(String user1, String user2, String lastMessage) {
        this.user1 = user1;
        this.user2 = user2;
        this.lastMessage = lastMessage;
    }

    public String getUser1() {
        return user1;
    }
    public String getUser2() {
        return user2;
    }
    public String getLastMessage() {
        return lastMessage;
    }

    public String getOtherUserName() {
        return otherUserName;
    }
    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }
}

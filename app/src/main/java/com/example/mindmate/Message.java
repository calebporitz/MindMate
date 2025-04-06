package com.example.mindmate;

import com.google.firebase.Timestamp;

public class Message {
    private String sender;
    private String text;
    private String receiver;
    private Timestamp timestamp;

    public Message() {} // Needed for Firestore

    public Message(String sender, String text, String receiver, Timestamp timestamp) {
        this.sender = sender;
        this.text = text;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }
    public String getText() {
        return text;
    }
    public String getReceiver() {
        return receiver;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
}

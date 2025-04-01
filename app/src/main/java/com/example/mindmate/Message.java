package com.example.mindmate;

public class Message {
    public String sender;
    public String text;

    public Message() {
        // Needed for Firebase deserialization
    }

    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }
}

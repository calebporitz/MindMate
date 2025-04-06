package com.example.mindmate;

public class ChatUtils {
    // Generates a unique chat ID for a pair of users. Ensures the same ID regardless of the order.
    public static String generateChatId(String user1Id, String user2Id) {
        if (user1Id == null || user2Id == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }
        return user1Id.compareTo(user2Id) < 0 ? user1Id + "_" + user2Id : user2Id + "_" + user1Id;
    }
}

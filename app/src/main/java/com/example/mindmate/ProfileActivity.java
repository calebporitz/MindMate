package com.example.mindmate;

import com.google.firebase.firestore.DocumentSnapshot;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView textUserName, textUserEmail;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private TextView textCurrentlyStudying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Find UI elements
        textUserName = findViewById(R.id.userName);
        textUserEmail = findViewById(R.id.userEmail);
        textCurrentlyStudying = findViewById(R.id.currentlyStudying);
        Button btnSetStatus = findViewById(R.id.btnSetStatus);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnResetPassword = findViewById(R.id.btnResetPassword);

        // Load user information, including status
        loadUserInfo();

        // Remove top bar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Button click listeners
        btnSetStatus.setOnClickListener(v -> showStatusDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnResetPassword.setOnClickListener(v -> showResetPasswordDialog());

        // Set up Settings Navigation
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Set up bottom navigation bar buttons
        ImageButton navSearching = findViewById(R.id.nav_find);
        navSearching.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SearchingActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        ImageButton navChat = findViewById(R.id.nav_chat);
        navChat.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void showStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Study Status");

        final EditText input = new EditText(this);
        input.setHint("Enter your status");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newStatus = input.getText().toString().trim();
            if (!newStatus.isEmpty()) {
                updateStudyStatus(newStatus);
            } else {
                Toast.makeText(ProfileActivity.this, "Status cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    private void updateStudyStatus(String status) {
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        db.collection("users").whereEqualTo("email", userEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String username = document.getId(); // Get the username as document ID
                            db.collection("users").document(username)
                                    .update("studyStatus", status)
                                    .addOnSuccessListener(aVoid -> {
                                        textCurrentlyStudying.setText("📖 " + status);
                                        Toast.makeText(ProfileActivity.this, "Status updated!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ProfileActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "User not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadUserInfo() {
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            textUserEmail.setText(userEmail);

            db.collection("users").whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String username = document.getString("username");
                                textUserName.setText(username);

                                // Load Study Status
                                String studyStatus = document.getString("studyStatus");
                                if (studyStatus != null && !studyStatus.isEmpty()) {
                                    textCurrentlyStudying.setText("📖 " + studyStatus);
                                } else {
                                    textCurrentlyStudying.setText("📖 Currently studying");
                                }
                            }
                        } else {
                            textUserName.setText("Unknown User");
                            Toast.makeText(ProfileActivity.this, "Failed to load username", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            textUserEmail.setText("No user logged in");
            textUserName.setText("Unknown User");
        }
    }


    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your current password and new password.");

        // Create a layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Current Password input
        final EditText inputCurrentPassword = new EditText(this);
        inputCurrentPassword.setHint("Current Password");
        inputCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputCurrentPassword);

        // New Password input
        final EditText inputNewPassword = new EditText(this);
        inputNewPassword.setHint("New Password");
        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNewPassword);

        // Repeat New Password input
        final EditText inputRepeatPassword = new EditText(this);
        inputRepeatPassword.setHint("Repeat New Password");
        inputRepeatPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputRepeatPassword);

        builder.setView(layout);

        // Confirm button
        builder.setPositiveButton("Change Password", (dialog, which) -> {
            String currentPassword = inputCurrentPassword.getText().toString().trim();
            String newPassword = inputNewPassword.getText().toString().trim();
            String repeatPassword = inputRepeatPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "All fields are required.", Toast.LENGTH_LONG).show();
            } else if (!newPassword.equals(repeatPassword)) {
                Toast.makeText(ProfileActivity.this, "New passwords do not match.", Toast.LENGTH_LONG).show();
            } else if (newPassword.length() < 6) {
                Toast.makeText(ProfileActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
            } else {
                resetUserPassword(currentPassword, newPassword);
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void resetUserPassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        // Re-authenticate the user before changing the password
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update the password
                currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update password.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(ProfileActivity.this, "Authentication failed. Check your current password.", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");

        // Log Out button
        builder.setPositiveButton("Log Out", (dialog, which) -> logoutUser());

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.show();
    }

    private void logoutUser() {
        mAuth.signOut(); // Logs the user out of Firebase
        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close ProfileActivity
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("This action is permanent. Enter your password and check the box to confirm.");

        // Create a layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Password input
        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Enter your password");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        // Confirmation checkbox
        final CheckBox confirmCheckBox = new CheckBox(this);
        confirmCheckBox.setText("I understand that this action is permanent.");
        layout.addView(confirmCheckBox);

        builder.setView(layout);

        // Confirm button
        builder.setPositiveButton("Delete", (dialog, which) -> {
            String password = inputPassword.getText().toString().trim();
            boolean isChecked = confirmCheckBox.isChecked();

            if (!password.isEmpty() && isChecked) {
                deleteUserAccount(password);
            } else {
                Toast.makeText(ProfileActivity.this, "Please enter your password and check the confirmation box.", Toast.LENGTH_LONG).show();
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void deleteUserAccount(String password) {
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        // Re-authenticate the user before deleting
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, password);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Delete user from Firestore
                db.collection("users").document(userEmail).delete()
                        .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "User data deleted from Firestore"))
                        .addOnFailureListener(e -> Log.e("ProfileActivity", "Error deleting Firestore data", e));

                // Delete user from Firebase Authentication
                currentUser.delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                        mAuth.signOut(); // Log out the user

                        // Redirect to login screen
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to delete account", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(ProfileActivity.this, "Authentication failed. Check your password.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
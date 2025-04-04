package com.example.mindmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.View;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private TextView textUserName, textUserEmail, textCurrentlyStudying;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private Uri selectedImageUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Find views
        textUserName = view.findViewById(R.id.fullName);
        textUserEmail = view.findViewById(R.id.userEmail);
        textCurrentlyStudying = view.findViewById(R.id.currentlyStudying);
        profileImage = view.findViewById(R.id.profileImage);

        Button btnSetStatus = view.findViewById(R.id.btnSetStatus);
        Button btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnResetPassword = view.findViewById(R.id.btnResetPassword);
        Button btnSetProfilePicture = view.findViewById(R.id.btnSetProfilePicture);

        // Load user info
        loadUserInfo();

        // Button listeners
        btnSetStatus.setOnClickListener(v -> showStatusDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnResetPassword.setOnClickListener(v -> showResetPasswordDialog());
        btnSetProfilePicture.setOnClickListener(v -> openImageChooser());

        // Settings button
        ImageButton btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri); // Shows the selected image

            // Optional: upload to Firebase Storage
            uploadProfilePictureToFirebase();
        }
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
                                String fullName = document.getString("fullName");
                                textUserName.setText(fullName != null ? fullName : "Unnamed User");

                                String studyStatus = document.getString("studyStatus");
                                if (studyStatus != null && !studyStatus.isEmpty()) {
                                    textCurrentlyStudying.setText("📖 " + studyStatus);
                                } else {
                                    textCurrentlyStudying.setText("📖 Currently studying");
                                }

                                // Load profile picture if it exists
                                String profilePicUrl = document.getString("profilePictureUrl");
                                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                    Glide.with(this).load(profilePicUrl).into(profileImage);
                                }
                            }
                        } else {
                            textUserName.setText("Unknown User");
                            Toast.makeText(getActivity(), "Failed to load user info", Toast.LENGTH_SHORT).show();

                        }
                    });
        } else {
            textUserEmail.setText("No user logged in");
            textUserName.setText("Unknown User");
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadProfilePictureToFirebase() {
        if (selectedImageUri == null || currentUser == null) return;

        String userId = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    // Update the user's document directly using UID
                    db.collection("users").document(userId)
                            .update("profilePictureUrl", downloadUrl)
                            .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "Profile picture URL saved"))
                            .addOnFailureListener(e -> Log.e("ProfileActivity", "Failed to save picture URL", e));
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileActivity", "Storage upload failed", e);
                });
    }


    private void showStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Study Status");

        final EditText input = new EditText(getActivity());
        input.setHint("Enter your status");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newStatus = input.getText().toString().trim();
            if (!newStatus.isEmpty()) {
                updateStudyStatus(newStatus);
            } else {
                Toast.makeText(getActivity(), "Status cannot be empty", Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateStudyStatus(String status) {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();

            return;
        }

        String userEmail = currentUser.getEmail();

        db.collection("users").whereEqualTo("email", userEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("users").document(document.getId())
                                    .update("studyStatus", status)
                                    .addOnSuccessListener(aVoid -> {
                                        textCurrentlyStudying.setText("📖 " + status);
                                        Toast.makeText(getActivity(), "Status updated!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(getActivity(), "User not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your current password and new password.");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputCurrentPassword = new EditText(getActivity());
        inputCurrentPassword.setHint("Current Password");
        inputCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputCurrentPassword);

        final EditText inputNewPassword = new EditText(getActivity());
        inputNewPassword.setHint("New Password");
        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNewPassword);

        final EditText inputRepeatPassword = new EditText(getActivity());
        inputRepeatPassword.setHint("Repeat New Password");
        inputRepeatPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputRepeatPassword);

        builder.setView(layout);

        builder.setPositiveButton("Change Password", (dialog, which) -> {
            String currentPassword = inputCurrentPassword.getText().toString().trim();
            String newPassword = inputNewPassword.getText().toString().trim();
            String repeatPassword = inputRepeatPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty()) {
                // Use getActivity() or getContext() instead of ProfileFragment.getActivity()
                Toast.makeText(getActivity(), "All fields are required.", Toast.LENGTH_LONG).show();
            } else if (!newPassword.equals(repeatPassword)) {
                // Use getActivity() or getContext() instead of ProfileFragment.getActivity()
                Toast.makeText(getActivity(), "New passwords do not match.", Toast.LENGTH_LONG).show();
            } else if (newPassword.length() < 6) {
                // Use getActivity() or getContext() instead of ProfileFragment.this
                Toast.makeText(getActivity(), "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
            } else {
                resetUserPassword(currentPassword, newPassword);
            }
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void resetUserPassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            // Use getActivity() or getContext() instead of 'this'
            Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }


        String userEmail = currentUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getActivity(), "Password updated successfully!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update password.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Authentication failed. Check your current password.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Log Out", (dialog, which) -> logoutUser());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish(); // finish the current activity
    }


    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Account");
        builder.setMessage("This action is permanent. Enter your password and check the box to confirm.");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputPassword = new EditText(getActivity());
        inputPassword.setHint("Enter your password");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        final CheckBox confirmCheckBox = new CheckBox(getActivity());
        confirmCheckBox.setText("I understand that this action is permanent.");
        layout.addView(confirmCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String password = inputPassword.getText().toString().trim();
            boolean isChecked = confirmCheckBox.isChecked();

            if (!password.isEmpty() && isChecked) {
                deleteUserAccount(password);
            } else {
                Toast.makeText(getActivity(), "Please enter your password and check the confirmation box.", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteUserAccount(String password) {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, password);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Delete all documents in "users" where email == userEmail
                db.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                db.collection("users").document(doc.getId()).delete();
                            }
                        })
                        .addOnFailureListener(e -> Log.e("ProfileActivity", "Error deleting Firestore data", e));

                // Delete user from Firebase Auth
                currentUser.delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Failed to delete account", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Authentication failed. Check your password.", Toast.LENGTH_LONG).show();
            }
        });

    }
}
package com.example.mindmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextFullName, editTextPassword, editTextPasswordRepeat;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find UI elements
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextFullName = findViewById(R.id.editTextFullName); // Full Name instead of Username
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordRepeat = findViewById(R.id.editTextPasswordRepeat);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String passwordRepeat = editTextPasswordRepeat.getText().toString().trim();

        // Validate email format and domain
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (!email.endsWith("@student.tue.nl")) {
            editTextEmail.setError("Use a @student.tue.nl email");
            editTextEmail.requestFocus();
            return;
        }

        // Validate full name
        if (fullName.isEmpty()) {
            editTextFullName.setError("Enter your full name");
            editTextFullName.requestFocus();
            return;
        }

        // Validate password length
        if (password.isEmpty() || password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        // Check if passwords match
        if (!password.equals(passwordRepeat)) {
            editTextPasswordRepeat.setError("Passwords do not match");
            editTextPasswordRepeat.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Proceed with Firebase authentication
        createFirebaseUser(email, password, fullName);
    }

    private void createFirebaseUser(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data in Firestore immediately
                            saveUserToFirestore(user.getUid(), email, fullName);

                            // Send email verification
                            sendEmailVerification(user);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Verification email sent! Check your inbox.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String fullName) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("fullName", fullName);

        db.collection("users").document(userId) // Store user by UID
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "User data saved successfully in Firestore"))
                .addOnFailureListener(e -> Log.e("RegisterActivity", "Failed to save user data", e));
    }
}

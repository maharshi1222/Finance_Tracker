package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeInformation extends AppCompatActivity {

    private EditText etOldUsername, etNewUsername, etReUsername, etOldPassword, etNewPassword, etRePassword, etOldEmail, etNewEmail, etReEmail, etOldPhone, etNewPhone, etRePhone;
    private String userName;

    private ImageView arrow8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        arrow8=findViewById(R.id.backArrow4);
        arrow8.setOnClickListener(v -> finish());

        userName = getIntent().getStringExtra("userName");
        if (userName == null) {
            userName = ""; // Default to empty if null, though this should be handled earlier
        }

        etOldUsername = findViewById(R.id.etOldUsername);
        etNewUsername = findViewById(R.id.etNewUsername);
        etReUsername = findViewById(R.id.etReUsername);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etRePassword = findViewById(R.id.etRePassword);

        etOldEmail = findViewById(R.id.etOldEmail);
        etNewEmail = findViewById(R.id.etNewEmail);
        etReEmail = findViewById(R.id.etReEmail);

        etOldPhone = findViewById(R.id.etOldPhone);
        etNewPhone = findViewById(R.id.etNewPhone);
        etRePhone = findViewById(R.id.etRePhone);

        ImageView backArrow = findViewById(R.id.backArrow4);
        backArrow.setOnClickListener(v -> finish());

        Button btnChange = findViewById(R.id.btnChange);
        btnChange.setOnClickListener(v -> {
            String oldUser = etOldUsername.getText().toString().trim();
            String newUser = etNewUsername.getText().toString().trim();
            String reUser = etReUsername.getText().toString().trim();

            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String rePass = etRePassword.getText().toString().trim();

            String oldEmail = etOldEmail.getText().toString().trim();
            String newEmail = etNewEmail.getText().toString().trim();
            String reEmail = etReEmail.getText().toString().trim();

            String oldPhone = etOldPhone.getText().toString().trim();
            String newPhone = etNewPhone.getText().toString().trim();
            String rePhone = etRePhone.getText().toString().trim();

            List<String> fields = new ArrayList<>();
            Map<String, Object> updates = new HashMap<>();
            boolean valid = true;

            if (!oldUser.isEmpty()) {
                if (newUser.isEmpty() || reUser.isEmpty()) {
                    valid = false;
                } else if (!newUser.equals(reUser)) {
                    new AlertDialog.Builder(this)
                            .setMessage("New Username and Re-enter Username do not match")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else {
                    fields.add("username");
                    updates.put("name", newUser.toLowerCase()); // Store new username in lowercase
                }
            }

            if (!oldPass.isEmpty()) {
                if (newPass.isEmpty() || rePass.isEmpty()) {
                    valid = false;
                } else if (!newPass.equals(rePass)) {
                    new AlertDialog.Builder(this)
                            .setMessage("New Password and Re-enter Password do not match")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else {
                    fields.add("password");
                    updates.put("password", newPass);
                }
            }

            if (!oldEmail.isEmpty()) {
                if (newEmail.isEmpty() || reEmail.isEmpty()) {
                    valid = false;
                } else if (!newEmail.equals(reEmail)) {
                    new AlertDialog.Builder(this)
                            .setMessage("New Email and Re-enter Email do not match")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else {
                    fields.add("email");
                    updates.put("email", newEmail);
                }
            }

            if (!oldPhone.isEmpty()) {
                if (newPhone.isEmpty() || rePhone.isEmpty()) {
                    valid = false;
                } else if (!newPhone.equals(rePhone)) {
                    new AlertDialog.Builder(this)
                            .setMessage("New Phone Number and Re-enter Phone Number do not match")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else {
                    fields.add("phone number");
                    updates.put("phone", newPhone);
                }
            }

            if (!valid) {
                new AlertDialog.Builder(this)
                        .setMessage("Please fill the new and re-enter fields for any old field entered")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            if (fields.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setMessage("Nothing to change")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            String message = "Do you want to change the " + String.join(", ", fields) + "?";
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Change")
                    .setMessage(message)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        updateFirestore(updates);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void updateFirestore(Map<String, Object> updates) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String searchName = userName.toLowerCase().trim(); // Normalize to lowercase for consistency
        Log.d("ChangeInformation", "Searching for user with name: " + searchName); // Debug log

        db.collection("users")
                .whereEqualTo("name", searchName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot) {
                            String docId = doc.getId();
                            Log.d("ChangeInformation", "Found document ID: " + docId); // Debug log
                            db.collection("users").document(docId).update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        if (updates.containsKey("name")) {
                                            userName = (String) updates.get("name");
                                        }
                                        new AlertDialog.Builder(this)
                                                .setMessage("Updated successfully")
                                                .setPositiveButton("OK", (d, w) -> {
                                                    Intent intent = new Intent(ChangeInformation.this, More.class);
                                                    intent.putExtra("userName", userName);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ChangeInformation", "Update failed: " + e.getMessage()); // Debug log
                                        new AlertDialog.Builder(this)
                                                .setMessage("Error updating: " + e.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show();
                                    });
                            break; // Update only the first matching document
                        }
                    } else {
                        Log.w("ChangeInformation", "No user found with name: " + searchName); // Debug log
                        new AlertDialog.Builder(this)
                                .setMessage("User not found. Please ensure the username matches the stored data.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChangeInformation", "Query failed: " + e.getMessage()); // Debug log
                    new AlertDialog.Builder(this)
                            .setMessage("Error querying: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                });
    }
}
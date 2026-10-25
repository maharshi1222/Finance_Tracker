package com.example.finance;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class feedback extends AppCompatActivity {

    private EditText etName, etEmail, etFeedback;
    private RatingBar ratingBar;
    private Button btnSubmit;

    private FirebaseFirestore db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        username = getIntent().getStringExtra("userName");
        if (username == null || username.isEmpty()) {
            username = "unknown_user";
        } else {
            username = username.substring(0, 1).toUpperCase() + username.substring(1);
        }

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etFeedback = findViewById(R.id.etFeedback);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Set username in name field and make it non-editable
        etName.setText(username);
        etName.setEnabled(false); // disables typing
        etName.setFocusable(false); // removes focus highlight

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String feedbackText = etFeedback.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (TextUtils.isEmpty(email) || !email.contains("@")) {
                etEmail.setError("Enter a valid email");
                return;
            }
            if (rating == 0) {
                Toast.makeText(this, "Please rate the app", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(feedbackText)) {
                etFeedback.setError("Please enter your feedback");
                return;
            }

            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("name", name);
            feedbackData.put("email", email);
            feedbackData.put("rating", rating);
            feedbackData.put("feedback", feedbackText);
            feedbackData.put("timestamp", System.currentTimeMillis());

            DocumentReference feedbackDocRef = db
                    .collection("users")
                    .document(username)
                    .collection("feedback")
                    .document(username);

            feedbackDocRef.set(feedbackData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(feedback.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();

                        etEmail.setText("");
                        etFeedback.setText("");
                        ratingBar.setRating(0);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(feedback.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}

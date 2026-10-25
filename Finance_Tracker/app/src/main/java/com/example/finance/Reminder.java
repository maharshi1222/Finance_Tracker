package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Reminder extends AppCompatActivity {

    private ImageView backarrow4;
    private LinearLayout reminderContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reminder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backarrow4 = findViewById(R.id.backArrow4);
        reminderContainer = findViewById(R.id.reminderContainer);

        String userName = getIntent().getStringExtra("userName");
        if (userName != null) userName = userName.trim().toLowerCase();
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, "Authentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
            Log.e("Reminder", "Username is null or empty");
            startActivity(new Intent(this, Login.class));
            finish();
        } else {
            loadReminders(userName);
        }
        backarrow4.setOnClickListener(v -> finish());
    }

    private void loadReminders(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        reminderContainer.removeAllViews();

        db.collection("users").document(username).collection("reminders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        TextView noReminders = new TextView(this);
                        noReminders.setText("No Reminders Yet");
                        noReminders.setTextSize(16);
                        noReminders.setGravity(Gravity.CENTER);
                        reminderContainer.addView(noReminders);
                        return;
                    }

                    // Collect all reminders and sort by ID
                    ArrayList<Map.Entry<String, Map<String, Object>>> allReminders = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            allReminders.add(new HashMap.SimpleEntry<>(doc.getId(), data));
                        }
                    }

                    Collections.sort(allReminders, (o1, o2) -> {
                        String id1 = o1.getKey().replace("reminder", "");
                        String id2 = o2.getKey().replace("reminder", "");
                        try {
                            return Integer.parseInt(id1) - Integer.parseInt(id2);
                        } catch (NumberFormatException e) {
                            Log.e("Reminder", "Error parsing reminder ID: " + e.getMessage());
                            return o1.getKey().compareTo(o2.getKey());
                        }
                    });

                    // Process each reminder and its notes
                    for (Map.Entry<String, Map<String, Object>> reminderEntry : allReminders) {
                        Map<String, Object> reminderData = reminderEntry.getValue();

                        // Sort notes by numeric suffix
                        TreeMap<String, Map<String, Object>> sortedNotes = new TreeMap<>();
                        for (Map.Entry<String, Object> noteEntry : reminderData.entrySet()) {
                            if (noteEntry.getValue() instanceof Map) {
                                sortedNotes.put(noteEntry.getKey(), (Map<String, Object>) noteEntry.getValue());
                            }
                        }

                        for (Map<String, Object> note : sortedNotes.values()) {
                            MaterialCardView cardView = new MaterialCardView(this);
                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                            cardParams.topMargin = 16;   // Gap above each card
                            cardParams.bottomMargin = 16; // Gap below each card
                            cardView.setLayoutParams(cardParams);
                            cardView.setCardElevation(4);
                            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                            cardView.setRadius(12);

                            LinearLayout cardLayout = new LinearLayout(this);
                            cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                            cardLayout.setPadding(16, 12, 16, 12);

                            // Note
                            TextView reminderText = new TextView(this);
                            reminderText.setText((String) note.get("note"));
                            reminderText.setTextSize(16);
                            reminderText.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Approx #2E7D32
                            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2
                            );
                            reminderText.setLayoutParams(noteParams);

                            // Date
                            TextView dateText = new TextView(this);
                            dateText.setText((String) note.get("reminderDate"));
                            dateText.setTextSize(14);
                            dateText.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Approx #757575
                            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                            );
                            dateParams.gravity = Gravity.END;
                            dateText.setLayoutParams(dateParams);

                            cardLayout.addView(reminderText);
                            cardLayout.addView(dateText);

                            cardView.addView(cardLayout);
                            reminderContainer.addView(cardView);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Reminder", "Error loading reminders: " + e.getMessage());
                    Toast.makeText(this, "Failed to load reminders", Toast.LENGTH_SHORT).show();
                });
    }

}
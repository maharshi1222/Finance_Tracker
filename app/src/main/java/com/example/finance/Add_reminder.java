package com.example.finance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Add_reminder extends AppCompatActivity {

    private ImageView backarrow2, dateset, timeset;
    private TextView date, time;
    private LinearLayout noteContainer;
    private MaterialCardView addNoteCard;
    private String UserName;
    private Button btnAddReminder;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_reminder);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backarrow2 = findViewById(R.id.backArrow2);
        date = findViewById(R.id.Date2);
        time = findViewById(R.id.Time2);
        noteContainer = findViewById(R.id.noteContainer);
        addNoteCard = findViewById(R.id.addNoteCard);
        btnAddReminder = findViewById(R.id.btnAddremainder);
        dateset = findViewById(R.id.dateset);
        timeset = findViewById(R.id.timeset);

        UserName = getIntent().getStringExtra("userName");
        if (UserName != null) UserName = UserName.trim().toLowerCase(); // normalize
        db = FirebaseFirestore.getInstance();

        backarrow2.setOnClickListener(v -> finish());
        addNoteCard.setOnClickListener(v -> addNewNote());
        btnAddReminder.setOnClickListener(v -> saveReminder());
        dateset.setOnClickListener(v -> showDatePicker(dateset));
        timeset.setOnClickListener(v -> showTimePicker(timeset));

        startTimeUpdate();
    }

    private void saveReminder() {
        if (UserName == null || UserName.isEmpty()) return;

        Map<String, Object> newNotes = new HashMap<>();
        int noteIndex = 1;
        boolean hasNote = false;

        for (int i = 0; i < noteContainer.getChildCount(); i++) {
            View child = noteContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                String noteText = "";
                String reminderDate = "";
                String reminderTime = "";

                for (int j = 0; j < layout.getChildCount(); j++) {
                    View inner = layout.getChildAt(j);
                    if (inner instanceof EditText) {
                        noteText = ((EditText) inner).getText().toString().trim();
                    } else if (inner instanceof ImageView && inner.getTag() != null) {
                        String tag = inner.getTag().toString();
                        if (tag.matches("\\d{2}/\\d{2}/\\d{4}")) reminderDate = tag;
                        else if (tag.matches("\\d{2}:\\d{2} [AP]M")) reminderTime = tag;
                    }
                }

                if (!noteText.isEmpty()) {
                    if (reminderDate.isEmpty() || reminderTime.isEmpty()) {
                        Toast.makeText(this, "Select date and time for all notes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> singleNote = new HashMap<>();
                    singleNote.put("note", noteText);
                    singleNote.put("reminderDate", reminderDate);
                    singleNote.put("reminderTime", reminderTime);

                    newNotes.put("note" + noteIndex, singleNote);
                    noteIndex++;
                    hasNote = true;
                }
            }
        }

        if (!hasNote) {
            Toast.makeText(this, "Please enter at least one note", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch existing reminders to determine next incremental ID
        db.collection("users")
                .document(UserName)
                .collection("reminders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int nextReminderNumber = querySnapshot.size() + 1; // next sequential number
                    String reminderId = "reminder" + nextReminderNumber;

                    db.collection("users")
                            .document(UserName)
                            .collection("reminders")
                            .document(reminderId)
                            .set(newNotes)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Reminder added successfully!", Toast.LENGTH_SHORT).show();
                                noteContainer.removeAllViews();

                                // ✅ Notify HomeScreen to refresh reminders
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching reminders: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void addNewNote() {
        int childCount = noteContainer.getChildCount();

        //Prevent empty note before adding another
        if (childCount > 0) {
            View lastChild = noteContainer.getChildAt(childCount - 1);
            if (lastChild != null) {
                EditText lastNoteInput = lastChild.findViewById(R.id.noteInput);
                if (lastNoteInput != null && lastNoteInput.getText().toString().trim().isEmpty()) {
                    lastNoteInput.setError("Please enter note before adding another");
                    lastNoteInput.requestFocus();
                    return;
                }
            }
        }

        // Inflate note_item.xml layout
        View newNoteLayout = getLayoutInflater().inflate(R.layout.note_item, noteContainer, false);

        ImageView dateIcon = newNoteLayout.findViewById(R.id.dateset);
        ImageView timeIcon = newNoteLayout.findViewById(R.id.timeset);

        dateIcon.setOnClickListener(v -> showDatePicker(dateIcon));
        timeIcon.setOnClickListener(v -> showTimePicker(timeIcon));

        //Copy previous note’s date/time if present
        if (childCount > 0) {
            View lastChild = noteContainer.getChildAt(childCount - 1);
            ImageView lastDate = lastChild.findViewById(R.id.dateset);
            ImageView lastTime = lastChild.findViewById(R.id.timeset);

            if (lastDate != null && lastDate.getTag() != null)
                dateIcon.setTag(lastDate.getTag());

            if (lastTime != null && lastTime.getTag() != null)
                timeIcon.setTag(lastTime.getTag());
        }

        noteContainer.addView(newNoteLayout);
    }

    private void showDatePicker(ImageView dateIcon) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    dateIcon.setTag(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void showTimePicker(ImageView timeIcon) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
                    timeIcon.setTag(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePicker.show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void startTimeUpdate() {
        android.os.Handler handler = new android.os.Handler();
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void updateDateTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("d'th' MMMM, yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String formattedDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String formattedTime = timeFormat.format(calendar.getTime());

        if (date != null) date.setText(formattedDate);
        if (time != null) time.setText(formattedTime);
    }
}

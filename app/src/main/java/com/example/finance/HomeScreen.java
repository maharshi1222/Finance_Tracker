package com.example.finance;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeScreen extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    private TextView tvUserName, SeeAllTransactions, SeeAllReminders, lblexpense, lblincome, lblbalance;
    private LinearLayout homeLayout, analysisLayout, moreLayout;
    private MaterialCardView spending, income;
    private ImageView plusreminder, plustransaction;
    private LinearLayout transactionContainer, reminderContainer;
    private NestedScrollView transactionsScroll, reminderScroll;
    private Spinner spinnerMonth;
    private FirebaseFirestore db;

    private boolean notificationsSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        // Reset notificationsSent flag for new user session
        notificationsSent = false; // Moved to ensure notifications are sent for each user

        // Check notification permission
        checkNotificationPermission();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.tvUserName);
        spending = findViewById(R.id.spending);
        income = findViewById(R.id.income);
        lblexpense = findViewById(R.id.lblexpense);
        lblincome = findViewById(R.id.lblincome);
        lblbalance = findViewById(R.id.lblbalance);
        SeeAllTransactions = findViewById(R.id.btnSeeAllTransactions);
        SeeAllReminders = findViewById(R.id.btnSeeAllReminders);
        homeLayout = findViewById(R.id.home);
        analysisLayout = findViewById(R.id.analysis);
        moreLayout = findViewById(R.id.more);
        plustransaction = findViewById(R.id.plustransaction);
        plusreminder = findViewById(R.id.plusreminder);
        transactionContainer = findViewById(R.id.transactionsContainer);
        reminderContainer = findViewById(R.id.reminderContainer);
        transactionsScroll = findViewById(R.id.transactionsScroll);
        reminderScroll = findViewById(R.id.reminderScroll);
        spinnerMonth = findViewById(R.id.spinnerMonth);

        String userName = getIntent().getStringExtra("userName");
        if (userName != null) userName = userName.trim().toLowerCase();
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        } else {
            Toast.makeText(this, "Authentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
            Log.e("HomeScreen", "Username is null or empty");
            startActivity(new Intent(this, Login.class));
            finish();
        }

        String[] monthsArray = {
                "This Month", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, monthsArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String month = parent.getItemAtPosition(position).toString();
                loadTransactions(month);
                loadReminders(month);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadTransactions("This Month");
                loadReminders("This Month");
            }
        });

        spending.setOnClickListener(v -> startActivity(new Intent(HomeScreen.this, Add_transaction.class)
                .putExtra("userName", tvUserName.getText().toString())));
        income.setOnClickListener(v -> startActivity(new Intent(HomeScreen.this, Add_transaction.class)
                .putExtra("userName", tvUserName.getText().toString())));

        SeeAllTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, Transaction.class);
            intent.putExtra("userName", tvUserName.getText().toString());
            startActivity(intent);
        });

        SeeAllReminders.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, Reminder.class);
            intent.putExtra("userName", tvUserName.getText().toString());
            startActivity(intent);
        });

        homeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, HomeScreen.class);
            intent.putExtra("userName", tvUserName.getText().toString());
            startActivity(intent);
        });
        analysisLayout.setOnClickListener(v -> startActivity(new Intent(HomeScreen.this, Analysis.class)
                .putExtra("userName", tvUserName.getText().toString())));
        moreLayout.setOnClickListener(v -> startActivity(new Intent(HomeScreen.this, More.class)
                .putExtra("userName", tvUserName.getText().toString())));

        plustransaction.setOnClickListener(v -> startActivityForResult(
                new Intent(HomeScreen.this, Add_transaction.class)
                        .putExtra("userName", tvUserName.getText().toString()), 101));
        plusreminder.setOnClickListener(v -> startActivityForResult(
                new Intent(HomeScreen.this, Add_reminder.class)
                        .putExtra("userName", tvUserName.getText().toString()), 102));
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationsSent = false; // Reset to ensure notifications can be sent
        loadTransactions("This Month");
        loadReminders("This Month");
        checkBudgetAlert();
    }

    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders may not show.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkBudgetAlert() {
        String username = tvUserName.getText().toString().trim().toLowerCase();
        if (username.isEmpty()) {
            Log.e("HomeScreen", "Username is empty, skipping budget alert check");
            return;
        }

        Log.d("HomeScreen", "Checking budget alert for user: " + username);

        db.collection("users").document(username).collection("budgetlimit").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.d("HomeScreen", "No budget limit document found for user: " + username);
                        new AlertDialog.Builder(HomeScreen.this)
                                .setTitle("⚠ No Budget Limit Found")
                                .setMessage("You haven’t set a monthly budget yet.\n\nPlease go to the 'More' section → Finance card to set your budget limit.")
                                .setPositiveButton("Go to More", (dialog, which) -> {
                                    Intent intent = new Intent(HomeScreen.this, More.class);
                                    intent.putExtra("userName", tvUserName.getText().toString());
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        return;
                    }

                    List<Map<String, Object>> budgetHistory =
                            (List<Map<String, Object>>) documentSnapshot.get("budgetHistory");
                    if (budgetHistory == null || budgetHistory.isEmpty()) {
                        Log.d("HomeScreen", "Budget history is empty for user: " + username);
                        new AlertDialog.Builder(HomeScreen.this)
                                .setTitle("⚠ No Budget Set")
                                .setMessage("You haven’t added any monthly budget.\n\nPlease go to the 'More' section from Finance card to set your budget limit.")
                                .setPositiveButton("Go to More", (dialog, which) -> {
                                    Intent intent = new Intent(HomeScreen.this, More.class);
                                    intent.putExtra("userName", tvUserName.getText().toString());
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        return;
                    }

                    Calendar calendar = Calendar.getInstance();
                    String currentMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

                    double budgetLimit = 0.0;
                    for (Map<String, Object> entry : budgetHistory) {
                        String month = (String) entry.get("month");
                        if (month != null && month.equalsIgnoreCase(currentMonth)) {
                            String limitStr = (String) entry.get("limit");
                            try {
                                budgetLimit = Double.parseDouble(limitStr.replaceAll("[^0-9.]", ""));
                                Log.d("HomeScreen", "Budget limit for " + currentMonth + " for user " + username + ": " + budgetLimit);
                            } catch (Exception e) {
                                Log.e("HomeScreen", "Error parsing budget limit for user " + username + ": " + limitStr + ", Error: " + e.getMessage());
                                budgetLimit = 0.0;
                            }
                            break;
                        }
                    }

                    if (budgetLimit == 0.0) {
                        Log.d("HomeScreen", "No budget limit set for " + currentMonth + " for user: " + username);
                        new AlertDialog.Builder(HomeScreen.this)
                                .setTitle("⚠ No Budget Limit for " + currentMonth)
                                .setMessage("You haven’t set your budget limit for " + currentMonth + ".\n\nPlease go to the 'More' section from Finance card to set your budget limit.")
                                .setPositiveButton("Go to More", (dialog, which) -> {
                                    Intent intent = new Intent(HomeScreen.this, More.class);
                                    intent.putExtra("userName", tvUserName.getText().toString());
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        return;
                    }

                    double finalBudgetLimit = budgetLimit;
                    db.collection("users").document(username).collection("transactions")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                double totalExpense = 0.0;
                                int transactionCount = 0;
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    Map<String, Object> txMap = doc.getData();
                                    if (txMap != null) {
                                        String type = (String) txMap.get("type");
                                        String amountStr = (String) txMap.get("amount");
                                        if ("expense".equalsIgnoreCase(type)) {
                                            try {
                                                double amount = Double.parseDouble(amountStr.replaceAll("[^0-9.]", ""));
                                                totalExpense += amount;
                                                transactionCount++;
                                            } catch (Exception e) {
                                                Log.e("HomeScreen", "Error parsing transaction amount for user " + username + ": " + amountStr + ", Error: " + e.getMessage());
                                            }
                                        }
                                    }
                                }

                                Log.d("HomeScreen", "User: " + username + ", Total Expenses: " + totalExpense + ", Budget Limit: " + finalBudgetLimit + ", Transactions Found: " + transactionCount);

                                String title = null;
                                String message = null;

                                if (totalExpense > finalBudgetLimit) {
                                    title = "⚠ Budget Limit Exceeded";
                                    message = "You’ve exceeded your budget for " + currentMonth +
                                            "!\nExpenses: ₹" + String.format(Locale.getDefault(), "%.2f", totalExpense) +
                                            "\nLimit: ₹" + String.format(Locale.getDefault(), "%.2f", finalBudgetLimit);
                                } else if (totalExpense == finalBudgetLimit) {
                                    title = "⚠ Budget Limit Reached";
                                    message = "You have reached your budget limit for " + currentMonth +
                                            "!\nExpenses: ₹" + String.format(Locale.getDefault(), "%.2f", totalExpense) +
                                            "\nLimit: ₹" + String.format(Locale.getDefault(), "%.2f", finalBudgetLimit);
                                } else {
                                    Log.d("HomeScreen", "No budget notification sent for user " + username + ": Expenses (" + totalExpense + ") do not exceed or equal limit (" + finalBudgetLimit + ")");
                                }

                                if (title != null && message != null) {
                                    Log.d("HomeScreen", "Sending budget notification for user: " + username + ", Title: " + title);
                                    new AlertDialog.Builder(HomeScreen.this)
                                            .setTitle(title)
                                            .setMessage(message)
                                            .setPositiveButton("OK", null)
                                            .show();

                                    NotificationHelper.sendNotification(HomeScreen.this, title, message, 1001);
                                }
                            })
                            .addOnFailureListener(e ->
                                    Log.e("HomeScreen", "Error fetching transactions for user " + username + ": " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        Log.e("HomeScreen", "Error checking budget limit for user " + username + ": " + e.getMessage()));
    }
    private void loadTransactions(String selectedMonth) {
        String username = tvUserName.getText().toString().trim().toLowerCase();
        if (username.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        transactionContainer.removeAllViews();
        transactionsScroll.setVisibility(View.GONE);
        findViewById(R.id.ic_transaction).setVisibility(View.VISIBLE);
        findViewById(R.id.texttransaction).setVisibility(View.VISIBLE);

        db.collection("users").document(username).collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Map<String, Object>> allTxs = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> txMap = doc.getData();
                        if (txMap != null) {
                            txMap.put("id", doc.getId());
                            allTxs.add(txMap);
                        }
                    }

                    if (allTxs.isEmpty()) return;

                    ArrayList<Map<String, Object>> uniqueTxs = new ArrayList<>();
                    ArrayList<String> seenIds = new ArrayList<>();
                    for (Map<String, Object> tx : allTxs) {
                        String id = (String) tx.get("id");
                        if (!seenIds.contains(id)) {
                            uniqueTxs.add(tx);
                            seenIds.add(id);
                        }
                    }

                    Collections.sort(uniqueTxs, (o1, o2) -> {
                        try {
                            com.google.firebase.Timestamp t1 = (com.google.firebase.Timestamp) o1.get("timestamp");
                            com.google.firebase.Timestamp t2 = (com.google.firebase.Timestamp) o2.get("timestamp");
                            return t2.toDate().compareTo(t1.toDate());
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    ArrayList<Map<String, Object>> filteredTxs = new ArrayList<>();
                    for (Map<String, Object> tx : uniqueTxs) {
                        try {
                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) tx.get("timestamp");
                            if (timestamp != null) {
                                java.util.Date date = timestamp.toDate();
                                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
                                String txMonth = monthFormat.format(date);

                                if ("This Month".equals(selectedMonth)) {
                                    SimpleDateFormat thisMonthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                                    String txMonthYear = thisMonthFormat.format(date);
                                    String currentMonthYear = thisMonthFormat.format(new java.util.Date());
                                    if (txMonthYear.equals(currentMonthYear)) filteredTxs.add(tx);
                                } else if (txMonth.equalsIgnoreCase(selectedMonth)) {
                                    filteredTxs.add(tx);
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    if (filteredTxs.isEmpty()) {
                        transactionsScroll.setVisibility(View.GONE);
                        findViewById(R.id.ic_transaction).setVisibility(View.VISIBLE);
                        findViewById(R.id.texttransaction).setVisibility(View.VISIBLE);
                        lblexpense.setText("Expense\n₹0");
                        lblincome.setText("Income\n₹0");
                        lblbalance.setText("Balance\n₹0");
                        return;
                    }

                    double totalExpense = 0.0, totalIncome = 0.0;
                    for (Map<String, Object> tx : filteredTxs) {
                        String type = (String) tx.get("type");
                        String amountStr = (String) tx.get("amount");
                        double amount = 0.0;
                        try { amount = Double.parseDouble(amountStr.replaceAll("[^0-9.]", "")); } catch (Exception ignored) {}
                        if ("expense".equalsIgnoreCase(type)) totalExpense += amount;
                        else if ("income".equalsIgnoreCase(type)) totalIncome += amount;
                    }
                    String rupee = "₹";
                    lblexpense.setText("Expense\n" + rupee + String.format(Locale.getDefault(), "%.2f", totalExpense));
                    lblincome.setText("Income\n" + rupee + String.format(Locale.getDefault(), "%.2f", totalIncome));
                    lblbalance.setText("Balance\n" + rupee + String.format(Locale.getDefault(), "%.2f", totalIncome - totalExpense));

                    transactionContainer.removeAllViews();
                    transactionsScroll.setVisibility(View.VISIBLE);
                    findViewById(R.id.ic_transaction).setVisibility(View.GONE);
                    findViewById(R.id.texttransaction).setVisibility(View.GONE);

                    for (int i = 0; i < Math.min(3, filteredTxs.size()); i++) {
                        Map<String, Object> tx = filteredTxs.get(i);

                        MaterialCardView cardView = new MaterialCardView(HomeScreen.this);
                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cardParams.setMargins(16, 16, 16, 16);
                        cardView.setLayoutParams(cardParams);
                        cardView.setCardElevation(4);
                        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                        cardView.setRadius(12);

                        LinearLayout cardLayout = new LinearLayout(HomeScreen.this);
                        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                        cardLayout.setPadding(16, 12, 16, 12);

                        // Amount
                        TextView amountText = new TextView(HomeScreen.this);
                        String amountStr = (String) tx.get("amount");
                        amountText.setText(rupee + amountStr);
                        amountText.setTextSize(16);
                        // Set color based on transaction type
                        String type = (String) tx.get("type");
                        if ("expense".equalsIgnoreCase(type)) {
                            amountText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Approx #D32F2F
                        } else if ("income".equalsIgnoreCase(type)) {
                            amountText.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Approx #2E7D32
                        }
                        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                        );
                        amountText.setLayoutParams(amountParams);

                        // Description
                        TextView descText = new TextView(HomeScreen.this);
                        String note = (String) tx.get("note");
                        String category = (String) tx.get("category");
                        String prefix = "expense".equalsIgnoreCase(type) ? "spent at" : "credited by";
                        String detail = (category != null && !category.isEmpty()) ? category : (note != null ? note : "");
                        descText.setText(prefix + " " + detail);
                        descText.setTextSize(16);
                        descText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2
                        );
                        descText.setLayoutParams(descParams);

                        cardLayout.addView(amountText);
                        cardLayout.addView(descText);

                        cardView.addView(cardLayout);
                        transactionContainer.addView(cardView);
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeScreen", "Error loading transactions: " + e.getMessage()));
    }

    private void loadReminders(String selectedMonth) {
        String username = tvUserName.getText().toString().trim().toLowerCase();
        if (username.isEmpty()) return;

        Log.d("HomeScreen", "Loading reminders for user: " + username);

        reminderContainer.removeAllViews();
        reminderScroll.setVisibility(View.GONE);
        findViewById(R.id.ic_reminder).setVisibility(View.VISIBLE);
        findViewById(R.id.txtreminder).setVisibility(View.VISIBLE);

        db.collection("users").document(username).collection("reminders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Map<String, Object>> allReminders = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat thisMonthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                    String currentMonthYear = thisMonthFormat.format(new java.util.Date());

                    // Collect only current user's reminders
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (!doc.exists()) continue;

                        // Double-check reminder belongs to logged-in user
                        String docUser = doc.getReference().getParent().getParent().getId();
                        if (docUser == null || !docUser.equalsIgnoreCase(username)) {
                            Log.d("HomeScreen", "Skipping reminder from user: " + docUser + ", expected: " + username);
                            continue;
                        }

                        Map<String, Object> rMap = doc.getData();
                        if (rMap != null) {
                            for (String key : rMap.keySet()) {
                                Object value = rMap.get(key);
                                if (value instanceof Map) {
                                    Map<String, Object> noteMap = (Map<String, Object>) value;
                                    noteMap.put("id", doc.getId() + "/" + key);
                                    allReminders.add(noteMap);
                                }
                            }
                        }
                    }

                    Log.d("HomeScreen", "Total reminders found for user " + username + ": " + allReminders.size());

                    ArrayList<Map<String, Object>> upcomingReminders = new ArrayList<>();
                    int notificationCounter = 2000;

                    for (Map<String, Object> note : allReminders) {
                        try {
                            String dateStr = (String) note.get("reminderDate");
                            if (dateStr == null) continue;
                            java.util.Date date = sdf.parse(dateStr);

                            String reminderMonthYear = thisMonthFormat.format(date);
                            if (currentMonthYear.equals(reminderMonthYear)) {
                                upcomingReminders.add(note);

                                // Only notify logged-in user
                                if (!notificationsSent) {
                                    String reminderNote = (String) note.get("note");
                                    String title = "🔔 Reminder for " + username;
                                    String message = (reminderNote != null ? reminderNote : "No note") + " on " + dateStr;
                                    Log.d("HomeScreen", "Sending reminder notification for user: " + username + ", Title: " + title + ", Message: " + message);
                                    NotificationHelper.sendNotification(HomeScreen.this, title, message, notificationCounter++);
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    Log.d("HomeScreen", "Upcoming reminders for user " + username + ": " + upcomingReminders.size());

                    // Mark notifications sent for this user session
                    notificationsSent = true;

                    // Display the first 3 reminders in the UI
                    reminderContainer.removeAllViews();
                    if (upcomingReminders.isEmpty()) {
                        reminderScroll.setVisibility(View.GONE);
                        findViewById(R.id.ic_reminder).setVisibility(View.VISIBLE);
                        findViewById(R.id.txtreminder).setVisibility(View.VISIBLE);
                    } else {
                        reminderScroll.setVisibility(View.VISIBLE);
                        findViewById(R.id.ic_reminder).setVisibility(View.GONE);
                        findViewById(R.id.txtreminder).setVisibility(View.GONE);
                    }

                    for (int i = 0; i < Math.min(3, upcomingReminders.size()); i++) {
                        Map<String, Object> note = upcomingReminders.get(i);
                        MaterialCardView cardView = new MaterialCardView(HomeScreen.this);
                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cardParams.setMargins(16, 16, 16, 16);
                        cardView.setLayoutParams(cardParams);
                        cardView.setCardElevation(4);
                        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                        cardView.setRadius(12);

                        LinearLayout cardLayout = new LinearLayout(HomeScreen.this);
                        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                        cardLayout.setPadding(16, 12, 16, 12);

                        // Note
                        TextView noteText = new TextView(HomeScreen.this);
                        noteText.setText((String) note.get("note"));
                        noteText.setTextSize(16);
                        noteText.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Approx #2E7D32
                        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2
                        );
                        noteText.setLayoutParams(noteParams);

                        // Date
                        TextView dateText = new TextView(HomeScreen.this);
                        dateText.setText((String) note.get("reminderDate"));
                        dateText.setTextSize(14);
                        dateText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                        );
                        dateParams.gravity = Gravity.END;
                        dateText.setLayoutParams(dateParams);

                        cardLayout.addView(noteText);
                        cardLayout.addView(dateText);

                        cardView.addView(cardLayout);
                        reminderContainer.addView(cardView);
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeScreen", "Error loading reminders for user " + username + ": " + e.getMessage()));
    }
}
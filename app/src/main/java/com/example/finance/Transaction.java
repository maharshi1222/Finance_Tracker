package com.example.finance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Transaction extends AppCompatActivity {
    private ImageView backarrow3, filter;
    private LinearLayout transactionContainer;
    private String userName;
    private String filterType;
    private int filterYear = -1;
    private int filterMonth = -1;
    private Calendar filterDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backarrow3 = findViewById(R.id.backArrow3);
        filter = findViewById(R.id.filter);
        transactionContainer = findViewById(R.id.transactionsContainer);

        userName = getIntent().getStringExtra("userName");
        if (userName != null) userName = userName.trim().toLowerCase();
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, "Authentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
            Log.e("Transaction", "Username is null or empty");
            startActivity(new Intent(this, Login.class));
            finish();
        }

        backarrow3.setOnClickListener(v -> finish());
        filter.setOnClickListener(v -> showFilterPopup(v));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userName != null && !userName.isEmpty()) {
            loadTransactions();
        }
    }

    private void loadTransactions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        transactionContainer.removeAllViews();

        Log.d("Transaction", "Loading transactions for user: " + userName);

        db.collection("users").document(userName).collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("Transaction", "Fetched " + querySnapshot.size() + " transaction documents");
                    ArrayList<Map<String, Object>> allTxs = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> txMap = doc.getData();
                        if (txMap != null) {
                            txMap.put("id", doc.getId());
                            allTxs.add(txMap);
                            Log.d("Transaction", "Transaction ID: " + doc.getId() + ", transactionDate: " + txMap.get("transactionDate"));
                        } else {
                            Log.w("Transaction", "Empty data for transaction document ID: " + doc.getId());
                        }
                    }

                    if (allTxs.isEmpty()) {
                        Log.d("Transaction", "No transactions found for user: " + userName);
                        TextView noTransactions = new TextView(this);
                        noTransactions.setText("No Transactions Yet");
                        noTransactions.setTextSize(16);
                        noTransactions.setGravity(Gravity.CENTER);
                        transactionContainer.addView(noTransactions);
                        return;
                    }

                    // Remove duplicates
                    ArrayList<Map<String, Object>> uniqueTxs = new ArrayList<>();
                    ArrayList<String> seenIds = new ArrayList<>();
                    for (Map<String, Object> tx : allTxs) {
                        String id = (String) tx.get("id");
                        if (id != null && !seenIds.contains(id)) {
                            uniqueTxs.add(tx);
                            seenIds.add(id);
                        } else {
                            Log.w("Transaction", "Duplicate or null ID found: " + id);
                        }
                    }
                    Log.d("Transaction", "Unique transactions count: " + uniqueTxs.size());

                    // Date formats - FIXED: Use YYYY-MM-DD for database parsing
                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    // Filter transactions
                    ArrayList<Map<String, Object>> filteredTxs = new ArrayList<>();
                    for (Map<String, Object> tx : uniqueTxs) {
                        String txDateStr = (String) tx.get("transactionDate");
                        if (txDateStr == null || txDateStr.isEmpty()) {
                            Log.w("Transaction", "Transaction missing transactionDate: " + tx.get("id"));
                            continue;
                        }

                        try {
                            Date txDate = dbFormat.parse(txDateStr);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(txDate);

                            boolean matches = false;
                            if (filterType == null || filterType.isEmpty()) {
                                matches = true;
                            } else if ("Date".equals(filterType) && filterDate != null) {
                                matches = cal.get(Calendar.YEAR) == filterDate.get(Calendar.YEAR) &&
                                        cal.get(Calendar.MONTH) == filterDate.get(Calendar.MONTH) &&
                                        cal.get(Calendar.DAY_OF_MONTH) == filterDate.get(Calendar.DAY_OF_MONTH);
                            } else if ("Month".equals(filterType) && filterMonth != -1 && filterYear != -1) {
                                matches = cal.get(Calendar.YEAR) == filterYear &&
                                        cal.get(Calendar.MONTH) == filterMonth;
                            } else if ("Year".equals(filterType) && filterYear != -1) {
                                matches = cal.get(Calendar.YEAR) == filterYear;
                            }

                            if (matches) {
                                // Add formatted date for UI in dd/MM/yyyy
                                String formattedDate = displayFormat.format(txDate);
                                tx.put("formattedDate", formattedDate);
                                filteredTxs.add(tx);
                                Log.d("Transaction", "Added to filtered: " + tx.get("id") + " (" + formattedDate + ")");
                            }

                        } catch (Exception e) {
                            Log.e("Transaction", "Error parsing transactionDate '" + txDateStr + "' for ID: " + tx.get("id") + ", error: " + e.getMessage());
                        }
                    }

                    Log.d("Transaction", "Filtered transactions count: " + filteredTxs.size());

                    if (filteredTxs.isEmpty()) {
                        Log.d("Transaction", "No transactions found for filter: " + filterType);
                        TextView noTransactions = new TextView(this);
                        noTransactions.setText("No Transactions for this filter");
                        noTransactions.setTextSize(16);
                        noTransactions.setGravity(Gravity.CENTER);
                        transactionContainer.addView(noTransactions);
                        return;
                    }

                    // Sort filtered transactions by original date descending (latest first)
                    Collections.sort(filteredTxs, (o1, o2) -> {
                        try {
                            Date d1 = dbFormat.parse((String) o1.get("transactionDate"));
                            Date d2 = dbFormat.parse((String) o2.get("transactionDate"));
                            return d2.compareTo(d1);
                        } catch (Exception e) {
                            Log.e("Transaction", "Error sorting transactions: " + e.getMessage());
                            return 0;
                        }
                    });

                    // Display transactions grouped by formattedDate
                    String lastDate = "";
                    for (Map<String, Object> tx : filteredTxs) {
                        String formattedDate = (String) tx.get("formattedDate");
                        if (formattedDate != null && !formattedDate.equals(lastDate)) {
                            lastDate = formattedDate;
                            TextView dateHeader = new TextView(this);
                            dateHeader.setText(formattedDate);
                            dateHeader.setTextSize(16);
                            dateHeader.setPadding(16, 16, 16, 8);
                            dateHeader.setGravity(Gravity.CENTER);
                            transactionContainer.addView(dateHeader);
                        } else if (formattedDate == null) {
                            Log.w("Transaction", "Formatted date is null for transaction ID: " + tx.get("id"));
                        }

                        // --- Transaction Card ---
                        MaterialCardView cardView = new MaterialCardView(this);
                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cardParams.topMargin = 8;
                        cardParams.bottomMargin = 8;
                        cardView.setLayoutParams(cardParams);
                        cardView.setCardElevation(4);
                        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                        cardView.setRadius(12);

                        LinearLayout cardLayout = new LinearLayout(this);
                        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                        cardLayout.setPadding(16, 12, 16, 12);

                        // Amount
                        TextView txText = new TextView(this);
                        String type = (String) tx.get("type");
                        String amount = (String) tx.get("amount");
                        txText.setText(amount);
                        txText.setTextSize(16);
                        // Set color based on transaction type
                        if ("expense".equalsIgnoreCase(type)) {
                            txText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Approx #D32F2F
                        } else if ("income".equalsIgnoreCase(type)) {
                            txText.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Approx #2E7D32
                        }
                        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                        );
                        txText.setLayoutParams(amountParams);

                        // Description
                        TextView descText = new TextView(this);
                        String note = (String) tx.get("note");
                        String category = (String) tx.get("category");
                        String prefix = "expense".equalsIgnoreCase(type) ? "spent at" : "income".equalsIgnoreCase(type) ? "credited by" : "";
                        String detail = (note != null && !note.isEmpty()) ? note : (category != null && !category.isEmpty() ? category : "");
                        descText.setText(prefix + " " + detail);
                        descText.setTextSize(16);
                        descText.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Approx #757575
                        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2
                        );
                        descText.setLayoutParams(descParams);

                        cardLayout.addView(txText);
                        cardLayout.addView(descText);

                        cardView.addView(cardLayout);
                        transactionContainer.addView(cardView);
                    }

                    Log.d("Transaction", "Displayed " + filteredTxs.size() + " transactions");
                })
                .addOnFailureListener(e -> {
                    Log.e("Transaction", "Error loading transactions: " + e.getMessage());
                    Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);
        popup.getMenu().add("Date");
        popup.getMenu().add("Month");
        popup.getMenu().add("Year");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            filterType = title;
            switch (title) {
                case "Date":
                    showDatePicker();
                    return true;
                case "Month":
                    showMonthPicker();
                    return true;
                case "Year":
                    showYearPicker();
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    filterDate = Calendar.getInstance();
                    filterDate.set(year, month, dayOfMonth);
                    loadTransactions();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis()); // Limit to today and before
        dialog.show();
    }

    private void showMonthPicker() {
        final String[] months = new DateFormatSymbols().getShortMonths(); // Jan, Feb, ...

        // Create a GridView
        GridView gridView = new GridView(this);
        gridView.setNumColumns(3); // 3 columns
        gridView.setHorizontalSpacing(20);
        gridView.setVerticalSpacing(20);
        gridView.setPadding(20, 20, 20, 20);

        // Adapter to show month names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, months);
        gridView.setAdapter(adapter);

        // Build AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Month");
        builder.setView(gridView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle month click
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            filterMonth = position;
            dialog.dismiss();
            showYearPickerForMonth();
        });
    }

    private void showYearPickerForMonth() {
        NumberPicker picker = new NumberPicker(this);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        picker.setMinValue(currentYear - 10); // Adjust range as needed, e.g., last 10 years
        picker.setMaxValue(currentYear);
        picker.setValue(currentYear);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Year");
        builder.setView(picker);
        builder.setPositiveButton("OK", (dialog, which) -> {
            filterYear = picker.getValue();
            loadTransactions();
        });
        builder.show();
    }

    private void showYearPicker() {
        NumberPicker picker = new NumberPicker(this);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        picker.setMinValue(currentYear - 10); // Adjust range as needed, e.g., last 10 years
        picker.setMaxValue(currentYear);
        picker.setValue(currentYear);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Year");
        builder.setView(picker);
        builder.setPositiveButton("OK", (dialog, which) -> {
            filterYear = picker.getValue();
            loadTransactions();
        });
        builder.show();
    }
}
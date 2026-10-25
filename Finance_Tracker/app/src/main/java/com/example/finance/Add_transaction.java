package com.example.finance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Add_transaction extends AppCompatActivity {

    private MaterialCardView spending, income;
    private ImageView expenseCheck, incomeCheck, backarrow, icCalendar;
    private EditText etAmount, etNote;
    private Spinner spCategory;
    private Button btnAdd;
    private TextView tvDate, tvTime;

    private String UserName;
    private Handler handler = new Handler();
    private Runnable updateTimeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_transaction);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spending = findViewById(R.id.spending);
        income = findViewById(R.id.income);
        expenseCheck = findViewById(R.id.expensecheck);
        incomeCheck = findViewById(R.id.incomecheck);
        backarrow = findViewById(R.id.backArrow);
        icCalendar = findViewById(R.id.icCalendar);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        btnAdd = findViewById(R.id.btnAddtransaction);
        tvDate = findViewById(R.id.Date);
        tvTime = findViewById(R.id.Time);
        spCategory = findViewById(R.id.spCategory);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.category_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Optional: handle selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        UserName = getIntent().getStringExtra("userName");
        if (UserName != null) UserName = UserName.trim().toLowerCase();

        startTimeUpdate();

        spending.setOnClickListener(v -> {
            expenseCheck.setVisibility(View.VISIBLE);
            incomeCheck.setVisibility(View.GONE);
        });

        income.setOnClickListener(v -> {
            incomeCheck.setVisibility(View.VISIBLE);
            expenseCheck.setVisibility(View.GONE);
        });

        backarrow.setOnClickListener(v -> finish());
        icCalendar.setOnClickListener(v -> openDatePicker());
        btnAdd.setOnClickListener(v -> saveTransaction());
    }

    private void startTimeUpdate() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                tvDate.setText(dateFormat.format(now));
                tvTime.setText(timeFormat.format(now));

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void openDatePicker() {
        handler.removeCallbacks(updateTimeRunnable);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedCalendar.getTime());
                    tvDate.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void saveTransaction() {
        if (UserName == null || UserName.isEmpty()) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = expenseCheck.getVisibility() == View.VISIBLE ? "Expense" :
                incomeCheck.getVisibility() == View.VISIBLE ? "Income" : "";

        if (type.isEmpty()) {
            Toast.makeText(this, "Please select Expense or Income", Toast.LENGTH_SHORT).show();
            return;
        }

        String amount = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (amount.isEmpty() || category.equals("Select your category")) {
            Toast.makeText(this, "Please enter amount and select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String transactionDate = tvDate.getText().toString();
        String transactionTime = timeFormat.format(now);
        Timestamp timestamp = new Timestamp(now);

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", type);
        transaction.put("amount", amount);
        transaction.put("note", note);
        transaction.put("category", category);
        transaction.put("transactionDate", transactionDate);
        transaction.put("transactionTime", transactionTime);
        transaction.put("timestamp", timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(UserName)
                .collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int nextIndex = querySnapshot.size() + 1;
                    db.collection("users")
                            .document(UserName)
                            .collection("transactions")
                            .document("transaction" + nextIndex)
                            .set(transaction)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to add transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching transactions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable);
    }
}

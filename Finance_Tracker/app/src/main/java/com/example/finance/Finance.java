package com.example.finance;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Finance extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 101;
    private String userName;
    private Spinner spExportType, spBudgetLimit, spMonth;
    private Button btnExport, btnChange;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_finance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    Log.d("Finance", "Permission granted: " + isGranted);
                    if (isGranted) {
                        btnExport.performClick(); // Retry export if permission granted
                    } else {
                        // Check if "Never ask again" is selected
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showPermissionDeniedPermanentlyDialog();
                        } else {
                            showPermissionDeniedDialog();
                        }
                    }
                }
        );

        userName = getIntent().getStringExtra("userName").toLowerCase(); // Store username in lowercase

        spExportType = findViewById(R.id.spExportType);
        String[] exportOptions = {"Select the export type", "csv", "pdf"};
        ArrayAdapter<String> exportAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exportOptions);
        exportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExportType.setAdapter(exportAdapter);

        spBudgetLimit = findViewById(R.id.spBudgetLimit);
        String[] budgetOptions = {"Select your monthly budget","50000 or less","100000", "200000", "300000", "400000", "500000", "1000000 or more"};
        ArrayAdapter<String> budgetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, budgetOptions);
        budgetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBudgetLimit.setAdapter(budgetAdapter);

        spMonth = findViewById(R.id.spMonth);
        String[] monthOptions = {"Select Month", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthOptions);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        ImageView backArrow = findViewById(R.id.backArrow5);
        backArrow.setOnClickListener(v -> finish());

        btnExport = findViewById(R.id.btnExport);
        btnChange = findViewById(R.id.btnChange);

        btnChange.setOnClickListener(v -> {
            String selectedLimit = spBudgetLimit.getSelectedItem().toString();
            String selectedMonth = spMonth.getSelectedItem().toString();
            if (!selectedLimit.equals("Select your monthly budget") && !selectedMonth.equals("Select Month")) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String docId = userName;
                Map<String, Object> budgetUpdate = new HashMap<>();
                budgetUpdate.put("username", userName);

                db.collection("users").document(userName).collection("budgetlimit").document(docId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            List<Map<String, Object>> budgetHistory = new ArrayList<>();
                            if (documentSnapshot.exists()) {
                                budgetHistory = (List<Map<String, Object>>) documentSnapshot.get("budgetHistory");
                                if (budgetHistory == null) {
                                    budgetHistory = new ArrayList<>();
                                } else {
                                    for (Map<String, Object> entry : budgetHistory) {
                                        String existingMonth = (String) entry.get("month");
                                        if (existingMonth != null && existingMonth.equals(selectedMonth)) {
                                            new AlertDialog.Builder(this)
                                                    .setMessage("Budget for " + selectedMonth + " already exists")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            return;
                                        }
                                    }
                                }
                            }

                            Map<String, Object> newEntry = new HashMap<>();
                            newEntry.put("month", selectedMonth);
                            newEntry.put("limit", selectedLimit);
                            budgetHistory.add(newEntry);

                            budgetUpdate.put("budgetHistory", budgetHistory);
                            db.collection("users").document(userName).collection("budgetlimit").document(docId)
                                    .set(budgetUpdate)
                                    .addOnSuccessListener(aVoid -> {
                                        new AlertDialog.Builder(this)
                                                .setMessage("Budget added for " + selectedMonth)
                                                .setCancelable(false)
                                                .setPositiveButton("OK", (dialog, which) -> {
                                                    Log.d("Finance", "OK button clicked");
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(Finance.this, More.class);
                                                    intent.putExtra("userName", userName);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .show();
                                    })
                                    .addOnFailureListener(e -> {
                                        new AlertDialog.Builder(this)
                                                .setMessage("Error adding budget: " + e.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            new AlertDialog.Builder(this)
                                    .setMessage("Error fetching data: " + e.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Please select a budget limit and month")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        btnExport.setOnClickListener(v -> {
            String selectedType = spExportType.getSelectedItem().toString().toLowerCase();
            if (!selectedType.equals("select the export type")) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Fetch both collections
                db.collection("users").document(userName).collection("transactions")
                        .get()
                        .addOnSuccessListener(transactionsQuery -> {
                            db.collection("users").document(userName).collection("budgetlimit")
                                    .get()
                                    .addOnSuccessListener(budgetQuery -> {
                                        List<Map<String, Object>> transactions = new ArrayList<>();
                                        for (DocumentSnapshot doc : transactionsQuery.getDocuments()) {
                                            Map<String, Object> data = doc.getData();
                                            if (data != null) {
                                                data.put("id", doc.getId());
                                                transactions.add(data);
                                            }
                                        }

                                        List<Map<String, Object>> budgets = new ArrayList<>();
                                        for (DocumentSnapshot doc : budgetQuery.getDocuments()) {
                                            Map<String, Object> data = doc.getData();
                                            if (data != null) {
                                                data.put("id", doc.getId());
                                                budgets.add(data);
                                            }
                                        }

                                        try {
                                            exportBothCollections(transactions, budgets, selectedType);
                                        } catch (Exception e) {
                                            Log.e("Export", "Error exporting", e);
                                            new AlertDialog.Builder(this)
                                                    .setMessage("Error exporting: " + e.getMessage())
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        new AlertDialog.Builder(this)
                                                .setMessage("Error fetching budgetlimit: " + e.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            new AlertDialog.Builder(this)
                                    .setMessage("Error fetching transactions: " + e.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Please select an export type")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

    }

    private void exportBothCollections(List<Map<String, Object>> transactions, List<Map<String, Object>> budgets, String type) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());
        String fileName = "FinanceData_" + userName + "_" + timestamp + "." + type;

        //MediaStore to save to Downloads folder
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, type.equals("csv") ? "text/csv" : "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FinanceApp");

        ContentResolver resolver = getContentResolver();
        Uri fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (fileUri == null) {
            throw new IOException("Failed to create file in MediaStore");
        }

        try (OutputStream outputStream = resolver.openOutputStream(fileUri)) {

            if (type.equals("csv")) {
                //CSV
                StringBuilder sb = new StringBuilder();
                sb.append("=== Transactions ===\n");
                sb.append("ID,Amount,Category,Note,TransactionDate,TransactionTime,Type\n");
                for (Map<String, Object> m : transactions) {
                    sb.append(m.getOrDefault("id", "") + "," +
                            m.getOrDefault("amount", "") + "," +
                            m.getOrDefault("category", "") + "," +
                            m.getOrDefault("note", "") + "," +
                            m.getOrDefault("transactionDate", "") + "," +
                            m.getOrDefault("transactionTime", "") + "," +
                            m.getOrDefault("type", "") + "\n");
                }

                sb.append("\n\n=== Budget Limit ===\n");
                sb.append("ID,Month,Limit\n");
                for (Map<String, Object> m : budgets) {
                    if (m.get("budgetHistory") instanceof List) {
                        List<Map<String, Object>> history = (List<Map<String, Object>>) m.get("budgetHistory");
                        for (Map<String, Object> entry : history) {
                            sb.append(m.get("id") + "," +
                                    entry.getOrDefault("month", "") + "," +
                                    entry.getOrDefault("limit", "") + "\n");
                        }
                    }
                }

                outputStream.write(sb.toString().getBytes());

            } else {
                //PDF
                PdfDocument pdf = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = pdf.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(12);

                int y = 60;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String generatedDate = sdf.format(new Date());

                // Header
                paint.setTextSize(14);
                canvas.drawText("Finance Report for: " + userName, 50, y, paint);
                y += 25;
                paint.setTextSize(12);
                canvas.drawText("Generated on: " + generatedDate, 50, y, paint);
                y += 30;

                // Transactions Section
                canvas.drawText("------------------------------------------------------------", 50, y, paint);
                y += 20;
                paint.setTextSize(13);
                canvas.drawText("TRANSACTIONS", 50, y, paint);
                paint.setTextSize(12);
                y += 20;
                canvas.drawText("------------------------------------------------------------", 50, y, paint);
                y += 20;
                canvas.drawText("Date        Time    Type      Category   Amount     Note", 50, y, paint);
                y += 20;
                canvas.drawText("------------------------------------------------------------", 50, y, paint);
                y += 20;

                for (Map<String, Object> m : transactions) {
                    String date = String.valueOf(m.getOrDefault("transactionDate", ""));
                    String time = String.valueOf(m.getOrDefault("transactionTime", ""));
                    String txnType = String.valueOf(m.getOrDefault("type", "")); // ✅ renamed here
                    String category = String.valueOf(m.getOrDefault("category", ""));
                    String amount = String.valueOf(m.getOrDefault("amount", ""));
                    String note = String.valueOf(m.getOrDefault("note", ""));
                    String line = String.format(Locale.getDefault(), "%-10s %-7s %-9s %-10s %-10s %s",
                            date, time, txnType, category, amount, note);
                    canvas.drawText(line, 50, y, paint);
                    y += 20;
                    if (y > 750) { // add new page if overflow
                        pdf.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                        page = pdf.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = 50;
                    }
                }

                // Budget Section
                y += 30;
                canvas.drawText("------------------------------------------------------------", 50, y, paint);
                y += 20;
                paint.setTextSize(13);
                canvas.drawText("BUDGET LIMITS", 50, y, paint);
                paint.setTextSize(12);
                y += 20;
                canvas.drawText("------------------------------------------------------------", 50, y, paint);
                y += 20;
                canvas.drawText("Month       Budget Limit", 50, y, paint);
                y += 20;
                canvas.drawText("------------------------------------------------------------", 50, y, paint);
                y += 20;

                for (Map<String, Object> m : budgets) {
                    if (m.get("budgetHistory") instanceof List) {
                        List<Map<String, Object>> history = (List<Map<String, Object>>) m.get("budgetHistory");
                        for (Map<String, Object> entry : history) {
                            String month = String.valueOf(entry.get("month"));
                            String limit = String.valueOf(entry.get("limit"));
                            String line = String.format(Locale.getDefault(), "%-10s ₹%s", month, limit);
                            canvas.drawText(line, 50, y, paint);
                            y += 20;
                        }
                    }
                }
                pdf.finishPage(page);
                pdf.writeTo(outputStream);
                pdf.close();
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Export Successful ✅")
                .setMessage("File saved to Downloads/FinanceApp\n\nFile name:\n" + fileName)
                .setPositiveButton("Open", (dialog, which) -> openExportedFile(fileUri, type))
                .setNegativeButton("Close", null)
                .show();
    }

    private void openExportedFile(Uri fileUri, String type) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, type.equals("csv") ? "text/csv" : "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app found to open this file type", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Storage permission denied. Please allow permission to export.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPermissionDeniedPermanentlyDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Storage permission denied permanently. Please enable it in Settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
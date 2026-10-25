package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Analysis extends AppCompatActivity {

    private ImageView arrow6;
    private TextView tvTotalIncome, tvTotalExpense;
    private PieChart pieChart;
    private BarChart barChart;
    private LinearLayout homeNav,analysisNav,moreNav;
    private FirebaseFirestore db;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userName = getIntent().getStringExtra("userName");
        if (userName != null) {
            userName = userName.trim().toLowerCase();
        }

        arrow6 = findViewById(R.id.backArrow6);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        homeNav = findViewById(R.id.homeNav);
        analysisNav = findViewById(R.id.analysisNav);
        moreNav = findViewById(R.id.moreNav);

        homeNav.setOnClickListener(v -> {
            Intent intent = new Intent(Analysis.this, HomeScreen.class);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        analysisNav.setOnClickListener(v -> {
            Intent intent = new Intent(Analysis.this, Analysis.class);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        moreNav.setOnClickListener(v -> {
            Intent intent = new Intent(Analysis.this, More.class);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });
        arrow6.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        loadFirebaseData();
    }

    private void loadFirebaseData() {
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference transactionsRef = db.collection("users")
                .document(userName)
                .collection("transactions");

        transactionsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0;
                    double totalExpense = 0;
                    HashMap<String, Float> categoryExpenseMap = new HashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("type");
                        String category = doc.getString("category");

                        Double amount = 0.0;
                        Object amountObj = doc.get("amount");
                        if (amountObj instanceof Number) {
                            amount = ((Number) amountObj).doubleValue();
                        } else if (amountObj instanceof String) {
                            try {
                                amount = Double.parseDouble(((String) amountObj).replaceAll("[^0-9.]", ""));
                            } catch (NumberFormatException ignored) {
                                amount = 0.0;
                            }
                        }

                        if (type == null || category == null) continue;

                        if (type.equalsIgnoreCase("Income")) {
                            totalIncome += amount;
                        } else if (type.equalsIgnoreCase("Expense")) {
                            totalExpense += amount;
                            categoryExpenseMap.put(category,
                                    categoryExpenseMap.getOrDefault(category, 0f) + amount.floatValue());
                        }
                    }

                    tvTotalIncome.setText("Total Income: ₹" + totalIncome);
                    tvTotalExpense.setText("Total Expense: ₹" + totalExpense);

                    setupPieChart(categoryExpenseMap);
                    setupBarChart((float) totalIncome, (float) totalExpense);

                    String suggestions = generateSuggestions(totalIncome, totalExpense, categoryExpenseMap);
                    TextView tvSuggestions = findViewById(R.id.tvSuggestions);
                    tvSuggestions.setText(suggestions);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(Analysis.this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateSuggestions(double totalIncome, double totalExpense, HashMap<String, Float> categoryExpenseMap) {
        StringBuilder sb = new StringBuilder();

        //Overall income vs expense
        if (totalIncome > totalExpense) {
            sb.append("Great! You are saving money. Keep tracking your expenses.\n");
        } else if (totalIncome < totalExpense) {
            sb.append("Warning: You are spending more than you earn. Try to reduce unnecessary expenses.\n");
        } else {
            sb.append("Your income and expenses are equal. Monitor spending to save more.\n");
        }

        //Highest spending category
        if (!categoryExpenseMap.isEmpty()) {
            String maxCategory = null;
            float maxAmount = 0f;
            for (Map.Entry<String, Float> entry : categoryExpenseMap.entrySet()) {
                if (entry.getValue() > maxAmount) {
                    maxAmount = entry.getValue();
                    maxCategory = entry.getKey();
                }
            }

            if (maxCategory != null) {
                sb.append("You spent the most on ").append(maxCategory).append(". Consider budgeting this category.\n");
            }
        }

        //General advice
        sb.append("Regularly review your spending habits to improve financial health.");

        return sb.toString();
    }

    private void setupPieChart(HashMap<String, Float> categoryExpenseMap) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryExpenseMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(16f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupBarChart(float income, float expense) {
        // Prepare entries
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, income));   // 0 → Income
        entries.add(new BarEntry(1, expense));  // 1 → Expense

        // Create DataSet
        BarDataSet dataSet = new BarDataSet(entries, "Income vs Expense");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);  // colorful bars
        dataSet.setValueTextSize(14f);                     // value text size

        // Create BarData
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);  // width of the bars

        // Setup BarChart
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);      // hide description
        barChart.getXAxis().setDrawLabels(false);         // hide X-axis labels
        barChart.getAxisRight().setEnabled(false);       // hide right Y-axis

        // Disable zoom and drag
        barChart.setPinchZoom(false);                     // disable pinch zoom
        barChart.setDoubleTapToZoomEnabled(false);       // disable double-tap zoom
        barChart.setScaleEnabled(false);                 // disable all scaling
        barChart.setDragEnabled(false);                  // disable dragging
        barChart.setTouchEnabled(true);                  // allow touch for value selection

        // Animate chart
        barChart.animateY(1000);

        // Add click listener to show bar info
        barChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                int xIndex = (int) e.getX();
                float value = e.getY();
                String label = (xIndex == 0) ? "Income" : "Expense";
                Toast.makeText(Analysis.this, label + ": ₹" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {
                // do nothing
            }
        });

        barChart.invalidate();
    }
}

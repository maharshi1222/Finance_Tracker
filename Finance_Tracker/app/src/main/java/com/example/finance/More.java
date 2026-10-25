package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;

public class More extends AppCompatActivity {

    private TextView moreUserName;
    private LinearLayout homeLayout, analysisLayout, moreLayout;
    private MaterialCardView changeinformation, finance, aboutapp, signout,feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        moreUserName = findViewById(R.id.moreUserName);
        String UserName = getIntent().getStringExtra("userName");

        if (UserName != null && !UserName.isEmpty()) {
            moreUserName.setText(UserName);
        }

        homeLayout = findViewById(R.id.morehome);
        analysisLayout = findViewById(R.id.moreanalysis);
        moreLayout = findViewById(R.id.Mmore);
        changeinformation = findViewById(R.id.changeinformation);
        finance = findViewById(R.id.finance);
        aboutapp = findViewById(R.id.aboutapp);
        feedback=findViewById(R.id.feedback);
        signout = findViewById(R.id.signout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this, HomeScreen.class);
                intent.putExtra("userName", UserName.toLowerCase());
                startActivity(intent);
            }
        });

        analysisLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this, Analysis.class);
                intent.putExtra("userName", UserName.toLowerCase());
                startActivity(intent);
            }
        });

        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this, More.class);
                intent.putExtra("userName", UserName.toLowerCase());
                startActivity(intent);
            }
        });

        changeinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this, ChangeInformation.class);
                intent.putExtra("userName", UserName.toLowerCase());
                startActivity(intent);
            }
        });

        finance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this, Finance.class);
                intent.putExtra("userName", UserName.toLowerCase());
                startActivity(intent);
            }
        });

        aboutapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this, AboutApp.class);
                intent.putExtra("userName", UserName.toLowerCase());
                startActivity(intent);
            }
        });

        feedback.setOnClickListener(v -> {
            Intent intent = new Intent(More.this, feedback.class);
            intent.putExtra("userName", UserName.toLowerCase());
            startActivity(intent);
        });


        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(More.this)
                        .setTitle("Logout")
                        .setMessage("Do you really want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(More.this, Login.class);
                            intent.putExtra("userName", UserName);
                            startActivity(intent);
                            finish(); // Close current activity
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }
}
package com.example.finance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    ImageView dot1,dot2,dot3;
    int currentScreen=1;
    Button btnproceeed,btnskip;
    ImageButton forward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        dot1.setColorFilter(currentScreen == 1 ? Color.BLACK : Color.GRAY);
        dot2.setColorFilter(currentScreen == 2 ? Color.BLACK : Color.GRAY);
        dot3.setColorFilter(currentScreen == 3 ? Color.BLACK : Color.GRAY);

        btnproceeed=findViewById(R.id.btnproceed);
        btnproceeed.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), Quote1.class);
            intent.putExtra("currentScreen", 2);
            startActivity(intent);
        });

        btnskip=findViewById(R.id.btnskip);
        btnskip.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
        });

        forward=findViewById(R.id.forward);
        forward.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
        });
    }
}
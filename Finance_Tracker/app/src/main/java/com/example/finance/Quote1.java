package com.example.finance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Quote1 extends AppCompatActivity {

    ImageView dot1,dot2,dot3;
    int currentScreen=1;
    Button btnnext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quote1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int passedScreen = getIntent().getIntExtra("currentScreen", 1);
        currentScreen = passedScreen;

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        dot1.setColorFilter(currentScreen == 1 ? Color.BLACK : Color.GRAY);
        dot2.setColorFilter(currentScreen == 2 ? Color.BLACK : Color.GRAY);
        dot3.setColorFilter(currentScreen == 3 ? Color.BLACK : Color.GRAY);

        btnnext=findViewById(R.id.btnready);
        btnnext.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), difference.class);
            intent.putExtra("currentScreen", 3);
            startActivity(intent);
        });
    }
}
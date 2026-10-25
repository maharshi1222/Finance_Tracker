package com.example.finance;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class signUp extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private TextView loginTab, signupTab;
    private View underline;
    private EditText nameField, emailField, passwordField, confirmPasswordField, phoneField;
    private Button signupBtn;
    private MaterialCardView googlebtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginTab = findViewById(R.id.login);
        signupTab = findViewById(R.id.Signup);
        underline = findViewById(R.id.indicator2);

        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password2);
        confirmPasswordField = findViewById(R.id.password3);
        phoneField = findViewById(R.id.phone);
        signupBtn = findViewById(R.id.btnsignup);
        googlebtn = findViewById(R.id.googlebtn);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        underline.post(() -> {
            int tabWidth = signupTab.getWidth();
            underline.getLayoutParams().width = tabWidth;
            underline.requestLayout();
            underline.setX(signupTab.getX());
        });

        loginTab.setOnClickListener(v -> animateUnderline(loginTab, Login.class));
        signupBtn.setOnClickListener(v -> registerUser());
        googlebtn.setOnClickListener(v -> signInWithGoogle());
    }

    private void registerUser() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Enter name");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Enter valid email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Enter password");
            return;
        } else if (!isStrongPassword(password)) {
            passwordField.setError("Password must be at least 8 characters, include uppercase, lowercase, number & special symbol");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            phoneField.setError("Enter valid phone number");
            return;
        }

        String safeName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String lowercaseName = name.toLowerCase(); // Store name in lowercase

        db.collection("users").document(safeName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(signUp.this, "User already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Prepare user data
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", lowercaseName); // Store name in lowercase
                        user.put("email", email);
                        user.put("password", password); // In production, use Firebase Auth
                        user.put("phone", phone);

                        // Save to Firestore with safeName as document ID
                        db.collection("users")
                                .document(safeName)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(signUp.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(signUp.this, Login.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(signUp.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(signUp.this, "Error checking user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isStrongPassword(String password) {
        // At least 8 chars, 1 upper, 1 lower, 1 number, 1 special char
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String email = account.getEmail();
                        String name = account.getDisplayName() != null ? account.getDisplayName() : "";
                        String phoneNumber = ""; // Google Sign-In typically doesn't provide phone number

                        String safeName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        String lowercaseName = name.toLowerCase(); // Store name in lowercase

                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("name", lowercaseName); // Store name in lowercase
                        user.put("password", "");
                        user.put("phoneNumber", phoneNumber);

                        db.collection("users").document(safeName).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(signUp.this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(signUp.this, HomeScreen.class);
                                    intent.putExtra("userName", lowercaseName); // Pass lowercase name
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(signUp.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(signUp.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void animateUnderline(TextView targetTab, Class<?> nextActivity) {
        float startX = underline.getX();
        float endX = targetTab.getX();

        ValueAnimator animator = ValueAnimator.ofFloat(startX, endX);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> underline.setX((float) animation.getAnimatedValue()));
        animator.start();

        underline.postDelayed(() -> {
            startActivity(new Intent(signUp.this, nextActivity));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 300);
    }
}
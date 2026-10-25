package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private TextView loginTab, signupTab;
    private View underline;
    private EditText emailPhoneField, passwordField;
    private Button loginBtn;
    private MaterialCardView googleBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // SHA-1 in Firebase required
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginTab = findViewById(R.id.Login);
        signupTab = findViewById(R.id.signup);
        underline = findViewById(R.id.indicator);
        emailPhoneField = findViewById(R.id.email_phone);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btnlogin);
        googleBtn = findViewById(R.id.googleBtn);

        underline.post(() -> {
            int tabWidth = loginTab.getWidth();
            underline.getLayoutParams().width = tabWidth;
            underline.requestLayout();
            underline.setX(loginTab.getX());
        });

        signupTab.setOnClickListener(v -> animateUnderline(signupTab, signUp.class));
        loginBtn.setOnClickListener(v -> loginUser());
        googleBtn.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginUser() {
        String input = emailPhoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            emailPhoneField.setError("Enter Email or Phone");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Enter Password");
            return;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            db.collection("users").whereEqualTo("email", input)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            DocumentSnapshot doc = query.getDocuments().get(0);
                            checkLogin(doc, password);
                        } else {
                            Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users").whereEqualTo("phone", input)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            DocumentSnapshot doc = query.getDocuments().get(0);
                            checkLogin(doc, password);
                        } else {
                            Toast.makeText(this, "Phone not registered", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void checkLogin(DocumentSnapshot document, String password) {
        if (document.exists()) {
            String storedPassword = document.getString("password");
            String userName = document.getString("name");
            if (storedPassword != null && storedPassword.equals(password)) {
                Intent intent = new Intent(Login.this, HomeScreen.class);
                intent.putExtra("userName", userName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
            }
        }
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
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);

                if (account != null) {
                    Log.d("GoogleSignIn", "Account retrieved: " + account.getEmail());
                    firebaseAuthWithGoogle(account);
                } else {
                    Log.e("GoogleSignIn", "Account is null after sign-in intent");
                    Toast.makeText(this, "Google Sign-In failed: Account null", Toast.LENGTH_SHORT).show();
                }

            } catch (ApiException e) {
                // Print full error details in Logcat
                Log.e("GoogleSignInError", "Sign-in failed. StatusCode: " + e.getStatusCode(), e);
                Toast.makeText(this, "Google Sign-In failed. Code: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();

                // Optional: interpret common errors
                switch (e.getStatusCode()) {
                    case 10:
                        Log.e("GoogleSignInError", "→ Developer error: Check your SHA-1 and default_web_client_id in Firebase.");
                        break;
                    case 12500:
                        Log.e("GoogleSignInError", "→ Firebase Auth failed: likely missing SHA-1 or mismatched OAuth client.");
                        break;
                    case 12501:
                        Log.e("GoogleSignInError", "→ User cancelled sign-in flow.");
                        break;
                    case 7:
                        Log.e("GoogleSignInError", "→ Network error. Check internet connection.");
                        break;
                    default:
                        Log.e("GoogleSignInError", "→ Unhandled error code. Check documentation.");
                        break;
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String email = firebaseUser.getEmail();
                            String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
                            saveUserToFirestore(email, name);
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String email, String name) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);
        user.put("password", "");
        user.put("phone", "");

        db.collection("users").document(email).set(user)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(Login.this, HomeScreen.class);
                    intent.putExtra("userName", name);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void animateUnderline(TextView targetTab, Class<?> nextActivity) {
        float startX = underline.getX();
        float endX = targetTab.getX();
        underline.animate().x(endX).setDuration(300).start();
        underline.postDelayed(() -> {
            Intent intent = new Intent(Login.this, nextActivity);
            startActivity(intent);
        }, 300);
    }
}

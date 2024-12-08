package com.example.navischool;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

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
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        textView = findViewById(R.id.registerNow);
        textView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(view -> {
            String email = Objects.requireNonNull(editTextEmail.getText()).toString();
            String password = Objects.requireNonNull(editTextPassword.getText()).toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Email field is empty", Toast.LENGTH_SHORT).show();
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Password field is empty", Toast.LENGTH_SHORT).show();
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkIfBusDriver(user.getUid());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void checkIfBusDriver(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getBoolean("isBusDriver") != null) {
                        boolean isBusDriver = Boolean.TRUE.equals(documentSnapshot.getBoolean("isBusDriver"));
                        Intent intent;
                        if (isBusDriver) {
                            intent = new Intent(getApplicationContext(), BusDriverActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show());
    }
}
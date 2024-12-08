package com.example.navischool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser user;

    TextView textView;
    EditText childNameInput, addressInput;
    Button addChildButton, removeChildButton, saveAddressButton, logoutButton;

    @SuppressLint("SetTextI18n")
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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        textView = findViewById(R.id.user_details);
        childNameInput = findViewById(R.id.child_name_input);
        addressInput = findViewById(R.id.address_input);
        addChildButton = findViewById(R.id.add_child_button);
        removeChildButton = findViewById(R.id.remove_child_button);
        saveAddressButton = findViewById(R.id.save_address_button);
        logoutButton = findViewById(R.id.logout);

        if (user == null) {
            // Redirect to LoginActivity if user is not logged in
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else {
            textView.setText("Welcome");
            checkIfAdmin(); // Check if user is an admin
        }

        // Add Child Button Logic
        addChildButton.setOnClickListener(view -> {
            String childName = childNameInput.getText().toString();
            if (TextUtils.isEmpty(childName)) {
                Toast.makeText(this, "Child name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        List<String> children = (List<String>) documentSnapshot.get("children");
                        if (children == null) {
                            children = new ArrayList<>();
                        }
                        children.add(childName);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("children", children);

                        db.collection("users").document(user.getUid())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Child added successfully!", Toast.LENGTH_SHORT).show();
                                    childNameInput.setText(""); // Clear input
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add child.", Toast.LENGTH_SHORT).show());
                    });
        });

        // Remove Child Button Logic
        removeChildButton.setOnClickListener(view -> {
            String childName = childNameInput.getText().toString();
            if (TextUtils.isEmpty(childName)) {
                Toast.makeText(this, "Child name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        List<String> children = (List<String>) documentSnapshot.get("children");
                        if (children != null && children.contains(childName)) {
                            children.remove(childName);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("children", children);

                            db.collection("users").document(user.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Child removed successfully!", Toast.LENGTH_SHORT).show();
                                        childNameInput.setText(""); // Clear input
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove child.", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Child not found.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Save Address Button Logic
        saveAddressButton.setOnClickListener(view -> {
            String address = addressInput.getText().toString();
            if (TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("address", address);

            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address saved successfully!", Toast.LENGTH_SHORT).show();
                        addressInput.setText(""); // Clear input
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save address.", Toast.LENGTH_SHORT).show());
        });

        // Logout Button Logic
        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        });
    }

    private void checkIfAdmin() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean isBusDriver = documentSnapshot.getBoolean("isBusDriver");
                    if (isBusDriver != null && isBusDriver) {
                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to check admin status.", Toast.LENGTH_SHORT).show());
    }
}

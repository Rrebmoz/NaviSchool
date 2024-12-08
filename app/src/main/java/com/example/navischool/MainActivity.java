package com.example.navischool;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
    EditText childNameInput, addressInput, phoneNumberInput;
    Button addChildButton, removeChildButton, saveAddressButton, savePhoneButton, logoutButton;
    ListView childrenListView;
    ChildListAdapter childListAdapter;
    List<String> childrenList;

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

        childNameInput = findViewById(R.id.child_name_input);
        addressInput = findViewById(R.id.address_input);
        phoneNumberInput = findViewById(R.id.phone_number_input); // Added phone number input
        addChildButton = findViewById(R.id.add_child_button);
        removeChildButton = findViewById(R.id.remove_child_button);
        saveAddressButton = findViewById(R.id.save_address_button);
        savePhoneButton = findViewById(R.id.save_phone); // Added save phone button
        logoutButton = findViewById(R.id.logout);

        childrenListView = findViewById(R.id.children_listview);
        childrenList = new ArrayList<>();
        childListAdapter = new ChildListAdapter(this, childrenList);
        childrenListView.setAdapter(childListAdapter);

        if (user == null) {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else {
            loadChildren();
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
                                    childNameInput.setText("");
                                    loadChildren(); // Reload the children list
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
                                        loadChildren(); // Reload the children list
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

        // Save Phone Button Logic
        savePhoneButton.setOnClickListener(view -> {
            String phoneNumber = phoneNumberInput.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phoneNumber.matches("\\d{10}")) {  // Example: simple validation for 10 digit number
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("phone_number", phoneNumber);

            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Phone number saved successfully!", Toast.LENGTH_SHORT).show();
                        phoneNumberInput.setText(""); // Clear input
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save phone number.", Toast.LENGTH_SHORT).show());
        });

        // Logout Button Logic
        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        });
    }

    private void loadChildren() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> children = (List<String>) documentSnapshot.get("children");
                    if (children == null) {
                        children = new ArrayList<>();
                    }

                    childrenList.clear();
                    childrenList.addAll(children);
                    childListAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load children.", Toast.LENGTH_SHORT).show());
    }
}


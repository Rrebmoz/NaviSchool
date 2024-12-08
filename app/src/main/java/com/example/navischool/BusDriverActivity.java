package com.example.navischool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusDriverActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;

    Button startButton, logoutButton;
    TextView textView;
    ListView childrenListView;

    List<String> groupedChildren;
    BusDriverChildAdapter childAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bus_driver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        startButton = findViewById(R.id.start_button); // Assuming you have a start button
        logoutButton = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        childrenListView = findViewById(R.id.children_listview);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText("Bus Driver Panel\n" + user.getEmail());
        }

        startButton.setOnClickListener(view -> loadChildren());

        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadChildren() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    groupedChildren = new ArrayList<>();
                    String currentParentAddress = null;
                    Map<String, String> childToPhoneMap = new HashMap<>();

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        List<String> children = (List<String>) documentSnapshot.get("children");
                        String parentPhone = documentSnapshot.getString("phone_number");
                        String parentAddress = documentSnapshot.getString("address");

                        if (children != null && !children.isEmpty()) {
                            if (currentParentAddress == null || !currentParentAddress.equals(parentAddress)) {
                                groupedChildren.add("Address: " + parentAddress);
                                currentParentAddress = parentAddress;
                            }

                            for (String child : children) {
                                groupedChildren.add(child);
                                childToPhoneMap.put(child, parentPhone); // Link child to parent's phone
                            }
                        }
                    }

                    // Set the adapter with the child-to-phone map
                    childAdapter = new BusDriverChildAdapter(this, groupedChildren, childToPhoneMap);
                    childrenListView.setAdapter(childAdapter);

                    childrenListView.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load children.", Toast.LENGTH_SHORT).show());
    }


    public void removeChildFromSession(String child) {
        groupedChildren.remove(child);
        childAdapter.notifyDataSetChanged();
    }
}
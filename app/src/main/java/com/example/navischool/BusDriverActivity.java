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
import java.util.List;

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
        setContentView(R.layout.activity_admin);
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

    // This method will load the grouped children when the START button is pressed
    private void loadChildren() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    groupedChildren = new ArrayList<>();
                    String currentParentEmail = null;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        List<String> children = (List<String>) documentSnapshot.get("children");
                        if (children != null && !children.isEmpty()) {
                            String parentAddress = documentSnapshot.getString("address");
                            if (currentParentEmail == null || !currentParentEmail.equals(parentAddress)) {
                                groupedChildren.add("Address: " + parentAddress);
                                currentParentEmail = parentAddress;
                            }
                            // Add the children under the parent email
                            groupedChildren.addAll(children);
                        }
                    }

                    // Set the adapter to display the children
                    childAdapter = new BusDriverChildAdapter(this, groupedChildren);
                    childrenListView.setAdapter(childAdapter);

                    // Show the children list and hide the start button
                    childrenListView.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load children.", Toast.LENGTH_SHORT).show();
                });
    }

    public void removeChildFromSession(String email) {
        groupedChildren.remove(email);
        childAdapter.notifyDataSetChanged();
    }
}
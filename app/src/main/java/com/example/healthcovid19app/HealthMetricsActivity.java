package com.example.healthcovid19app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class HealthMetricsActivity extends AppCompatActivity {

    private EditText peakFlow1EditText, peakFlow2EditText, peakFlow3EditText;
    private EditText spo2EditText, heartRateEditText, breathingRateEditText;
    private Button submitButton;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_metrics);

        // Initialize Firebase Auth and Database Reference
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize EditTexts and Button
        peakFlow1EditText = findViewById(R.id.peakFlow1EditText);
        peakFlow2EditText = findViewById(R.id.peakFlow2EditText);
        peakFlow3EditText = findViewById(R.id.peakFlow3EditText);
        spo2EditText = findViewById(R.id.spo2EditText);
        heartRateEditText = findViewById(R.id.heartRateEditText);
        breathingRateEditText = findViewById(R.id.breathingRateEditText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    Toast.makeText(HealthMetricsActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                String peakFlow1 = peakFlow1EditText.getText().toString();
                String peakFlow2 = peakFlow2EditText.getText().toString();
                String peakFlow3 = peakFlow3EditText.getText().toString();
                String spo2 = spo2EditText.getText().toString();
                String heartRate = heartRateEditText.getText().toString();
                String breathingRate = breathingRateEditText.getText().toString();

                if (validateInputs(peakFlow1, peakFlow2, peakFlow3, spo2, heartRate, breathingRate)) {
                    saveHealthMetrics(currentUser.getUid(), peakFlow1, peakFlow2, peakFlow3, spo2, heartRate, breathingRate);
                } else {
                    Toast.makeText(HealthMetricsActivity.this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void saveHealthMetrics(String userId, String peakFlow1, String peakFlow2, String peakFlow3, String spo2, String heartRate, String breathingRate) {
        Map<String, Object> healthMetrics = new HashMap<>();
        healthMetrics.put("peakFlow1", peakFlow1);
        healthMetrics.put("peakFlow2", peakFlow2);
        healthMetrics.put("peakFlow3", peakFlow3);
        healthMetrics.put("spo2", spo2);
        healthMetrics.put("heartRate", heartRate);
        healthMetrics.put("breathingRate", breathingRate);

        mDatabase.child("users").child(userId).child("healthMetrics").push().setValue(healthMetrics)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(HealthMetricsActivity.this, "Health metrics saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HealthMetricsActivity.this, "Failed to save health metrics", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

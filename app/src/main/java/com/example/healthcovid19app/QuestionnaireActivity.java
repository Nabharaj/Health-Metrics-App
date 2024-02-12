package com.example.healthcovid19app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionnaireActivity extends AppCompatActivity {

    private Spinner skinColorSpinner;
    private EditText heightEditText, weightEditText, chestMeasurementEditText;
    private Button submitButton, nextButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not authenticated, redirect to login screen or show message
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_LONG).show();
             Intent intent = new Intent(this, LoginActivity.class); // Replace with your login activity
             startActivity(intent);
             finish(); // Close current activity
            return;
        }

        initializeUI();

        submitButton.setOnClickListener(v -> {
            if (!isUserAuthenticated()) {
                Toast.makeText(QuestionnaireActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            String skinColor = skinColorSpinner.getSelectedItem().toString();
            String height = heightEditText.getText().toString();
            String weight = weightEditText.getText().toString();
            String chestMeasurement = chestMeasurementEditText.getText().toString();

            saveUserResponses(currentUser.getUid(), skinColor, height, weight, chestMeasurement);
        });

        nextButton = findViewById(R.id.next_button); // Replace with your actual button ID
        nextButton.setOnClickListener(view -> navigateToRecordActivity());
    }

    private boolean isUserAuthenticated() {
        return mAuth.getCurrentUser() != null;
    }

    private void initializeUI() {
        skinColorSpinner = findViewById(R.id.spinnerSkinColor);
        heightEditText = findViewById(R.id.editTextHeight);
        weightEditText = findViewById(R.id.editTextWeight);
        chestMeasurementEditText = findViewById(R.id.editTextChestMeasurement);
        submitButton = findViewById(R.id.submit_button);
        nextButton = findViewById(R.id.next_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.skin_color_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinColorSpinner.setAdapter(adapter);
    }

    private void saveUserResponses(String userId, String skinColor, String height, String weight, String chestMeasurement) {
        Map<String, Object> questionnaireData = new HashMap<>();
        questionnaireData.put("skinColor", skinColor);
        questionnaireData.put("height", height);
        questionnaireData.put("weight", weight);
        questionnaireData.put("chestMeasurement", chestMeasurement);

        mDatabase.child("users").child(userId).child("questionnaires").push().setValue(questionnaireData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(QuestionnaireActivity.this, "Questionnaire responses saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(QuestionnaireActivity.this, "Failed to save responses: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToRecordActivity() {
        Intent intent = new Intent(QuestionnaireActivity.this, RecordActivity.class); // Replace with the actual activity you want to navigate to
        startActivity(intent);
    }
}

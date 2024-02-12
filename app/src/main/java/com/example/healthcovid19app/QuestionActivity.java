package com.example.healthcovid19app;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.example.healthcovid19app.databinding.ActivityQuestionBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class QuestionActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    public static final int PERMISSION_REQUEST_CODE = 1245;
    String str;

    private static final String TAG = "QuestionActivity";


    ActivityQuestionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login activity
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class); // Replace with your login activity
            startActivity(intent);
            finish(); // Close current activity
            return;
        }

        binding.etEthinicityOther.setVisibility(View.GONE);
        binding.rbEthnicityOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                binding.etEthinicityOther.setVisibility(b ? View.VISIBLE : View.GONE);
            }

        });

        setAgeSpinner();
        setCountrySpinner();

        binding.nextaudio.setOnClickListener(view -> onNextClick());

        Intent intent = getIntent();
        str = intent.getStringExtra("message_key");
        binding.ref.setText(str);


    }

    private void onNextClick() {

        if (!checkPermission()) {
            requestPermission();
            Toast.makeText(this, "Allow permission to record", Toast.LENGTH_SHORT).show();
            return;
        }

        //Demographics

        String name = String.valueOf(binding.etName.getText());
        String age = String.valueOf(binding.spinnerAge.getSelectedItem());
        if (binding.spinnerAge.getSelectedItemPosition() == 0) {
            age = null;
        }
        String sex = getSex();
        String country = String.valueOf(binding.spinnerCountry.getSelectedItem());
        if (binding.spinnerCountry.getSelectedItemPosition() == 0) {
            country = null;
        }
        String ethnicity = getethnicity();
        String smoke = getSmoke();

        //Symptons

        String symptons = "";
        String otherSymptons = String.valueOf(binding.etSymptonsOther.getText());
        if (binding.cbSymptons1.isChecked()) {
            symptons += binding.cbSymptons1.getText() + ", ";
        }
        if (binding.cbSymptons2.isChecked()) {
            symptons += binding.cbSymptons2.getText() + ", ";
        }
        if (binding.cbSymptons3.isChecked()) {
            symptons += binding.cbSymptons3.getText() + ", ";
        }
        if (binding.cbSymptons4.isChecked()) {
            symptons += binding.cbSymptons4.getText() + ", ";
        }
        if (binding.cbSymptons5.isChecked()) {
            symptons += binding.cbSymptons5.getText() + ", ";
        }
        if (binding.cbSymptons6.isChecked()) {
            symptons += binding.cbSymptons6.getText() + ", ";
        }
        if (!otherSymptons.isEmpty()) {
            symptons += otherSymptons + ", ";
        }

        if (symptons.length() >= 4)
            symptons = symptons.substring(0, symptons.length() - 2);

        //disease

        String disease = "";
        String otherDisease = String.valueOf(binding.etDiseaseOther.getText());
        if (binding.cbDisease1.isChecked()) {
            disease += binding.cbDisease1.getText() + ", ";
        }
        if (binding.cbDisease2.isChecked()) {
            disease += binding.cbDisease2.getText() + ", ";
        }
        if (binding.cbDisease3.isChecked()) {
            disease += binding.cbDisease3.getText() + ", ";
        }
        if (binding.cbDisease4.isChecked()) {
            disease += binding.cbDisease4.getText() + ", ";
        }
        if (!otherDisease.isEmpty()) {
            disease += otherDisease + ", ";
        }

        if (disease.length() >= 4)
            disease = disease.substring(0, disease.length() - 2);

        if (name.isEmpty()) {
            binding.etName.setError("Enter Name");
            binding.etName.requestFocus();
            return;
        }

        binding.etName.setError(null);

        if (age == null) {
            Toast.makeText(this, "Select Age", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sex == null) {
            Toast.makeText(this, "Select Sex", Toast.LENGTH_SHORT).show();
            return;
        }

        if (country == null) {
            Toast.makeText(this, "Select Country", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ethnicity == null) {
            Toast.makeText(this, "Select Ethnicity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (smoke == null) {
            Toast.makeText(this, "Select Smoke", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!binding.checkBox.isChecked()) {
            Toast.makeText(this, "Check The Disclaimer", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "name: " + name);
        Log.d(TAG, "age: " + age);
        Log.d(TAG, "sex: " + sex);
        Log.d(TAG, "country: " + country);
        Log.d(TAG, "ethnicity: " + ethnicity);
        Log.d(TAG, "smoke: " + smoke);
        Log.d(TAG, "symtons: " + symptons);
        Log.d(TAG, "disease: " + disease);


        Intent intent = new Intent(QuestionActivity.this, QuestionnaireActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("age", age);
        intent.putExtra("sex", sex);
        intent.putExtra("country", country);
        intent.putExtra("ethnicity", ethnicity);
        intent.putExtra("smoke", smoke);
        intent.putExtra("symptons", symptons);
        intent.putExtra("disease", disease);

        intent.putExtra("id", str);
        startActivity(intent);
        saveDataToFirebase(name, age, sex, country, ethnicity, smoke, symptons, disease);

    }

    private String getSmoke() {
        if (binding.smokeYes.isChecked()) {
            return String.valueOf(binding.smokeYes.getText());
        }
        if (binding.smokeNo.isChecked()) {
            return String.valueOf(binding.smokeNo.getText());
        }
        return null;
    }

    private String getSex() {
        if (binding.rbSexMale.isChecked()) {
            return String.valueOf(binding.rbSexMale.getText());
        }
        if (binding.rbSexFemale.isChecked()) {
            return String.valueOf(binding.rbSexFemale.getText());
        }
        if (binding.rbSexOther.isChecked()) {
            return String.valueOf(binding.rbSexOther.getText());
        }
        return null;
    }

    private String getethnicity() {
        if (binding.rbAsian.isChecked()) {
            return String.valueOf(binding.rbAsian.getText());
        }
        if (binding.rbWhite.isChecked()) {
            return String.valueOf(binding.rbWhite.getText());
        }
        if (binding.rbBlack.isChecked()) {
            return String.valueOf(binding.rbBlack.getText());
        }
        if (binding.rbEthnicityOther.isChecked()) {

            String otherEthnicity = String.valueOf(binding.etEthinicityOther.getText());

            if (otherEthnicity.isEmpty()) {
                binding.etEthinicityOther.setError("Enter Other Ethnicity");
                binding.etEthinicityOther.requestFocus();
                return null;
            }
            return otherEthnicity;
        }
        return null;

    }


    private void setAgeSpinner() {

        ArrayList<String> sizes = new ArrayList<>();
        sizes.add("Select Age");
        sizes.add("Under 18");
        sizes.add("18-25");
        sizes.add("26-35");
        sizes.add("36-50");
        sizes.add("50+");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizes);
        binding.spinnerAge.setAdapter(adapter);

    }

    private void setCountrySpinner() {
        String[] country_list = {"Select Country", "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Anguilla", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia &amp; Herzegovina", "Botswana", "Brazil", "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Cape Verde", "Cayman Islands", "Chad", "Chile", "China", "Colombia", "Congo", "Cook Islands", "Costa Rica", "Cote D Ivoire", "Croatia", "Cruise Ship", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Estonia", "Ethiopia", "Falkland Islands", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia", "French West Indies", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea Bissau", "Guyana", "Haiti", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kuwait", "Kyrgyz Republic", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Mauritania", "Mauritius", "Mexico", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Namibia", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Norway", "Oman", "Pakistan", "Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russia", "Rwanda", "Saint Pierre &amp; Miquelon", "Samoa", "San Marino", "Satellite", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "South Africa", "South Korea", "Spain", "Sri Lanka", "St Kitts &amp; Nevis", "St Lucia", "St Vincent", "St. Lucia", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor L'Este", "Togo", "Tonga", "Trinidad &amp; Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks &amp; Caicos", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "Uruguay", "Uzbekistan", "Venezuela", "Vietnam", "Virgin Islands (US)", "Yemen", "Zambia", "Zimbabwe"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, country_list);
        binding.spinnerCountry.setAdapter(adapter);
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(this, RECORD_AUDIO);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
        }
    }


    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
            ActivityCompat.requestPermissions(QuestionActivity.this, new String[]{RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            //below android 11
            ActivityCompat.requestPermissions(QuestionActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_AUDIO = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE && WRITE_AUDIO) {
                    // perform action when allow permission success
//                    startService();
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void saveDataToFirebase(String name, String age, String sex, String country, String ethnicity, String smoke, String symptoms, String disease) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Sanitize name and age to ensure they are valid Firebase keys
            String sanitizedAge = age.replaceAll("[^a-zA-Z0-9]", "");
            String sanitizedUsername = name.replaceAll("[^a-zA-Z0-9]", "");
            // Combine sanitized name and age for a unique session ID
            String sessionId = sanitizedUsername + "_" + sanitizedAge;

            Map<String, Object> questionData = new HashMap<>();
            questionData.put("name", name);
            questionData.put("age", age);
            questionData.put("sex", sex);
            questionData.put("country", country);
            questionData.put("ethnicity", ethnicity);
            questionData.put("smoke", smoke);
            questionData.put("symptoms", symptoms);
            questionData.put("disease", disease);

            // Store data under this session ID
            mDatabase.child("users").child(userId).child("questions").child(sessionId).setValue(questionData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Questions data saved successfully");
                        Toast.makeText(QuestionActivity.this, "Session data saved successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuestionActivity.this, QuestionnaireActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save Question data", e);
                        Toast.makeText(QuestionActivity.this, "Failed to save question data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(QuestionActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }






}
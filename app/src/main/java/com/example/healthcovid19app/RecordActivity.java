package com.example.healthcovid19app;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.example.healthcovid19app.databinding.ActivityRecordBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivity";
    ActivityRecordBinding binding;
    String high, low;
    String high_url, low_url;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    SensorManager sensorManager1;
    SensorManager sensorManager2;
    Sensor gyroscope;
    Sensor accelerometer;
    String gyroscope_x_high = "";
    String gyroscope_x_low = "";
    String gyroscope_y_high = "";
    String gyroscope_y_low = "";
    String gyroscope_z_high = "";
    String gyroscope_z_low = "";
    String accelerometer_x_high = "";
    String accelerometer_x_low = "";
    String accelerometer_y_high = "";
    String accelerometer_y_low = "";
    String accelerometer_z_high = "";
    String accelerometer_z_low = "";
    boolean mIsSensorUpdateEnabled;


    private void startSensor(boolean isHigh) {

        sensorManager1 = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager2 = (SensorManager) getSystemService(SENSOR_SERVICE);


        gyroscope = sensorManager1.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorManager2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager1.registerListener(sensor1(isHigh), gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager2.registerListener(sensor2(isHigh), accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        mIsSensorUpdateEnabled = true;
    }

    private void stopSensors(boolean isHigh) {
        sensorManager1.unregisterListener(sensor1(isHigh), gyroscope);
        sensorManager2.unregisterListener(sensor1(isHigh), accelerometer);
//        sensorManager1 = null;
//        sensorManager2 = null;
        mIsSensorUpdateEnabled = false;

        if (isHigh) {
            isHighRunning = false;
        } else {
            isLowRunning = false;
        }

        setButtonVisibility();

    }

    private SensorEventListener sensor1(boolean isHigh) {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                if (!mIsSensorUpdateEnabled) {
                    stopSensors(isHigh);
                    Log.e("SensorMM", "SensorUpdate disabled. returning");
                    return;
                }

                if (isHigh) {
                    gyroscope_x_high += !String.valueOf(sensorEvent.values[0]).equals("null") ? sensorEvent.values[0] + ", " : "";
                    gyroscope_y_high += !String.valueOf(sensorEvent.values[1]).equals("null") ? sensorEvent.values[1] + ", " : "";
                    gyroscope_z_high += !String.valueOf(sensorEvent.values[2]).equals("null") ? sensorEvent.values[2] + ", " : "";
                    return;
                }

                gyroscope_x_low += !String.valueOf(sensorEvent.values[0]).equals("null") ? sensorEvent.values[0] + ", " : "";
                gyroscope_y_low += !String.valueOf(sensorEvent.values[1]).equals("null") ? sensorEvent.values[1] + ", " : "";
                gyroscope_z_low += !String.valueOf(sensorEvent.values[2]).equals("null") ? sensorEvent.values[2] + ", " : "";


            }

            private void stopSensors(boolean isHigh) {
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private SensorEventListener sensor2(boolean isHigh) {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                if (!mIsSensorUpdateEnabled) {
                    Log.e("SensorMM", "SensorUpdate disabled. returning");
                    return;
                }

                // Log the sensor data here, where sensorEvent is accessible
                Log.d(TAG, "Gyroscope X High: " + sensorEvent.values[0]);

                if (isHigh) {
                    accelerometer_x_high += !String.valueOf(sensorEvent.values[0]).equals("null") ? sensorEvent.values[0] + ", " : "";
                    accelerometer_y_high += !String.valueOf(sensorEvent.values[1]).equals("null") ? sensorEvent.values[1] + ", " : "";
                    accelerometer_z_high += !String.valueOf(sensorEvent.values[2]).equals("null") ? sensorEvent.values[2] + ", " : "";
                    return;
                }

                accelerometer_x_low += !String.valueOf(sensorEvent.values[0]).equals("null") ? sensorEvent.values[0] + ", " : "";
                accelerometer_y_low += !String.valueOf(sensorEvent.values[1]).equals("null") ? sensorEvent.values[1] + ", " : "";
                accelerometer_z_low += !String.valueOf(sensorEvent.values[2]).equals("null") ? sensorEvent.values[2] + ", " : "";
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                // Handle accuracy change if needed
            }
        };
    }



    boolean isHighRunning = false;
    boolean isLowRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setButtonVisibility();

        binding.startHigh.setOnClickListener(view -> {
            if (isHighRunning) {
                stopRecording();
                return;
            }
            startRecording(true);
        });
        binding.startLow.setOnClickListener(view -> {
            if (isLowRunning) {
                stopRecording();
                return;
            }
            startRecording(false);
        });
        binding.upload.setOnClickListener(view -> uploadData());
    }

    private void setButtonVisibility() {
        if (isHighRunning) {
            binding.startHigh.setText("STOP");
        } else {
            binding.startHigh.setText("START");
        }
        binding.startHigh.setEnabled(!isLowRunning);
        binding.startLow.setEnabled(!isHighRunning);
        if (isLowRunning) {
            binding.startLow.setText("STOP");
        } else {
            binding.startLow.setText("START");
        }
    }

    private void uploadData() {
        if (high == null) {
            Toast.makeText(this, "Record High Cough", Toast.LENGTH_SHORT).show();
            return;
        }
        if (low == null) {
            Toast.makeText(this, "Record Low Cough", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Upload...!", Toast.LENGTH_SHORT).show();
        // First Upload High Cough,, then Low Cough
        uploadAudio(true);
    }

    private void uploadAudio(boolean isHigh) {
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("audio/mpeg").build();
        mStorageRef.child("audio/" + UUID.randomUUID()).putFile(Uri.fromFile(new File(isHigh ? high : low)), metadata).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "onViewClicked: " + "upload");
            Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl().addOnSuccessListener(uri -> {
                        if (isHigh) {
                            high_url = (uri.toString());
                            Log.d(TAG, "uploadAudio: " + "High \n" + high_url);
                            uploadAudio(false);
                        } else {
                            low_url = (uri.toString());
                            Log.d(TAG, "uploadAudio: " + "Low \n" + low_url);
                            setData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof IOException)
                            Toast.makeText(RecordActivity.this, "internet connection error", Toast.LENGTH_SHORT).show();
                        else Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
                    });
        }).addOnFailureListener(e -> {
            if (e instanceof IOException)
                Toast.makeText(RecordActivity.this, "internet connection error", Toast.LENGTH_SHORT).show();
            else Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
        });
    }

    private void setData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();  // Get the user's unique ID
        String sessionId = UUID.randomUUID().toString(); // Generate a unique session ID

        // Prepare your data map
        HashMap<String, String> map = new HashMap<>();
        map.put("gyroscope_x_high", gyroscope_x_high);
        map.put("gyroscope_x_low", gyroscope_x_low);
        map.put("gyroscope_y_high", gyroscope_y_high);
        map.put("gyroscope_y_low", gyroscope_y_low);
        map.put("gyroscope_z_high", gyroscope_z_high);
        map.put("gyroscope_z_low", gyroscope_z_low);
        map.put("accelerometer_x_high", accelerometer_x_high);
        map.put("accelerometer_x_low", accelerometer_x_low);
        map.put("accelerometer_y_high", accelerometer_y_high);
        map.put("accelerometer_y_low", accelerometer_y_low);
        map.put("accelerometer_z_high", accelerometer_z_high);
        map.put("accelerometer_z_low", accelerometer_z_low);

        // Save the data under the user's ID in a session-specific sub-directory
        mDatabase.child("users").child(userId).child("sessions").child(sessionId).setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RecordActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                    navigateToHealthMetricsActivity();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof IOException)
                        Toast.makeText(RecordActivity.this, "Internet connection error", Toast.LENGTH_SHORT).show();
                    else
                        Log.d(TAG, "Failure: " + e.getLocalizedMessage());
                });
    }



    private void navigateToHealthMetricsActivity() {
        Intent intent = new Intent(RecordActivity.this, HealthMetricsActivity.class);
        startActivity(intent);

        // Finish the current activity
        finish();
    }


    MediaRecorder recorder;

    public void startRecording(boolean isHigh) {
        if (isHigh) {
            gyroscope_x_high = "";
            gyroscope_y_high = "";
            gyroscope_z_high = "";
        } else {
            gyroscope_x_low = "";
            gyroscope_y_low = "";
            gyroscope_z_low = "";
        }

        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        if (isHigh) {
            high = getOutputMediaFile().getAbsolutePath();
            recorder.setOutputFile(high);
        } else {
            low = getOutputMediaFile().getAbsolutePath();
            recorder.setOutputFile(low);
        }

        try {
            recorder.prepare();
            recorder.start();
            startSensor(isHigh);
            if (isHigh) {
                isHighRunning = true;
            } else {
                isLowRunning = true;
            }
            setButtonVisibility();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void stopRecording() {
        recorder.stop();
        recorder.release();
        stopSensors(isHighRunning);
        if (isHighRunning) {
            isHighRunning = false;
        } else {
            isLowRunning = false;
        }
        setButtonVisibility();
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "BioSense");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator +
                "audio_" + System.currentTimeMillis() + ".3gp");
    }
}

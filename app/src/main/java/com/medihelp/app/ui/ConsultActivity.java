package com.medihelp.app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import com.medihelp.app.MediHelpApp;
import com.medihelp.app.R;
import com.medihelp.app.data.MedicalDatabase;
import com.medihelp.app.data.UserPreferences;
import com.medihelp.app.model.Consultation;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsultActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_CAMERA = 102;
    private static final int REQUEST_GALLERY = 103;

    private EditText symptomsInput;
    private TextView adviceOutput;
    private View adviceCard;
    private ProgressBar progressBar;
    private TextView ageInfoText;
    private ImageView photoPreview;
    private MaterialButton removePhotoButton;
    private FrameLayout photoStatusBadge;
    private View photoPreviewContainer;
    private View uploadButtonsContainer;

    private ExecutorService executor;
    private UserPreferences preferences;
    private String currentPhotoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Consultation");
        }

        symptomsInput = findViewById(R.id.symptomsInput);
        adviceOutput = findViewById(R.id.adviceOutput);
        adviceCard = findViewById(R.id.adviceCard);
        progressBar = findViewById(R.id.progressBar);
        ageInfoText = findViewById(R.id.ageInfoText);
        photoPreview = findViewById(R.id.photoPreview);
        removePhotoButton = findViewById(R.id.removePhotoButton);
        photoStatusBadge = findViewById(R.id.photoStatusBadge);
        photoPreviewContainer = findViewById(R.id.photoPreviewContainer);
        uploadButtonsContainer = findViewById(R.id.uploadButtonsContainer);
        MaterialButton cameraButton = findViewById(R.id.cameraButton);
        MaterialButton galleryButton = findViewById(R.id.galleryButton);

        cameraButton.setOnClickListener(v -> checkCameraPermission());
        galleryButton.setOnClickListener(v -> checkStoragePermission());
        removePhotoButton.setOnClickListener(v -> removePhoto());

        executor = Executors.newSingleThreadExecutor();
        preferences = MediHelpApp.getInstance().getPreferences();

        updateAgeInfo();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "MEDIHELP_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_GALLERY);
    }

    private void removePhoto() {
        currentPhotoPath = null;
        photoPreviewContainer.setVisibility(View.GONE);
        photoStatusBadge.setVisibility(View.GONE);
        removePhotoButton.setVisibility(View.GONE);
        uploadButtonsContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && currentPhotoPath != null) {
                displayPhoto(currentPhotoPath);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        String savedPath = saveImageFromUri(selectedImage);
                        currentPhotoPath = savedPath;
                        displayPhoto(savedPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String saveImageFromUri(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "MEDIHELP_" + timestamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);
        
        FileOutputStream out = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        out.flush();
        out.close();
        
        return imageFile.getAbsolutePath();
    }

    private void displayPhoto(String path) {
        if (path != null && !path.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                photoPreview.setImageBitmap(bitmap);
                photoPreviewContainer.setVisibility(View.VISIBLE);
                photoStatusBadge.setVisibility(View.VISIBLE);
                removePhotoButton.setVisibility(View.VISIBLE);
                uploadButtonsContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateAgeInfo() {
        int age = preferences.getAge();
        String language = preferences.getLanguage();
        String ageGroup;
        String ageText;

        if (age < 18) {
            if (language.equals("de")) {
                ageGroup = "Kind/Teenager";
                ageText = "Ihr Alter: " + age + " Jahre (" + ageGroup + ")";
            } else if (language.equals("nl")) {
                ageGroup = "Kind/Teenager";
                ageText = "Uw leeftijd: " + age + " jaar (" + ageGroup + ")";
            } else if (language.equals("fr")) {
                ageGroup = "Enfant/Ado";
                ageText = "Votre âge: " + age + " ans (" + ageGroup + ")";
            } else if (language.equals("es")) {
                ageGroup = "Niño/Adolescente";
                ageText = "Su edad: " + age + " años (" + ageGroup + ")";
            } else if (language.equals("si")) {
                ageGroup = "ළමා/දියමන්ති";
                ageText = "ඔබේ වයස: " + age + " වසර (" + ageGroup + ")";
            } else {
                ageGroup = "Child/Teenager";
                ageText = "Your age: " + age + " years (" + ageGroup + ")";
            }
        } else {
            if (language.equals("de")) {
                ageText = "Ihr Alter: " + age + " Jahre (Erwachsener)";
            } else if (language.equals("nl")) {
                ageText = "Uw leeftijd: " + age + " jaar (Volwassene)";
            } else if (language.equals("fr")) {
                ageText = "Votre âge: " + age + " ans (Adulte)";
            } else if (language.equals("es")) {
                ageText = "Su edad: " + age + " años (Adulto)";
            } else if (language.equals("si")) {
                ageText = "ඔබේ වයස: " + age + " වසර (වැඩිහිටි)";
            } else {
                ageText = "Your age: " + age + " years (Adult)";
            }
        }
        ageInfoText.setText(ageText);
    }

    public void onGetAdvice(View view) {
        String symptoms = symptomsInput.getText().toString().trim();

        if (TextUtils.isEmpty(symptoms)) {
            symptomsInput.setError("Please describe your symptoms");
            return;
        }

        adviceCard.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        final int userAge = preferences.getAge();
        final String language = preferences.getLanguage();

        executor.execute(() -> {
            String advice = MedicalDatabase.generateAdvice(symptoms, userAge, language);

            Consultation consultation = new Consultation(symptoms, advice, userAge, currentPhotoPath);
            MediHelpApp.getInstance().getDatabase().consultationDao().insert(consultation);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                adviceOutput.setText(advice);
                adviceCard.setVisibility(View.VISIBLE);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    public void onViewHistory(View view) {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
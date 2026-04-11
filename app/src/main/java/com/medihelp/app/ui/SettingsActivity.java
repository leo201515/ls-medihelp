package com.medihelp.app.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.medihelp.app.MediHelpApp;
import com.medihelp.app.R;
import com.medihelp.app.data.UserPreferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {
    private UserPreferences preferences;
    private TextInputLayout ageLayout;
    private TextInputEditText ageInput;
    private MaterialButton saveButton;
    private RadioGroup languageGroup;
    private RadioButton radioGerman;
    private RadioButton radioEnglish;
    private RadioButton radioDutch;
    private RadioButton radioFrench;
    private RadioButton radioSpanish;
    private RadioButton radioSinhala;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        preferences = MediHelpApp.getInstance().getPreferences();

        ageLayout = findViewById(R.id.ageLayout);
        ageInput = findViewById(R.id.ageInput);
        saveButton = findViewById(R.id.saveButton);
        languageGroup = findViewById(R.id.languageGroup);
        radioGerman = findViewById(R.id.radioGerman);
        radioEnglish = findViewById(R.id.radioEnglish);
        radioDutch = findViewById(R.id.radioDutch);
        radioFrench = findViewById(R.id.radioFrench);
        radioSpanish = findViewById(R.id.radioSpanish);
        radioSinhala = findViewById(R.id.radioSinhala);

        ageInput.setText(String.valueOf(preferences.getAge()));

        String currentLang = preferences.getLanguage();
        switch (currentLang) {
            case "de":
                radioGerman.setChecked(true);
                break;
            case "en":
                radioEnglish.setChecked(true);
                break;
            case "nl":
                radioDutch.setChecked(true);
                break;
            case "fr":
                radioFrench.setChecked(true);
                break;
            case "es":
                radioSpanish.setChecked(true);
                break;
            case "si":
                radioSinhala.setChecked(true);
                break;
            default:
                radioGerman.setChecked(true);
                break;
        }

        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String ageStr = ageInput.getText().toString().trim();

        if (ageStr.isEmpty()) {
            ageLayout.setError("Please enter age");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            ageLayout.setError("Invalid number");
            return;
        }

        if (age < 0 || age > 150) {
            ageLayout.setError("Please enter a valid age (0-150)");
            return;
        }

        preferences.setAge(age);

        int selectedId = languageGroup.getCheckedRadioButtonId();
        String lang;
        if (selectedId == R.id.radioGerman) {
            lang = "de";
        } else if (selectedId == R.id.radioEnglish) {
            lang = "en";
        } else if (selectedId == R.id.radioDutch) {
            lang = "nl";
        } else if (selectedId == R.id.radioFrench) {
            lang = "fr";
        } else if (selectedId == R.id.radioSpanish) {
            lang = "es";
        } else if (selectedId == R.id.radioSinhala) {
            lang = "si";
        } else {
            lang = "de";
        }
        preferences.setLanguage(lang);

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
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

package com.medihelp.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.medihelp.app.MediHelpApp;
import com.medihelp.app.R;
import com.medihelp.app.data.UserPreferences;

public class MainActivity extends AppCompatActivity {
    private TextView ageDisplay;
    private TextView languageDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("MediHelp");

        ageDisplay = findViewById(R.id.ageDisplay);
        languageDisplay = findViewById(R.id.languageDisplay);
        CardView consultCard = findViewById(R.id.consultCard);
        CardView historyCard = findViewById(R.id.historyCard);

        consultCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ConsultActivity.class));
        });

        historyCard.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileDisplay();
    }

    private void updateProfileDisplay() {
        UserPreferences prefs = MediHelpApp.getInstance().getPreferences();
        int age = prefs.getAge();
        String language = prefs.getLanguage();

        String ageText;
        String langText;

        if (language.equals("de")) {
            ageText = "Alter: " + age + " Jahre";
            langText = "Sprache: Deutsch";
        } else if (language.equals("nl")) {
            ageText = "Leeftijd: " + age;
            langText = "Taal: Nederlands";
        } else if (language.equals("fr")) {
            ageText = "Âge: " + age + " ans";
            langText = "Langue: Français";
        } else if (language.equals("es")) {
            ageText = "Edad: " + age + " años";
            langText = "Idioma: Español";
        } else if (language.equals("si")) {
            ageText = "වයස: " + age + " වසර";
            langText = "භාෂාව: සිංහල";
        } else {
            ageText = "Age: " + age + " years";
            langText = "Language: English";
        }

        ageDisplay.setText(ageText);
        languageDisplay.setText(langText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

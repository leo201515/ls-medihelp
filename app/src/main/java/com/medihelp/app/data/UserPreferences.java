package com.medihelp.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class UserPreferences {
    private static final String KEY_AGE = "user_age";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_LANGUAGE = "language";
    private static final int DEFAULT_AGE = 30;
    private static final String DEFAULT_LANGUAGE = "de";

    private final SharedPreferences prefs;

    public UserPreferences(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getAge() {
        return prefs.getInt(KEY_AGE, DEFAULT_AGE);
    }

    public void setAge(int age) {
        prefs.edit().putInt(KEY_AGE, age).apply();
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public void setName(String name) {
        prefs.edit().putString(KEY_NAME, name).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String lang) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }
}

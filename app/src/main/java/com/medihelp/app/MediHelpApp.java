package com.medihelp.app;

import android.app.Application;
import com.medihelp.app.data.AppDatabase;
import com.medihelp.app.data.UserPreferences;

public class MediHelpApp extends Application {
    private static MediHelpApp instance;
    private AppDatabase database;
    private UserPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
        preferences = new UserPreferences(this);
    }

    public static MediHelpApp getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }
}

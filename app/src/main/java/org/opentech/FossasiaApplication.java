package org.opentech;

import android.app.Application;
import android.preference.PreferenceManager;

import org.opentech.alarms.FosdemAlarmManager;
import org.opentech.db.DatabaseManager;

public class FossasiaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseManager.init(this);
        // Initialize settings
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // Alarms (requires settings)
        FosdemAlarmManager.init(this);
    }
}

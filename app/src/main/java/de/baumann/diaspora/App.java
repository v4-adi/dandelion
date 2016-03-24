package de.baumann.diaspora;

import android.app.Application;

/**
 * Created by gregor on 24.03.16.
 */
public class App extends Application {
    private AppSettings appSettings;
    public static final String APP_LOG_TAG = "DIASPORA_";

    @Override
    public void onCreate() {
        super.onCreate();
        appSettings = new AppSettings(getApplicationContext());
    }

    public AppSettings getSettings() {
        return appSettings;
    }
}

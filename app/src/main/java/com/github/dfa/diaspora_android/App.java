package com.github.dfa.diaspora_android;

import android.app.Application;

import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.AvatarImageLoader;

/**
 * Created by gregor on 24.03.16.
 */
public class App extends Application {
    private AppSettings appSettings;
    private AvatarImageLoader avatarImageLoader;
    public static final String APP_LOG_TAG = "DIASPORA_";

    @Override
    public void onCreate() {
        super.onCreate();
        appSettings = new AppSettings(getApplicationContext());
        avatarImageLoader = new AvatarImageLoader(getApplicationContext());
    }

    public AppSettings getSettings() {
        return appSettings;
    }

    public AvatarImageLoader getAvatarImageLoader() {
        return avatarImageLoader;
    }
}

package com.github.dfa.diaspora_android;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.AvatarImageLoader;

/**
 * Created by gregor on 24.03.16.
 */
public class App extends Application {
    public static final String TAG = "DIASPORA_";

    private AppSettings appSettings;
    private AvatarImageLoader avatarImageLoader;
    private CookieManager cookieManager;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context c = getApplicationContext();
        appSettings = new AppSettings(c);
        avatarImageLoader = new AvatarImageLoader(c);


        // Get cookie manager
        cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(c);
        }
        cookieManager.setAcceptCookie(true);
    }

    public AppSettings getSettings() {
        return appSettings;
    }

    public AvatarImageLoader getAvatarImageLoader() {
        return avatarImageLoader;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }
}

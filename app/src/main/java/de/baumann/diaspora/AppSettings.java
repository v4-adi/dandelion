package de.baumann.diaspora;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by de-live-gdev on 20.03.16. Part of Diaspora WebApp.
 */
class AppSettings {
    private final SharedPreferences pref;

    public AppSettings(Context context) {
        Context context1 = context.getApplicationContext();
        pref = context1.getSharedPreferences("app", Context.MODE_PRIVATE);
    }

    private void setString(String key, String value) {
        pref.edit().putString(key, value).apply();
    }
    private void setInt(String key, int value) {
        pref.edit().putInt(key, value).apply();
    }
    private void setBool(String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

    /*
    //   Preferences
     */
    private static final String PREF_PROFILE_ID = "profileID";
    private static final String PREF_IS_LOAD_IMAGES = "loadImages";
    private static final String PREF_MINIMUM_FONT_SIZE = "minimumFontSize";


    /*
    //     Setters & Getters
    */
    public String getProfileId() {
        return pref.getString(PREF_PROFILE_ID, "");
    }

    public void setProfileId(String profileId) {
        setString(PREF_PROFILE_ID, profileId);
    }


    public boolean isLoadImages() {
        return pref.getBoolean(PREF_IS_LOAD_IMAGES, true);
    }

    public void setLoadImages(boolean loadImages) {
        setBool(PREF_IS_LOAD_IMAGES, loadImages);
    }


    public int getMinimumFontSize() {
        return pref.getInt(PREF_MINIMUM_FONT_SIZE, 8);
    }

    public void setMinimumFontSize(int minimumFontSize) {
        setInt(PREF_MINIMUM_FONT_SIZE, minimumFontSize);
    }
}

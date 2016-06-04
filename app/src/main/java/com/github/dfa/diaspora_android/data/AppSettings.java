package com.github.dfa.diaspora_android.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gsantner on 20.03.16. Part of Diaspora for Android.
 */
public class AppSettings {
    private final SharedPreferences prefApp;
    private final SharedPreferences prefPod;
    private final Context context;

    public AppSettings(Context context) {
        this.context = context.getApplicationContext();
        prefApp = this.context.getSharedPreferences("app", Context.MODE_PRIVATE);
        prefPod = this.context.getSharedPreferences("pod0", Context.MODE_PRIVATE);
    }

    public void clearPodSettings() {
        prefPod.edit().clear().apply();
    }

    public void clearAppSettings() {
        prefApp.edit().clear().apply();
    }

    private void setString(SharedPreferences pref, String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    private void setInt(SharedPreferences pref, String key, int value) {
        pref.edit().putInt(key, value).apply();
    }

    private void setBool(SharedPreferences pref, String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

    /*
    //   Preferences
     */
    public static class PREF {
        private static final String IS_LOAD_IMAGES = "loadImages";
        private static final String MINIMUM_FONT_SIZE = "minimumFontSize";
        private static final String PODUSERPROFILE_AVATAR_URL = "podUserProfile_avatar";
        private static final String PODUSERPROFILE_NAME = "podUserProfile_name";
        private static final String PODUSERPROFILE_ID = "podUserProfile_guid";
        private static final String PODDOMAIN = "podDomain";
    }


    /*
    //     Setters & Getters
    */
    public String getProfileId() {
        return prefPod.getString(PREF.PODUSERPROFILE_ID, "");
    }

    public void setProfileId(String profileId) {
        setString(prefPod, PREF.PODUSERPROFILE_ID,profileId);
    }


    public boolean isLoadImages() {
        return prefApp.getBoolean(PREF.IS_LOAD_IMAGES, true);
    }

    public void setLoadImages(boolean loadImages) {
        setBool(prefApp, PREF.IS_LOAD_IMAGES, loadImages);
    }


    public int getMinimumFontSize() {
        return prefApp.getInt(PREF.MINIMUM_FONT_SIZE, 8);
    }

    public void setMinimumFontSize(int minimumFontSize) {
        setInt(prefApp, PREF.MINIMUM_FONT_SIZE, minimumFontSize);
    }

    public String getAvatarUrl() {
        return prefPod.getString(PREF.PODUSERPROFILE_AVATAR_URL, "");
    }

    public void setAvatarUrl(String avatarUrl) {
        setString(prefPod, PREF.PODUSERPROFILE_AVATAR_URL, avatarUrl);
    }

    public String getName() {
        return prefPod.getString(PREF.PODUSERPROFILE_NAME, "");
    }

    public void setName(String name) {
        setString(prefPod, PREF.PODUSERPROFILE_NAME, name);
    }

    public String getPodDomain() {
        return prefPod.getString(PREF.PODDOMAIN, "");
    }

    public void setPodDomain(String podDomain) {
        setString(prefPod, PREF.PODDOMAIN, podDomain);
    }

    public boolean hasPodDomain(){
        return !prefPod.getString(PREF.PODDOMAIN, "").equals("");
    }
}

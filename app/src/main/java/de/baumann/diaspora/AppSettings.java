package de.baumann.diaspora;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by de-live-gdev on 20.03.16. Part of Diaspora WebApp.
 */
class AppSettings {
    private final SharedPreferences pref;
    private final Context context;

    public AppSettings(Context context) {
        this.context = context.getApplicationContext();
        pref = this.context.getSharedPreferences("app", Context.MODE_PRIVATE);
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
    private static final String PREF_WEBUSERPROFILE_ID = "webUserProfile_guid";
    private static final String PREF_IS_LOAD_IMAGES = "loadImages";
    private static final String PREF_MINIMUM_FONT_SIZE = "minimumFontSize";
    private static final String PREF_AVATAR_URL = "webUserProfile_avatar";
    private static final String PREF_WEBUSERPROFILE_NAME = "webUserProfile_name";
    private static final String PREF_PODDOMAIN = "podDomain";



    /*
    //     Setters & Getters
    */
    public String getProfileId() {
        return pref.getString(PREF_WEBUSERPROFILE_ID, "");
    }

    public void setProfileId(String profileId) {
        setString(PREF_WEBUSERPROFILE_ID, profileId);
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

    public String getAvatarUrl() {
        return pref.getString(PREF_AVATAR_URL, "");
    }

    public void setAvatarUrl(String avatarUrl) {
        setString(PREF_AVATAR_URL, avatarUrl);
    }

    public String getName(){
        return pref.getString(PREF_WEBUSERPROFILE_NAME, "");
    }

    public void setName(String name){
        setString(PREF_WEBUSERPROFILE_NAME, name);
    }

    public String getPodDomain(){
        return pref.getString(PREF_PODDOMAIN, "");
    }

    public void setPodDomain(String podDomain){
        setString(PREF_PODDOMAIN, podDomain);
    }


}

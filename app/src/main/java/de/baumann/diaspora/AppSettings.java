package de.baumann.diaspora;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by de-live-gdev on 20.03.16. Part of Diaspora WebApp.
 */
class AppSettings {
    private final SharedPreferences pref;

    public AppSettings(Context context){
        Context context1 = context.getApplicationContext();
        pref = context1.getSharedPreferences("app", Context.MODE_PRIVATE);
    }

    private void setString(String key, String value){
        pref.edit().putString(key,value).apply();
    }

    /*
    //     Setters & Getters
    */
    private static final String PREF_PROFILE_ID = "profileID";
    public String getProfileId(){
        return pref.getString(PREF_PROFILE_ID, "");
    }
    public void setProfileId(String profileId){
        setString(PREF_PROFILE_ID, profileId);
    }
}

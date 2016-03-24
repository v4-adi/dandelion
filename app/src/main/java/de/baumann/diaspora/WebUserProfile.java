package de.baumann.diaspora;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by de-live-gdev on 24.03.16.  Part of Diaspora WebApp.
 */
public class WebUserProfile {
    private final int MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF = 5000;
    JSONObject json;
    long lastLoaded;
    boolean isWebUserProfileLoaded;

    public WebUserProfile(){
    }

    public boolean isRefreshNeeded(){
        return (System.currentTimeMillis() - lastLoaded) >= MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF;
    }

    public boolean isWebUserProfileLoaded() {
        return isWebUserProfileLoaded;
    }

    public boolean loadFromJson(String json) {
        try {
            this.json = new JSONObject(json);
            lastLoaded = System.currentTimeMillis();
            isWebUserProfileLoaded = true;
        } catch (JSONException e) {
            Log.d(App.APP_LOG_TAG, e.getMessage());
            isWebUserProfileLoaded = false;
        }
        return isWebUserProfileLoaded;
    }

    /**
     * Get the Avatar URL's
     * @return Avatar URL's
     *   [0] small
     *   [1] medium
     *   [2] large
     */
    public String[] getAvatarUrls(){
        try {
            String[] avatars = new String[3];
            JSONObject o = json.getJSONObject("avatar");
            avatars[0] = o.getString("small");
            avatars[1] = o.getString("medium");
            avatars[2] = o.getString("large");
            return avatars;
        } catch (JSONException e) {
            return null;
        }
    }

    public int getId(){
        try {
            return json.getInt("id");
        } catch (JSONException e) {
            return 0;
        }
    }

    /**
     * Get the users profile address id
     * @return guid
     */
    public int getGuid(){
        try {
            return json.getInt("guid");
        } catch (JSONException e) {
            return 0;
        }
    }

    public String getName(){
        try {
            return json.getString("guid");
        } catch (JSONException e) {
            return null;
        }
    }

    public String getDiasporaAddress(){
        try {
            return json.getString("diaspora_id");
        } catch (JSONException e) {
            return null;
        }
    }

    public int getNotificationCount(){
        try {
            return json.getInt("notifications_count");
        } catch (JSONException e) {
            return 0;
        }
    }

    public int getUnreadMessagesCount(){
        try {
            return json.getInt("unread_messages_count");
        } catch (JSONException e) {
            return 0;
        }
    }

    public int getFollowingCount(){
        try {
            return json.getInt("following_count");
        } catch (JSONException e) {
            return 0;
        }
    }


    /*
     * Not implemented / not needed yet:
     *   boolean  "admin"
     *   boolean "moderator"
     *   array  "aspects"
     *      int "id"
     *      string "name"
     *      boolean "selected"
     *
     *   array  "services"
     *      ? ?
     *   array  "configured_services"
     *      ? ?
     *
     */
}

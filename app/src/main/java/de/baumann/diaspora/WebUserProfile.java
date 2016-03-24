package de.baumann.diaspora;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by de-live-gdev on 24.03.16.  Part of Diaspora WebApp.
 */
public class WebUserProfile {
    private static final int MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF = 5000;

    private Handler uiHandler;
    private WebUserProfileChangedListener listener;
    private App app;
    private AppSettings appSettings;
    private JSONObject json;
    private long lastLoaded;
    private boolean isWebUserProfileLoaded;

    private String avatarUrl;
    private String guid;
    private String name;
    private int notificationCount;
    private int unreadMessagesCount;

    public WebUserProfile(App app, Handler uiHandler, WebUserProfileChangedListener listener) {
        this.listener = listener;
        this.uiHandler = uiHandler;
        this.app = app;
        appSettings = app.getSettings();

        avatarUrl = appSettings.getAvatarUrl();
        guid = appSettings.getProfileId();
        name = appSettings.getName();
    }

    public boolean isRefreshNeeded() {
        return (System.currentTimeMillis() - lastLoaded) >= MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF;
    }

    public boolean isWebUserProfileLoaded() {
        return isWebUserProfileLoaded;
    }

    public boolean parseJson(String jsonStr) {
        try {
            this.json = new JSONObject(jsonStr);
            lastLoaded = System.currentTimeMillis();

            String str;
            int integer;

            // Avatar
            if (json.has("avatar")) {
                JSONObject avatarJson = json.getJSONObject("avatar");
                if (avatarJson.has("medium") && !((str = avatarJson.getString("medium")).equals(avatarUrl))) {
                    app.getAvatarImageLoader().clearAvatarImage();
                    avatarUrl = str;
                    appSettings.setAvatarUrl(str);
                    uiHandler.post(new Runnable() {
                        public void run() {
                            listener.onUserProfileAvatarChanged(avatarUrl);
                        }
                    });
                }
            }

            // GUID (User id)
            if (json.has("guid") && !((str = json.getString("guid")).equals(guid))) {
                guid = str;
                appSettings.setProfileId(guid);
            }

            // Name
            if (json.has("name") && !((str = json.getString("name")).equals(name))) {
                name = str;
                appSettings.setName(name);
                uiHandler.post(new Runnable() {
                    public void run() {
                        listener.onUserProfileNameChanged(name);
                    }
                });
            }

            // Unread message count
            if (json.has("notifications_count") && (integer = json.getInt("notifications_count")) != notificationCount) {
                notificationCount = integer;
                uiHandler.post(new Runnable() {
                    public void run() {
                        listener.onNotificationCountChanged(notificationCount);
                    }
                });
            }

            // Unread message count
            if (json.has("unread_messages_count") && (integer = json.getInt("unread_messages_count")) != unreadMessagesCount) {
                unreadMessagesCount = integer;
                uiHandler.post(new Runnable() {
                    public void run() {
                        listener.onUnreadMessageCountChanged(unreadMessagesCount);
                    }
                });
            }

            isWebUserProfileLoaded = true;
        } catch (JSONException e) {
            Log.d(App.APP_LOG_TAG, e.getMessage());
            isWebUserProfileLoaded = false;
        }
        lastLoaded = System.currentTimeMillis();
        return isWebUserProfileLoaded;
    }

    /*
    //  Getters & Setters
     */

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getGuid() {
        return guid;
    }

    public String getName() {
        return name;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    /*
     * Not implemented / not needed yet:
     *   string "diasporaAddress"
     *   int "id"
     *   boolean  "admin"
     *   int "following_count"
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
     */
}

interface WebUserProfileChangedListener {
    void onUserProfileNameChanged(String name);
    void onUserProfileAvatarChanged(String avatarUrl);
    void onNotificationCountChanged(int notificationCount);
    void onUnreadMessageCountChanged(int unreadMessageCount);
}

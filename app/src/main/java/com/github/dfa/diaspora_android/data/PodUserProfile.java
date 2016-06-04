package com.github.dfa.diaspora_android.data;

import android.os.Handler;
import android.util.Log;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.listener.WebUserProfileChangedListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gsantner on 24.03.16.  Part of Diaspora for Android.
 */
public class PodUserProfile {
    private static final int MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF = 5000;

    private Handler callbackHandler;
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


    public PodUserProfile(App app) {
        this.app = app;
        appSettings = app.getSettings();

        avatarUrl = appSettings.getAvatarUrl();
        guid = appSettings.getProfileId();
        name = appSettings.getName();
    }

    public PodUserProfile(App app, Handler callbackHandler, WebUserProfileChangedListener listener) {
        this(app);
        this.listener = listener;
        this.callbackHandler = callbackHandler;
    }

    public boolean isRefreshNeeded() {
        return (System.currentTimeMillis() - lastLoaded) >= MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF;
    }

    public boolean isWebUserProfileLoaded() {
        return isWebUserProfileLoaded;
    }

    public boolean parseJson(String jsonStr) {
        try {
            json = new JSONObject(jsonStr);
            lastLoaded = System.currentTimeMillis();

            // Avatar
            if (json.has("avatar")) {
                JSONObject avatarJson = json.getJSONObject("avatar");
                if (avatarJson.has("medium") && setAvatarUrl(avatarJson.getString("medium"))) {
                    app.getAvatarImageLoader().clearAvatarImage();
                    appSettings.setAvatarUrl(avatarUrl);
                }
            }

            // GUID (User id)
            if (json.has("guid") && loadGuid(json.getString("guid"))) {
                appSettings.setProfileId(guid);
            }

            // Name
            if (json.has("name") && loadName(json.getString("name"))) {
                appSettings.setName(name);
            }

            // Unread message count
            if (json.has("notifications_count") && loadNotificationCount(json.getInt("notifications_count"))) {
            }

            // Unread message count
            if (json.has("unread_messages_count") && loadUnreadMessagesCount(json.getInt("unread_messages_count"))) {
            }
            isWebUserProfileLoaded = true;
        } catch (JSONException e) {
            Log.d(App.TAG, e.getMessage());
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
     * Private property setters
     */
    private boolean setAvatarUrl(final String avatarUrl) {
        if (!this.avatarUrl.equals(avatarUrl)) {
            this.avatarUrl = avatarUrl;
            if (listener != null && callbackHandler != null) {
                callbackHandler.post(new Runnable() {
                    public void run() {
                        listener.onUserProfileAvatarChanged(avatarUrl);
                    }
                });
            }
            return true;
        }
        return false;
    }

    private boolean loadGuid(final String guid) {
        if (!this.guid.equals(guid)) {
            this.guid = guid;
            return true;
        }
        return false;
    }

    private boolean loadName(final String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            if (listener != null && callbackHandler != null) {
                callbackHandler.post(new Runnable() {
                    public void run() {
                        listener.onUserProfileNameChanged(name);
                    }
                });
            }
            return true;
        }
        return false;
    }

    private boolean loadNotificationCount(final int notificationCount) {
        if (this.notificationCount != notificationCount) {
            this.notificationCount = notificationCount;
            if (listener != null && callbackHandler != null) {
                callbackHandler.post(new Runnable() {
                    public void run() {
                        listener.onNotificationCountChanged(notificationCount);
                    }
                });
            }
            return true;
        }
        return false;
    }

    private boolean loadUnreadMessagesCount(final int unreadMessagesCount) {
        if (this.unreadMessagesCount != unreadMessagesCount) {
            this.unreadMessagesCount = unreadMessagesCount;
            if (listener != null && callbackHandler != null) {
                callbackHandler.post(new Runnable() {
                    public void run() {
                        listener.onUnreadMessageCountChanged(unreadMessagesCount);
                    }
                });
            }
            return true;
        }
        return false;
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

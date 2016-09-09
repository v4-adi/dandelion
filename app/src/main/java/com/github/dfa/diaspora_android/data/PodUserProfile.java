/*
    This file is part of the Diaspora for Android.

    Diaspora for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora for Android.

    If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.dfa.diaspora_android.data;

import android.os.Handler;
import com.github.dfa.diaspora_android.util.Log;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.listener.WebUserProfileChangedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User profile
 * Created by gsantner (https://gsantner.github.io/) on 24.03.16.  Part of Diaspora for Android.
 */
public class PodUserProfile {
    private static final int MINIMUM_WEBUSERPROFILE_LOAD_TIMEDIFF = 5000;

    private Handler callbackHandler;
    private WebUserProfileChangedListener listener;
    private final App app;
    private final AppSettings appSettings;
    private JSONObject json;
    private long lastLoaded;
    private boolean isWebUserProfileLoaded;

    private String avatarUrl;
    private String guid;
    private String name;
    private PodAspect[] podAspects;
    private String[] followedTags;
    private int notificationCount;
    private int unreadMessagesCount;


    public PodUserProfile(App app) {
        this.app = app;
        appSettings = app.getSettings();

        avatarUrl = appSettings.getAvatarUrl();
        guid = appSettings.getProfileId();
        name = appSettings.getName();
        podAspects = appSettings.getPodAspects();
        followedTags = appSettings.getFollowedTags();
        notificationCount = appSettings.getNotificationCount();
        unreadMessagesCount = appSettings.getUnreadMessageCount();
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
                if (avatarJson.has("large") && setAvatarUrl(avatarJson.getString("large"))) {
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
                appSettings.setNotificationCount(notificationCount);
            }

            // Unread message count
            if (json.has("unread_messages_count") && loadUnreadMessagesCount(json.getInt("unread_messages_count"))) {
                appSettings.setUnreadMessageCount(unreadMessagesCount);
            }

            // Aspect
            if (json.has("aspects") && loadAspects(json.getJSONArray("aspects"))) {
                appSettings.setPodAspects(podAspects);
            }

            // Followed tags
            if (json.has("android_app.followed_tags")
                    && loadFollowedTags(json.getJSONArray("android_app.followed_tags"))) {
                appSettings.setFollowedTags(followedTags);
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

    public PodAspect[] getAspects() {
        return podAspects;
    }

    public String[] getFollowedTags() {
        return followedTags;
    }

    /*
     * Private property setters
     */

    /**
     * Sets the avatar, returns true if this was a new one, false if already the old one
     * @param avatarUrl url
     * @return true if new avatar url
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

    private boolean loadAspects(final JSONArray jsonAspects) throws JSONException {
        podAspects = new PodAspect[jsonAspects.length()];
        for (int i = 0; i < jsonAspects.length(); i++) {
            podAspects[i] = new PodAspect(jsonAspects.getJSONObject(i));
        }
        return true;
    }

    private boolean loadFollowedTags(final JSONArray jsonTags) throws JSONException {
        followedTags = new String[jsonTags.length()];
        for (int i = 0; i < jsonTags.length(); i++) {
            followedTags[i] = jsonTags.getString(i);
        }
        return true;
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

    public Handler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(Handler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public WebUserProfileChangedListener getListener() {
        return listener;
    }

    public void setListener(WebUserProfileChangedListener listener) {
        this.listener = listener;
    }

    /*
     * Not implemented / not needed yet:
     *   string "diasporaAddress"
     *   int "id"
     *   boolean  "admin"
     *   int "following_count"
     *   boolean "moderator"
     *
     *   array  "services"
     *      ? ?
     *   array  "configured_services"
     *      ? ?
     */
}

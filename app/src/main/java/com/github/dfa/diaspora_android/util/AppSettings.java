/*
    This file is part of the dandelion*.
    dandelion* is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    dandelion* is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with the dandelion*.
    If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.dfa.diaspora_android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.BuildConfig;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.DiasporaAspect;
import com.github.dfa.diaspora_android.data.DiasporaPodList.DiasporaPod;
import com.github.dfa.diaspora_android.web.ProxyHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.github.gsantner.opoc.util.AppSettingsBase;

/**
 * Settings
 * Created by gsantner (https://gsantner.github.io/) on 20.03.16. Part of dandelion*.
 */
@SuppressWarnings("ConstantConditions")
public class AppSettings extends AppSettingsBase {
    private final SharedPreferences prefPod;
    private DiasporaPod currentPod0Cached;

    public static AppSettings get() {
        return new AppSettings(App.get());
    }

    private AppSettings(Context context) {
        super(context);
        prefPod = this.context.getSharedPreferences("pod0", Context.MODE_PRIVATE);
    }

    /**
     * Clear all settings in prefPod (Settings related to the configured pod)
     * This uses commit instead of apply, since
     * SettingsActivity.SettingsFragmentDebugging.showWipeSettingsDialog()
     * kills the app after the calling this, so we have to block until we are finished.
     */
    @SuppressLint("CommitPrefEdits")
    public void resetPodSettings() {
        super.resetSettings(prefPod);
    }

    /**
     * Clear all settings in prefApp (related to the App itself)
     * This uses commit instead of apply, since
     * SettingsActivity.SettingsFragmentDebugging.showWipeSettingsDialog()
     * kills the app after the calling this, so we have to block until we are finished.
     */
    @SuppressLint("CommitPrefEdits")
    public void resetAppSettings() {
        super.resetSettings(prefApp);
    }

    //#################################
    //## Getter & Setter for settings
    //#################################
    public String getProfileId() {
        return getString(prefPod, R.string.pref_key__podprofile_id, "");
    }

    public void setProfileId(String profileId) {
        setString(prefPod, R.string.pref_key__podprofile_id, profileId);
    }

    public boolean isLoadImages() {
        return getBool(prefApp, R.string.pref_key__load_images, true);
    }

    public int getMinimumFontSize() {
        switch (getString(prefApp, R.string.pref_key__font_size, "")) {
            case "huge":
                return 20;
            case "large":
                return 16;
            case "normal":
                return 8;
            default:
                setString(prefApp, R.string.pref_key__font_size, "normal");
                return 8;
        }
    }

    public String getAvatarUrl() {
        return getString(prefPod, R.string.pref_key__podprofile_avatar_url, "");
    }

    public void setAvatarUrl(String avatarUrl) {
        setString(prefPod, R.string.pref_key__podprofile_avatar_url, avatarUrl);
    }

    public String getName() {
        return getString(prefPod, R.string.pref_key__podprofile_name, "");
    }

    public void setName(String name) {
        setString(prefPod, R.string.pref_key__podprofile_name, name);
    }

    public DiasporaPod getPod() {
        if (currentPod0Cached == null) {
            String pref = getString(prefPod, R.string.pref_key__current_pod_0, "");

            try {
                currentPod0Cached = new DiasporaPod().fromJson(new JSONObject(pref));
            } catch (JSONException e) {
                currentPod0Cached = null;
            }
        }
        return currentPod0Cached;
    }

    public void setPod(DiasporaPod pod) {
        try {
            setString(prefPod, R.string.pref_key__current_pod_0,
                    pod == null ? null : pod.toJson().toString());
            currentPod0Cached = pod;
        } catch (JSONException ignored) {
        }
    }

    public boolean hasPod() {
        return !getString(prefPod, R.string.pref_key__current_pod_0, "").equals("");
    }

    public void setPodAspects(DiasporaAspect[] aspects) {
        setStringArray(prefPod, R.string.pref_key__podprofile_aspects, aspects);
    }

    public DiasporaAspect[] getAspects() {
        String[] s = getStringArray(prefPod, R.string.pref_key__podprofile_aspects);
        DiasporaAspect[] aspects = new DiasporaAspect[s.length];
        for (int i = 0; i < aspects.length; i++) {
            aspects[i] = new DiasporaAspect(s[i]);
        }
        return aspects;
    }

    public String[] getFollowedTags() {
        return getStringArray(prefPod, R.string.pref_key__podprofile_followed_tags);
    }

    public void setFollowedTags(String[] values) {
        setStringArray(prefPod, R.string.pref_key__podprofile_followed_tags, values);
    }

    public String[] getFollowedTagsFavs() {
        return getStringArray(prefPod, R.string.pref_key__podprofile_followed_tags_favs);
    }

    public void setFollowedTagsFavs(List<String> values) {
        setStringList(prefPod, R.string.pref_key__podprofile_followed_tags_favs, values);
    }

    public String[] getAspectFavs() {
        return getStringArray(prefPod, R.string.pref_key__podprofile_aspects_favs);
    }

    public void setAspectFavs(List<String> values) {
        setStringList(prefPod, R.string.pref_key__podprofile_aspects_favs, values);
    }

    public int getUnreadMessageCount() {
        return getInt(prefPod, R.string.pref_key__podprofile_unread_message_count, 0);
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        setInt(prefPod, R.string.pref_key__podprofile_unread_message_count, unreadMessageCount);
    }

    public int getNotificationCount() {
        return getInt(prefPod, R.string.pref_key__podprofile_notification_count, 0);
    }

    public void setNotificationCount(int notificationCount) {
        setInt(prefPod, R.string.pref_key__podprofile_notification_count, notificationCount);
    }

    public boolean isAppendSharedViaApp() {
        return getBool(prefApp, R.string.pref_key__append_shared_via_app, true);
    }

    @SuppressLint("CommitPrefEdits")
    public void setProxyHttpEnabled(boolean enabled) {
        //commit instead of apply because the app is likely to be killed before apply is called.
        prefApp.edit().putBoolean(context.getString(R.string.pref_key__http_proxy_enabled), enabled).commit();
    }

    /**
     * Default return value: false
     *
     * @return whether proxy is enabled or not
     */
    public boolean isProxyHttpEnabled() {
        try {
            return getBool(prefApp, R.string.pref_key__http_proxy_enabled, false);
        } catch (ClassCastException e) {
            setProxyHttpEnabled(false);
            return false;
        }
    }

    public boolean wasProxyEnabled() {
        return getBool(prefApp, R.string.pref_key__proxy_was_enabled, false);
    }

    /**
     * Needed in order to determine, whether the proxy has just been disabled (trigger app restart)
     * or if proxy was disabled before (do not restart app)
     *
     * @param b new value
     */
    @SuppressLint("CommitPrefEdits")
    public void setProxyWasEnabled(boolean b) {
        prefApp.edit().putBoolean(context.getString(R.string.pref_key__proxy_was_enabled), b).commit();
    }

    /**
     * Default value: ""
     *
     * @return proxy host
     */
    public String getProxyHttpHost() {
        return getString(prefApp, R.string.pref_key__http_proxy_host, "");
    }

    public void setProxyHttpHost(String value) {
        setString(prefApp, R.string.pref_key__http_proxy_host, value);
    }

    /**
     * Default value: 0
     *
     * @return proxy port
     */
    public int getProxyHttpPort() {
        try {
            String str = getString(prefApp, R.string.pref_key__http_proxy_port, "0");
            return Integer.parseInt(str);
        } catch (ClassCastException e) {
            int port = getInt(prefApp, R.string.pref_key__http_proxy_port, 0);
            setProxyHttpPort(port);
            return port;
        }
    }

    public void setProxyHttpPort(int value) {
        setString(prefApp, R.string.pref_key__http_proxy_port, Integer.toString(value));
    }

    public ProxyHandler.ProxySettings getProxySettings() {
        return new ProxyHandler.ProxySettings(isProxyHttpEnabled(), getProxyHttpHost(), getProxyHttpPort());
    }

    public boolean isIntellihideToolbars() {
        return getBool(prefApp, R.string.pref_key__intellihide_toolbars, true);
    }

    public boolean isChromeCustomTabsEnabled() {
        return getBool(prefApp, R.string.pref_key__chrome_custom_tabs_enabled, true);
    }

    public boolean isLoggingEnabled() {
        return getBool(prefApp, R.string.pref_key__logging_enabled, false);
    }

    public boolean isLoggingSpamEnabled() {
        return getBool(prefApp, R.string.pref_key__logging_spam_enabled, false);
    }

    public boolean isVisibleInNavExit() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__exit, false);
    }

    public boolean isVisibleInNavHelp_license() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__help_license, true);
    }

    public boolean isVisibleInNavPublic_activities() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__public_activities, false);
    }

    public boolean isVisibleInNavMentions() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__mentions, false);
    }

    public boolean isVisibleInNavCommented() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__commented, true);
    }

    public boolean isVisibleInNavLiked() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__liked, true);
    }

    public boolean isVisibleInNavActivities() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__activities, true);
    }

    public boolean isVisibleInNavAspects() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__aspects, true);
    }

    public boolean isVisibleInNavFollowed_tags() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__followed_tags, true);
    }

    public boolean isVisibleInNavProfile() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__profile, true);
    }

    public boolean isVisibleInNavContacts() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__contacts, false);
    }

    public boolean isVisibleInNavStatistics() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__statistics, false);
    }

    public boolean isVisibleInNavReports() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__reports, false);
    }

    public boolean isVisibleToggleMobileDesktop() {
        return getBool(prefApp, R.string.pref_key__visibility_nav__toggle_mobile_desktop, false);
    }

    public boolean isTopbarStreamShortcutEnabled() {
        return getBool(prefApp, R.string.pref_key__topbar_stream_shortcut, false);
    }

    public String getScreenRotation() {
        return getString(prefApp, R.string.pref_key__screen_rotation, R.string.rotation_val_system);
    }

    public boolean isAppFirstStart() {
        boolean value = getBool(prefApp, R.string.pref_key__app_first_start, true);
        setBool(prefApp, R.string.pref_key__app_first_start, false);
        return value;
    }

    public boolean isAppCurrentVersionFirstStart() {
        int value = getInt(prefApp, R.string.pref_key__app_first_start_current_version, -1);
        setInt(prefApp, R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
        return value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
    }

    public long getLastVisitedPositionInStream() {
        return getLong(prefPod, R.string.pref_key__podprofile_last_stream_position, -1);
    }

    public void setLastVisitedPositionInStream(long timestamp) {
        setLong(prefPod, R.string.pref_key__podprofile_last_stream_position, timestamp);
    }

    public void setLanguage(String value) {
        setString(prefApp, R.string.pref_key__language, value);
    }

    public String getLanguage() {
        return getString(prefApp, R.string.pref_key__language, "");
    }

    public void setPrimaryColorSettings(int base, int shade) {
        setInt(prefApp, R.string.pref_key__primary_color_base, base);
        setInt(prefApp, R.string.pref_key__primary_color_shade, shade);
    }

    public int[] getPrimaryColorSettings() {
        return new int[]{
                getInt(prefApp, R.string.pref_key__primary_color_base, rcolor(R.color.md_blue_650)),
                getInt(prefApp, R.string.pref_key__primary_color_shade, rcolor(R.color.primary))
        };
    }

    @SuppressWarnings("ConstantConditions")
    public int getPrimaryColor() {
        if (isAmoledColorMode()) {
            return Color.BLACK;
        } else {
            return getInt(prefApp, R.string.pref_key__primary_color_shade, rcolor(
                    BuildConfig.IS_TEST_BUILD ? R.color.md_brown_800 : R.color.primary));
        }
    }

    public void setAccentColorSettings(int base, int shade) {
        setInt(prefApp, R.string.pref_key__accent_color_base, base);
        setInt(prefApp, R.string.pref_key__accent_color_shade, shade);
    }

    public int[] getAccentColorSettings() {
        return new int[]{
                getInt(prefApp, R.string.pref_key__accent_color_base, rcolor(R.color.md_green_400)),
                getInt(prefApp, R.string.pref_key__accent_color_shade, rcolor(R.color.accent))
        };
    }

    public int getAccentColor() {
        return getInt(prefApp, R.string.pref_key__accent_color_shade, rcolor(R.color.accent));
    }

    public boolean isExtendedNotificationsActivated() {
        return getBool(prefApp, R.string.pref_key__extended_notifications, false);
    }

    public boolean isAmoledColorMode() {
        return getBool(prefApp, R.string.pref_key__primary_color__amoled_mode, false);
    }

    public boolean isAdBlockEnabled() {
        return getBool(prefApp, R.string.pref_key__adblock_enable, true);
    }
}
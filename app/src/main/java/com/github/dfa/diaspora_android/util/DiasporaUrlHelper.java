package com.github.dfa.diaspora_android.util;

import com.github.dfa.diaspora_android.data.AppSettings;

/**
 * Created by vanitas on 10.08.16.
 */
public class DiasporaUrlHelper {
    private AppSettings settings;

    public static final String HTTPS = "https://";
    public static final String SUBURL_NOTIFICATIONS = "/notifications";
    public static final String SUBURL_POSTS = "/posts/";
    public static final String SUBURL_STREAM = "/stream";
    public static final String SUBURL_CONVERSATIONS = "/conversations";
    public static final String SUBURL_NEW_POST = "/status_messages/new";
    public static final String SUBURL_PEOPLE = "/people/";
    public static final String SUBURL_ACTIVITY = "/activity";
    public static final String SUBURL_LIKED = "/liked";
    public static final String SUBURL_COMMENTED = "/commented";
    public static final String SUBURL_MENTIONS = "/mentions";
    public static final String SUBURL_PUBLIC = "/public";
    public static final String SUBURL_TOGGLE_MOBILE = "/mobile/toggle";
    public static final String SUBURL_SEARCH_TAGS = "/tags/";
    public static final String SUBURL_SEARCH_PEOPLE = "/people.mobile?q=";
    public static final String SUBURL_FOLOWED_TAGS = "/followed_tags";
    public static final String SUBURL_ASPECTS = "/aspects";
    public static final String URL_BLANK = "about:blank";

    public DiasporaUrlHelper(AppSettings settings) {
        this.settings = settings;
    }

    public String getPodUrl() {
        return HTTPS+settings.getPodDomain();
    }

    public String getStreamUrl() {
        return getPodUrl()+SUBURL_STREAM;
    }

    public String getNotificationsUrl() {
        return getPodUrl()+SUBURL_NOTIFICATIONS;
    }

    public String getPostsUrl() {
        return getPodUrl()+SUBURL_POSTS;
    }

    public String getConversationsUrl() {
        return getPodUrl()+SUBURL_CONVERSATIONS;
    }

    public String getNewPostUrl() {
        return getPodUrl()+SUBURL_NEW_POST;
    }

    public String getProfileUrl() {
        return getPodUrl()+SUBURL_PEOPLE+settings.getProfileId();
    }

    public String getActivityUrl() {
        return getPodUrl()+SUBURL_ACTIVITY;
    }

    public String getLikedPostsUrl() {
        return getPodUrl()+SUBURL_LIKED;
    }

    public String getCommentedUrl() {
        return getPodUrl()+SUBURL_COMMENTED;
    }

    public String getMentionsUrl() {
        return getPodUrl()+SUBURL_MENTIONS;
    }

    public String getPublicUrl() {
        return getPodUrl()+SUBURL_PUBLIC;
    }

    public String getToggleMobileUrl() {
        return getPodUrl()+SUBURL_TOGGLE_MOBILE;
    }

    public String getSearchTagsUrl(String query) {
        return getPodUrl()+SUBURL_SEARCH_TAGS+query;
    }

    public String getSearchPeopleUrl(String query) {
        return getPodUrl()+SUBURL_SEARCH_PEOPLE+query;
    }

    public String getBlankUrl() {
        return URL_BLANK;
    }
}

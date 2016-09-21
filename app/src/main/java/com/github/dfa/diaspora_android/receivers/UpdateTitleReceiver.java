package com.github.dfa.diaspora_android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.DiasporaUrlHelper;

/**
 * BroadcastReceiver used to update the title of the MainActivity depending on the url of the ui__webview
 * Created by vanitas on 11.09.16.
 */
public class UpdateTitleReceiver extends BroadcastReceiver {
    private DiasporaUrlHelper urls;
    private AppSettings appSettings;
    private App app;
    private TitleCallback callback;

    public UpdateTitleReceiver(App app, DiasporaUrlHelper urls, TitleCallback callback) {
        this.urls = urls;
        this.app = app;
        this.appSettings = app.getSettings();
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getStringExtra(MainActivity.EXTRA_URL);
        if (url != null && url.startsWith(urls.getPodUrl())) {
            String subUrl = url.substring((urls.getPodUrl()).length());
            AppLog.spam(this, "onReceive()- Set title for subUrl " + subUrl);
            if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_STREAM)) {
                setTitle(R.string.nav_stream);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_POSTS)) {
                setTitle(R.string.diaspora);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_NOTIFICATIONS)) {
                setTitle(R.string.notifications);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_CONVERSATIONS)) {
                setTitle(R.string.conversations);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_NEW_POST)) {
                setTitle(R.string.new_post);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_PEOPLE + appSettings.getProfileId())) {
                setTitle(R.string.nav_profile);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_ACTIVITY)) {
                setTitle(R.string.nav_activities);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_LIKED)) {
                setTitle(R.string.nav_liked);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_COMMENTED)) {
                setTitle(R.string.nav_commented);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_MENTIONS)) {
                setTitle(R.string.nav_mentions);
            } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_PUBLIC)) {
                setTitle(R.string.public_);
            } else if (urls.isAspectUrl(url)) {
                setTitle(urls.getAspectNameFromUrl(url, app));
            }
        } else {
            AppLog.spam(this, "onReceive()- Invalid url: " + url);
        }
    }

    private void setTitle(int rId) {
        callback.setTitle(rId);
    }

    private void setTitle(String title) {
        callback.setTitle(title);
    }

    public interface TitleCallback {
        void setTitle(int Rid);

        void setTitle(String title);
    }
}

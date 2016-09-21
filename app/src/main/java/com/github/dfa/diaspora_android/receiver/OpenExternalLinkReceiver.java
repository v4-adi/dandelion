package com.github.dfa.diaspora_android.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.BrowserFallback;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.CustomTabActivityHelper;
import com.github.dfa.diaspora_android.util.Helpers;

/**
 * BroadcastReceiver that opens links in a Chrome CustomTab
 * Created by vanitas on 11.09.16.
 */
public class OpenExternalLinkReceiver extends BroadcastReceiver {
    private final Activity parent;

    public OpenExternalLinkReceiver(Activity parent) {
        this.parent = parent;
    }

    @Override
    public void onReceive(Context c, Intent receiveIntent) {
        AppSettings settings = new AppSettings(c);

       AppLog.v(this, "OpenExternalLinkReceiver.onReceive(): url");

        Uri url = null;
        try {
            String sUrl = receiveIntent.getStringExtra(MainActivity.EXTRA_URL);
            url = Uri.parse(sUrl);
        } catch (Exception _ignored) {
           AppLog.v(this, "Could not open Chrome Custom Tab (bad URL)");
            return;
        }

        if (settings.isChromeCustomTabsEnabled()) {
            // Setup Chrome Custom Tab
            CustomTabsIntent.Builder customTab = new CustomTabsIntent.Builder();
            customTab.setToolbarColor(Helpers.getColorFromRessource(c, R.color.colorPrimary));
            customTab.addDefaultShareMenuItem();

            Bitmap backButtonIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.chrome_custom_tab__back);
            customTab.setCloseButtonIcon(backButtonIcon);

            // Launch Chrome Custom Tab
            CustomTabActivityHelper.openCustomTab(parent, customTab.build(), url, new BrowserFallback());
        } else {
            // Open in normal browser (via intent)
            Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, url);
            openBrowserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(openBrowserIntent);
        }
    }
}

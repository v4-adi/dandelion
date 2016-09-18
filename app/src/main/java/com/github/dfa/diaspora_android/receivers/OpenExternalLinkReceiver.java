package com.github.dfa.diaspora_android.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.BrowserFallback;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.CustomTabActivityHelper;
import com.github.dfa.diaspora_android.util.Helpers;
import com.github.dfa.diaspora_android.util.Log;

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

        Log.v(App.TAG, "OpenExternalLinkReceiver.onReceive(): url");

        Uri url = null;
        try {
            String sUrl = receiveIntent.getStringExtra(MainActivity.EXTRA_URL);
            url = Uri.parse(sUrl);
        } catch (Exception _ignored) {
            Log.v(App.TAG, "Could not open Chrome Custom Tab (bad URL)");
            return;
        }

        if (settings.isChromeCustomTabsEnabled()) {
            // Setup Chrome Custom Tab
            CustomTabsIntent.Builder customTab = new CustomTabsIntent.Builder();
            customTab.setToolbarColor(Helpers.getColorFromRessource(c, R.color.colorPrimary));
            customTab.setStartAnimations(c, android.R.anim.slide_in_left, android.R.anim.fade_out);
            customTab.setExitAnimations(c, android.R.anim.fade_in, android.R.anim.slide_out_right);
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

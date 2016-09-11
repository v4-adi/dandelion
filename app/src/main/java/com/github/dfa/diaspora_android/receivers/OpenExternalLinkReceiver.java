package com.github.dfa.diaspora_android.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsIntent;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.BrowserFallback;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.CustomTabActivityHelper;
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
    public void onReceive(Context context, Intent intent) {
        String url = intent.getStringExtra(MainActivity.EXTRA_URL);
        Log.v(App.TAG, "OpenExternalLinkReceiver.onReceive(): url");
        if(url != null) {
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            if(Build.VERSION.SDK_INT >= 23) {
                intentBuilder.setToolbarColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
            } else {
                intentBuilder.setToolbarColor(context.getResources().getColor(R.color.colorPrimary));
            }
            intentBuilder.setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.fade_out);
            intentBuilder.setExitAnimations(context, android.R.anim.fade_in, android.R.anim.slide_out_right);
            Bitmap backButtonIcon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_arrow_back_white_24px);
            intentBuilder.setCloseButtonIcon(backButtonIcon);
            CustomTabActivityHelper.openCustomTab(parent, intentBuilder.build(), Uri.parse(url), new BrowserFallback());
        }
    }
}

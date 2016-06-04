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

package com.github.dfa.diaspora_android.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.webkit.WebView;

import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;

public class Helpers {

    public static boolean isOnline(Context context) {
        ConnectivityManager cnm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cnm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static void animateToActivity(Activity from, Class to, boolean finishFromActivity) {
        Intent intent = new Intent(from, to);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        if (finishFromActivity) {
            from.finish();
        }
    }

    public static void hideTopBar(final WebView wv) {
        wv.loadUrl("javascript: ( function() {" +
                "    if(document.getElementById('main_nav')) {" +
                "        document.getElementById('main_nav').parentNode.removeChild(" +
                "        document.getElementById('main_nav'));" +
                "    } else if (document.getElementById('main-nav')) {" +
                "        document.getElementById('main-nav').parentNode.removeChild(" +
                "        document.getElementById('main-nav'));" +
                "    }" +
                "})();");
    }

    public static void getNotificationCount(final WebView wv) {
        wv.loadUrl("javascript: ( function() {" +
                "if (document.getElementById('notification')) {" +
                "       var count = document.getElementById('notification').innerHTML;" +
                "       AndroidBridge.setNotificationCount(count.replace(/(\\r\\n|\\n|\\r)/gm, \"\"));" +
                "    } else {" +
                "       AndroidBridge.setNotificationCount('0');" +
                "    }" +
                "    if (document.getElementById('conversation')) {" +
                "       var count = document.getElementById('conversation').innerHTML;" +
                "       AndroidBridge.setConversationCount(count.replace(/(\\r\\n|\\n|\\r)/gm, \"\"));" +
                "    } else {" +
                "       AndroidBridge.setConversationCount('0');" +
                "    }" +
                "})();");
    }

    public static void getUserProfile(final WebView wv) {
        wv.loadUrl("javascript: ( function() {" +
                "    if (typeof gon !== 'undefined' && typeof gon.user !== 'undefined') {" +
                "       var userProfile = JSON.stringify(gon.user);" +
                "       AndroidBridge.setUserProfile(userProfile.toString());" +
                "    } " +
                "})();");
    }
}

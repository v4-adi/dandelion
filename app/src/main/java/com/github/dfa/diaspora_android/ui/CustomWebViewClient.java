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
package com.github.dfa.diaspora_android.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.dfa.diaspora_android.App;

public class CustomWebViewClient extends WebViewClient {
    private App app;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;

    public CustomWebViewClient(App app, SwipeRefreshLayout swipeRefreshLayout, WebView webView) {
        this.app = app;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.webView = webView;
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!url.contains(app.getSettings().getPodDomain())) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.getApplicationContext().startActivity(i);
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        swipeRefreshLayout.setEnabled(true);
        if(url.contains(app.getSettings().getPodDomain()+"/conversations/") || url.endsWith("status_messages/new") || url.equals("about:blank")){
            swipeRefreshLayout.setEnabled(false);
        }
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        swipeRefreshLayout.setRefreshing(false);

        final CookieManager cookieManager = app.getCookieManager();
        String cookies = cookieManager.getCookie(url);
        //Log.d(App.TAG, "All the cookies in a string:" + cookies);

        if (cookies != null) {
            cookieManager.setCookie(url, cookies);
            cookieManager.setCookie("https://" + app.getSettings().getPodDomain(), cookies);
            //for (String c : cookies.split(";")) {
            // Log.d(App.TAG, "Cookie: " + c.split("=")[0] + " Value:" + c.split("=")[1]);
            //}
            //new ProfileFetchTask(app).execute();
        }
    }

}

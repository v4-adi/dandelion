package com.github.dfa.diaspora_android.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.ui.ContextMenuWebView;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.DiasporaUrlHelper;
import com.github.dfa.diaspora_android.util.WebHelper;

/**
 * Fragment that contains a WebView displaying the stream of the user
 * Created by vanitas on 21.09.16.
 */

public class StreamFragment extends WebViewFragment {
    public static final String TAG = "com.github.dfa.diaspora_android.StreamFragment";

    private DiasporaUrlHelper urls;
    private Bundle webViewState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stream__fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.webView = (ContextMenuWebView) view.findViewById(R.id.webView);
        this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        this.appSettings = ((App) getActivity().getApplication()).getSettings();
        this.urls = new DiasporaUrlHelper(appSettings);

        this.setup(
                webView,
                progressBar,
                appSettings);

        if(webView.getUrl() == null) {
            loadUrl(urls.getPodUrl());
        }
        this.setRetainInstance(true);
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stream__menu_top, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateBottomOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stream__menu_bottom, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.d(this, "StreamFragment.onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.action_reload: {
                if(WebHelper.isOnline(getContext())) {
                    reloadUrl();
                    return true;
                } else {
                    return false;
                }
            }

            case R.id.action_toggle_desktop_page: {
                loadUrl(urls.getToggleMobileUrl());
                return true;
            }

            case R.id.action_go_to_top: {
                ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), 0);
                anim.setDuration(400);
                anim.start();
                return true;
            }

            case R.id.action_share_link: {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
                sharingIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share_dotdotdot)));
                return true;
            }

            case R.id.action_take_screenshot: {
                makeScreenshotOfWebView(false);
                return true;
            }

            case R.id.action_share_screenshot: {
                makeScreenshotOfWebView(true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        webViewState = new Bundle();
        webView.saveState(webViewState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(webViewState != null) {
            webView.restoreState(webViewState);
        }
    }

    @Override
    public boolean onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public ContextMenuWebView getWebView() {
        AppLog.d(this, "getWebView: "+(this.webView != null));
        return this.webView;
    }
}

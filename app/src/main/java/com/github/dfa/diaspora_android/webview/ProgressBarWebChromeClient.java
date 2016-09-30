package com.github.dfa.diaspora_android.webview;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * WebChromeClient that connects the ProgressBar and the WebView and updates the progress of the progressBar.
 * Created by vanitas on 26.09.16.
 */

public class ProgressBarWebChromeClient extends WebChromeClient {
    protected final ProgressBar progressBar;
    protected final WebView webView;

    public ProgressBarWebChromeClient(WebView webView, ProgressBar progressBar) {
        this.webView = webView;
        this.progressBar = progressBar;
    }

    public void onProgressChanged(WebView wv, int progress) {
        progressBar.setProgress(progress);
        progressBar.setVisibility(progress == 100 ? View.GONE : View.VISIBLE);
    }
}

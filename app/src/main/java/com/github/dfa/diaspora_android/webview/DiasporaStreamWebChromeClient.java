package com.github.dfa.diaspora_android.webview;

import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.WebHelper;

/**
 * Created by vanitas on 26.09.16.
 */

public class DiasporaStreamWebChromeClient extends FileUploadWebChromeClient {
    protected SharedTextCallback sharedTextCallback;

    public DiasporaStreamWebChromeClient(WebView webView, ProgressBar progressBar, FileUploadCallback fileUploadCallback, SharedTextCallback callback) {
        super(webView, progressBar, fileUploadCallback);
        this.sharedTextCallback = callback;
    }

    @Override
    public void onProgressChanged(WebView wv, int progress) {
        super.onProgressChanged(wv, progress);
        if (progress > 0 && progress <= 60) {
            WebHelper.getUserProfile(wv);
            WebHelper.optimizeMobileSiteLayout(wv);
        }

        if (progress > 60) {
            WebHelper.optimizeMobileSiteLayout(wv);

            String textToBeShared = sharedTextCallback.getSharedText();
            if (textToBeShared != null) {
                AppLog.d(this, "Share text into webView");
                WebHelper.shareTextIntoWebView(wv, textToBeShared);
            }
        }
    }

    public interface SharedTextCallback {
        String getSharedText();
        void setSharedText(String shared);
    }
}

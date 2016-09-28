package com.github.dfa.diaspora_android.ui;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * Created by vanitas on 26.09.16.
 */

public class FileUploadWebChromeClient extends ProgressBarWebChromeClient {
    protected FileUploadCallback fileUploadCallback;

    public FileUploadWebChromeClient(WebView webView, ProgressBar progressBar, FileUploadCallback fileUploadCallback) {
        super(webView, progressBar);
        this.fileUploadCallback = fileUploadCallback;
    }

    @Override
    public void onProgressChanged(WebView wv, int progress) {
        super.onProgressChanged(wv, progress);
    }

    //For Android 4.1/4.2 only. DO NOT REMOVE!
    @SuppressWarnings("unused")
    protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
    {
        fileUploadCallback.legacyImageUpload(uploadMsg, acceptType, capture);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        return fileUploadCallback.imageUpload(webView, filePathCallback, fileChooserParams);
    }

    public interface FileUploadCallback {
        boolean imageUpload(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams);
        void legacyImageUpload(ValueCallback<Uri> uploadMsg, String acceptType, String capture);
    }
}

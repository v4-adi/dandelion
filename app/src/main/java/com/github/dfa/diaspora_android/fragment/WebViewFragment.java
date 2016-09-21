package com.github.dfa.diaspora_android.fragment;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.data.PodUserProfile;
import com.github.dfa.diaspora_android.ui.ContextMenuWebView;
import com.github.dfa.diaspora_android.ui.CustomWebViewClient;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.WebHelper;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.webkit.WebkitProxy;

/**
 * Fragment that contains a WebView with a bunch of functionality
 * Created by vanitas on 21.09.16.
 */

public abstract class WebViewFragment extends CustomFragment {

    protected WebSettings webSettings;
    protected WebViewClient webViewClient;
    protected ContextMenuWebView webView;
    protected ProgressBar progressBar;
    protected AppSettings appSettings;

    protected String textToBeShared;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void setup(ContextMenuWebView webView, final ProgressBar progressBar, AppSettings appSettings) {
        this.appSettings = appSettings;
        this.webSettings = webView.getSettings();
        this.webView = webView;
        this.progressBar = progressBar;

        if (appSettings.isProxyEnabled()) {
            if (!setProxy(appSettings.getProxyHost(), appSettings.getProxyPort())) {
                AppLog.e(this, "Could not enable Proxy");
                Toast.makeText(getContext(), R.string.toast_set_proxy_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (appSettings.wasProxyEnabled()) {
            resetProxy();
        }

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMinimumFontSize(appSettings.getMinimumFontSize());
        webSettings.setLoadsImagesAutomatically(appSettings.isLoadImages());
        webSettings.setAppCacheEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            WebView.enableSlowWholeDocumentDraw();
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        this.registerForContextMenu(webView);
        //webView.setParentActivity(this);
        webView.setOverScrollMode(WebView.OVER_SCROLL_ALWAYS);

        // Setup WebView
        webView.addJavascriptInterface(new JavaScriptInterface(), "AndroidBridge");

        //Set proxy
        if (appSettings.isProxyEnabled()) {
            if (!setProxy(appSettings.getProxyHost(), appSettings.getProxyPort())) {
                AppLog.d(this, "Could not enable Proxy");
                Toast.makeText(getContext(), R.string.toast_set_proxy_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (appSettings.wasProxyEnabled()) {
            resetProxy();
        }

        /*
         * WebViewClient
         */
        this.webViewClient = new CustomWebViewClient((App) getActivity().getApplication(), webView);
        webView.setWebViewClient(webViewClient);

        /*
         * WebChromeClient
         */
        webView.setWebChromeClient(new WebChromeClient() {
            final ProgressBar pb = progressBar;

            public void onProgressChanged(WebView wv, int progress) {
                pb.setProgress(progress);

                if (progress > 0 && progress <= 60) {
                    WebHelper.getUserProfile(wv);
                    WebHelper.optimizeMobileSiteLayout(wv);
                }

                if (progress > 60) {
                    WebHelper.optimizeMobileSiteLayout(wv);

                    if (textToBeShared != null) {
                        WebHelper.shareTextIntoWebView(wv, textToBeShared);
                    }
                }

                progressBar.setVisibility(progress == 100 ? View.GONE : View.VISIBLE);
            }

            //For Android 4.1/4.2 only. DO NOT REMOVE!
            @SuppressWarnings("unused")
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                AppLog.v(this, "openFileChooser(ValCallback<Uri>, String, String");
                //imageUploadFilePathCallbackOld = uploadMsg;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("return-data", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                AppLog.v(this, "startActivityForResult");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), MainActivity.INPUT_FILE_REQUEST_CODE_OLD);
            }

            /*
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if(Build.VERSION.SDK_INT >= 23) {
                    int hasWRITE_EXTERNAL_STORAGE = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(getContext())
                                    .setMessage(R.string.permissions_image)
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (android.os.Build.VERSION.SDK_INT >= 23)
                                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    })
                                    .show();
                            return false;
                        }
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
                        return false;
                    }
                }
                AppLog.d(this, "onOpenFileChooser");
                if (MainActivity.imageUploadFilePathCallbackNew != null) imageUploadFilePathCallbackNew.onReceiveValue(null);
                imageUploadFilePathCallbackNew = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile;
                    try {
                        photoFile = Helpers.createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                       AppLog.e(this, "ERROR creating temp file: "+ ex.toString());
                        // Error occurred while creating the File
                        Snackbar.make(contentLayout, R.string.unable_to_load_image, Snackbar.LENGTH_LONG).show();
                        return false;
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                Log.d(App.TAG,"startActivityForResult");
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE_NEW);
                return true;
            }
            */
        });

    }

    /**
     * Set proxy according to arguments. host must not be "" or null, port must be positive.
     * Return true on success and update appSettings' proxy related values.
     *
     * @param host proxy host (eg. localhost or 127.0.0.1)
     * @param port proxy port (eg. 8118)
     * @return success
     * @throws IllegalArgumentException if arguments do not fit specifications above
     */
    private boolean setProxy(final String host, final int port) {
        AppLog.i(this, "StreamFragment.setProxy()");
        if (host != null && !host.equals("") && port >= 0) {
            AppLog.i(this, "Set proxy to "+host+":"+port);
            //Temporary change thread policy
            AppLog.v(this, "Set temporary ThreadPolicy");
            StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            StrictMode.ThreadPolicy tmp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(tmp);

            AppLog.v(this, "Apply NetCipher proxy settings");
            NetCipher.setProxy(host, port); //Proxy for HttpsUrlConnections
            try {
                //Proxy for the webview
                AppLog.v(this, "Apply Webkit proxy settings");
                WebkitProxy.setProxy(MainActivity.class.getName(), getContext().getApplicationContext(), null, host, port);
            } catch (Exception e) {
                AppLog.e(this, "Could not apply WebKit proxy settings:\n"+e.toString());
            }
            AppLog.v(this, "Save changes in appSettings");
            appSettings.setProxyEnabled(true);
            appSettings.setProxyWasEnabled(true);

            AppLog.v(this, "Reset old ThreadPolicy");
            StrictMode.setThreadPolicy(old);
            AppLog.i(this, "Success! Reload WebView");
            webView.reload();
            return true;
        } else {
            AppLog.e(this, "Invalid proxy configuration. Host: "+host+" Port: "+port+"\nRefuse to set proxy");
            return false;
        }
    }

    private boolean setProxy() {
        return setProxy(appSettings.getProxyHost(), appSettings.getProxyPort());
    }

    private void resetProxy() {
        AppLog.i(this, "StreamFragment.resetProxy()");
        AppLog.v(this, "write changes to appSettings");
        appSettings.setProxyEnabled(false);
        appSettings.setProxyWasEnabled(false);

        //Temporary change thread policy
        AppLog.v(this, "Set temporary ThreadPolicy");
        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.ThreadPolicy tmp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tmp);

        AppLog.v(this, "clear NetCipher proxy");
        NetCipher.clearProxy();
        try {
            AppLog.v(this, "clear WebKit proxy");
            WebkitProxy.resetProxy(MainActivity.class.getName(), getContext());
        } catch (Exception e) {
           AppLog.e(this, "Could not clear WebKit proxy:\n"+e.toString());
        }
        AppLog.v(this, "Reset old ThreadPolicy");
        StrictMode.setThreadPolicy(old);

        //Restart app
        AppLog.i(this, "Success! Restart app due to proxy reset");
        Intent restartActivity = new Intent(getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 12374, restartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void setUserProfile(final String webMessage) throws JSONException {
            PodUserProfile pup = ((App)getActivity().getApplication()).getPodUserProfile();
            AppLog.i(this, "StreamFragment.JavaScriptInterface.setUserProfile()");
            if (pup.isRefreshNeeded()) {
                AppLog.v(this, "PodUserProfile needs refresh; Try to parse JSON");
                pup.parseJson(webMessage);
            } else {
                AppLog.v(this, "No PodUserProfile refresh needed");
            }
        }

        @JavascriptInterface
        public void contentHasBeenShared() {
            textToBeShared = null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected boolean makeScreenshotOfWebView(boolean hasToShareScreenshot) {
        AppLog.i(this, "StreamFragment.makeScreenshotOfWebView()");
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasWRITE_EXTERNAL_STORAGE = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.permissions_screenshot)
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23)
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            })
                            .show();
                    return false;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }

        Date dateNow = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd--HH_mm_ss", Locale.getDefault());
        File fileSaveDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Diaspora");

        String fileSaveName = hasToShareScreenshot ? ".DfA_share.jpg" : String.format("DfA_%s.jpg", dateFormat.format(dateNow));
        if (!fileSaveDirectory.exists()) {
            if(!fileSaveDirectory.mkdirs()) {
                AppLog.w(this, "Could not mkdir "+fileSaveDirectory.getAbsolutePath());
            }
        }

        if (!hasToShareScreenshot) {
            Snackbar.make(webView, getString(R.string.share__toast_screenshot) + " " + fileSaveName, Snackbar.LENGTH_LONG).show();
        }

        Bitmap bitmap;
        webView.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(webView.getDrawingCache());
        webView.setDrawingCacheEnabled(false);

        OutputStream bitmapWriter = null;
        try {
            bitmapWriter = new FileOutputStream(new File(fileSaveDirectory, fileSaveName));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bitmapWriter);
            bitmapWriter.flush();
            bitmap.recycle();
        } catch (Exception e) {
            return false;
        } finally {
            if (bitmapWriter != null) {
                try {
                    bitmapWriter.close();
                } catch (IOException _ignSaveored) {/* Nothing */}
            }
        }

        // Only show share intent when Action Share Screenshot was selected
        if (hasToShareScreenshot) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/jpeg");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
            sharingIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
            Uri bmpUri = Uri.fromFile(new File(fileSaveDirectory, fileSaveName));
            sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.action_share_dotdotdot)));
        } else {
            // Broadcast that this file is indexable
            File file = new File(fileSaveDirectory, fileSaveName);
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
            getActivity().sendBroadcast(intent);
        }
        return true;
    }

    public void loadUrl(String url) {
        getWebView().loadUrlNew(url);
    }

    public String getUrl() {
        return getWebView().getUrl();
    }

    public void reloadUrl() {
        getWebView().reload();
    }

    public ContextMenuWebView getWebView() {
        return this.webView;
    }
}

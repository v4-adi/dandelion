package com.github.dfa.diaspora_android.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.data.PodUserProfile;
import com.github.dfa.diaspora_android.ui.ContextMenuWebView;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.DiasporaUrlHelper;
import com.github.dfa.diaspora_android.util.Helpers;
import com.github.dfa.diaspora_android.util.WebHelper;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Fragment that contains a WebView displaying the stream of the user
 * Created by vanitas on 21.09.16.
 */

public class StreamFragment extends WebViewFragment {
    public static final String TAG = "com.github.dfa.diaspora_android.StreamFragment";

    private DiasporaUrlHelper urls;

    private ValueCallback<Uri[]> imageUploadFilePathCallbackNew;
    private ValueCallback<Uri> imageUploadFilePathCallbackOld;
    private String mCameraPhotoPath;
    protected String textToBeShared;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d(this, "onCreateView()");
        return inflater.inflate(R.layout.stream__fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        AppLog.d(this, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        this.webView = (ContextMenuWebView) view.findViewById(R.id.webView);
        this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        this.appSettings = ((App) getActivity().getApplication()).getSettings();
        this.urls = new DiasporaUrlHelper(appSettings);

        this.setup(
                webView,
                progressBar,
                appSettings);

        // Setup WebView
        webView.addJavascriptInterface(new JavaScriptInterface(), "AndroidBridge");

        if(webView.getUrl() == null) {
            loadUrl(urls.getPodUrl());
        }

        //Set WebChromeClient
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
                        AppLog.d(this, "Share text into webView");
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
                if (imageUploadFilePathCallbackNew != null) imageUploadFilePathCallbackNew.onReceiveValue(null);
                imageUploadFilePathCallbackNew = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile;
                    try {
                        photoFile = Helpers.createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        AppLog.e(this, "ERROR creating temp file: "+ ex.toString());
                        // Error occurred while creating the File
                        Snackbar.make(webView, R.string.unable_to_load_image, Snackbar.LENGTH_LONG).show();
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
                AppLog.d(this, "startActivityForResult");
                startActivityForResult(chooserIntent, MainActivity.INPUT_FILE_REQUEST_CODE_NEW);
                return true;
            }
        });

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.d(this, "onActivityResult(): "+requestCode);
        switch (requestCode) {
            case MainActivity.INPUT_FILE_REQUEST_CODE_NEW:
            case MainActivity.INPUT_FILE_REQUEST_CODE_OLD:
                AppLog.d(this, "INPUT_FILE_REQUEST_CODE: "+requestCode);
                onImageUploadResult(requestCode, resultCode, data);
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public ContextMenuWebView getWebView() {
        AppLog.d(this, "getWebView: "+(this.webView != null));
        return this.webView;
    }

    public void setTextToBeShared(String text) {
        this.textToBeShared = text;
    }

    public void onImageUploadResult(int requestCode, int resultCode, Intent data) {
        AppLog.d(this, "onImageUploadResult");
        switch (requestCode) {
            case MainActivity.INPUT_FILE_REQUEST_CODE_NEW: {
                AppLog.v(this, "Upload image using recent method (Lollipop+)");
                if (imageUploadFilePathCallbackNew == null || resultCode != Activity.RESULT_OK) {
                    AppLog.e(this, "Callback is null: " + (imageUploadFilePathCallbackNew == null)
                            + " resultCode: " + resultCode);
                    if (imageUploadFilePathCallbackNew != null)
                        imageUploadFilePathCallbackNew.onReceiveValue(new Uri[]{});
                    return;
                }
                Uri[] results = null;
                if (data == null) {
                    if (mCameraPhotoPath != null) {
                        AppLog.v(this, "Intent data is null. Try to parse cameraPhotoPath");
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    } else {
                        AppLog.w(this, "Intent data is null and cameraPhotoPath is null");
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        AppLog.v(this, "Intent has data. Try to parse dataString");
                        results = new Uri[]{Uri.parse(dataString)};
                    } else {
                        AppLog.w(this, "dataString is null");
                    }
                }
                AppLog.v(this, "handle received result over to callback");
                imageUploadFilePathCallbackNew.onReceiveValue(results);
                imageUploadFilePathCallbackNew = null;
                return;
            }
            case MainActivity.INPUT_FILE_REQUEST_CODE_OLD: {
                AppLog.v(this, "Upload image using legacy method (Jelly Bean, Kitkat)");
                if (imageUploadFilePathCallbackOld == null || resultCode != Activity.RESULT_OK) {
                    AppLog.e(this, "Callback is null: " + (imageUploadFilePathCallbackOld == null)
                            + " resultCode: " + resultCode);
                    if (imageUploadFilePathCallbackOld != null)
                        imageUploadFilePathCallbackOld.onReceiveValue(null);
                    return;
                }
                Uri results = null;
                if (data == null) {
                    if (mCameraPhotoPath != null) {
                        AppLog.v(this, "Intent has no data. Try to parse cameraPhotoPath");
                        results = Uri.parse(mCameraPhotoPath);
                    } else {
                        AppLog.w(this, "Intent has no data and cameraPhotoPath is null");
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        AppLog.v(this, "Intent has data. Try to parse dataString");
                        results = Uri.parse(dataString);
                    } else {
                        AppLog.w(this, "dataString is null");
                    }
                }
                AppLog.v(this, "handle received result over to callback");
                imageUploadFilePathCallbackOld.onReceiveValue(results);
                imageUploadFilePathCallbackOld = null;
            }
        }
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void setUserProfile(final String webMessage) throws JSONException {
            PodUserProfile pup = ((App)getActivity().getApplication()).getPodUserProfile();
            AppLog.v(this, "StreamFragment.JavaScriptInterface.setUserProfile()");
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
}

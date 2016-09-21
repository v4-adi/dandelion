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
package com.github.dfa.diaspora_android.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.data.PodUserProfile;
import com.github.dfa.diaspora_android.listener.WebUserProfileChangedListener;
import com.github.dfa.diaspora_android.receivers.OpenExternalLinkReceiver;
import com.github.dfa.diaspora_android.receivers.UpdateTitleReceiver;
import com.github.dfa.diaspora_android.ui.BadgeDrawable;
import com.github.dfa.diaspora_android.ui.ContextMenuWebView;
import com.github.dfa.diaspora_android.ui.CustomWebViewClient;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.CustomTabHelpers.CustomTabActivityHelper;
import com.github.dfa.diaspora_android.util.DiasporaUrlHelper;
import com.github.dfa.diaspora_android.util.Helpers;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.webkit.WebkitProxy;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WebUserProfileChangedListener {


    private static final int INPUT_FILE_REQUEST_CODE_NEW = 1;
    private static final int INPUT_FILE_REQUEST_CODE_OLD = 2;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public static final int REQUEST_CODE__ACCESS_EXTERNAL_STORAGE = 124;

    public static final String ACTION_OPEN_URL = "com.github.dfa.diaspora_android.MainActivity.open_url";
    public static final String ACTION_OPEN_EXTERNAL_URL = "com.github.dfa.diaspora_android.MainActivity.open_external_url";
    public static final String ACTION_CHANGE_ACCOUNT = "com.github.dfa.diaspora_android.MainActivity.change_account";
    public static final String ACTION_CLEAR_CACHE = "com.github.dfa.diaspora_android.MainActivity.clear_cache";
    public static final String ACTION_UPDATE_TITLE_FROM_URL = "com.github.dfa.diaspora_android.MainActivity.set_title";
    public static final String ACTION_RELOAD_ACTIVITY = "com.github.dfa.diaspora_android.MainActivity.reload_activity";
    public static final String URL_MESSAGE = "URL_MESSAGE";
    public static final String EXTRA_URL = "com.github.dfa.diaspora_android.extra_url";
    public static final String CONTENT_HASHTAG = "content://com.github.dfa.diaspora_android.mainactivity/";

    private App app;
    private ValueCallback<Uri[]> imageUploadFilePathCallbackNew;
    private ValueCallback<Uri> imageUploadFilePathCallbackOld;
    private String mCameraPhotoPath;
    private CustomTabActivityHelper customTabActivityHelper;
    private WebSettings webSettings;
    private AppSettings appSettings;
    private DiasporaUrlHelper urls;
    private PodUserProfile podUserProfile;
    private final Handler uiHandler = new Handler();
    private CustomWebViewClient webViewClient;
    private OpenExternalLinkReceiver brOpenExternalLink;
    private BroadcastReceiver brSetTitle;
    private Snackbar snackbarExitApp;
    private Snackbar snackbarNoInternet;
    private String textToBeShared = null;

    /**
     * UI Bindings
     */
    @BindView(R.id.content_layout)
    RelativeLayout contentLayout;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.toolbar)
    Toolbar toolbarTop;

    @BindView(R.id.toolbar2)
    ActionMenuView toolbarBottom;

    @BindView(R.id.placeholder_webview)
    FrameLayout webviewPlaceholder;

    ContextMenuWebView webView;

    @BindView(R.id.main__navigaion_view)
    NavigationView navView;

    @BindView(R.id.main__layout)
    DrawerLayout navDrawer;


    // NavHeader cannot be bound by Butterknife
    private TextView navheaderTitle;
    private TextView navheaderDescription;
    private ImageView navheaderImage;


    /**
     * END  UI Bindings
     */

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.v(this, "onCreate()");

        // Bind UI
        setContentView(R.layout.main__activity);

        if ((app = (App) getApplication()) == null) AppLog.e(this, "App is null!");
        if ((appSettings = app.getSettings()) == null) AppLog.e(this, "AppSettings is null!");
        if ((podUserProfile = app.getPodUserProfile()) == null)
            AppLog.e(this, "PodUserProfile is null!");
        podUserProfile.setCallbackHandler(uiHandler);
        podUserProfile.setListener(this);
        urls = new DiasporaUrlHelper(appSettings);
        customTabActivityHelper = new CustomTabActivityHelper();

        setupUI(savedInstanceState);

        if (appSettings.isProxyEnabled()) {
            if (!setProxy(appSettings.getProxyHost(), appSettings.getProxyPort())) {
                AppLog.e(this, "Could not enable Proxy");
                Toast.makeText(MainActivity.this, R.string.toast_set_proxy_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (appSettings.wasProxyEnabled()) {
            resetProxy();
        }

        brOpenExternalLink = new OpenExternalLinkReceiver(this);
        brSetTitle = new UpdateTitleReceiver(app, urls, new UpdateTitleReceiver.TitleCallback() {
            @Override
            public void setTitle(int rId) {
                MainActivity.this.setTitle(rId);
            }

            @Override
            public void setTitle(String title) {
                MainActivity.this.setTitle(title);
            }
        });
    }

    private void setupUI(Bundle savedInstanceState) {
        AppLog.i(this, "setupUI()");
        ButterKnife.bind(this);
        if (webviewPlaceholder.getChildCount() != 0) {
            AppLog.v(this, "remove child views from webViewPlaceholder");
            webviewPlaceholder.removeAllViews();
        } else {
            AppLog.v(this, "webViewPlaceholder had no child views");
        }

        boolean newWebView = (webView == null);
        if (newWebView) {
            AppLog.v(this, "WebView was null. Create new one.");
            View webviewHolder = getLayoutInflater().inflate(R.layout.ui__webview, this.contentLayout, false);
            this.webView = (ContextMenuWebView) webviewHolder.findViewById(R.id.webView);
            ((LinearLayout) webView.getParent()).removeView(webView);
            setupWebView(savedInstanceState);
        } else {
            AppLog.v(this, "Reuse old WebView to avoid reloading page");
        }

        AppLog.v(this, "Add WebView to placeholder");
        webviewPlaceholder.addView(webView);
        // Setup toolbar
        setSupportActionBar(toolbarTop);
        getMenuInflater().inflate(R.menu.main__menu_bottom, toolbarBottom.getMenu());
        toolbarBottom.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return MainActivity.this.onOptionsItemSelected(item);
            }
        });
        setTitle(R.string.app_name);

        //Setup snackbar
        snackbarExitApp = Snackbar
                .make(contentLayout, R.string.confirm_exit, Snackbar.LENGTH_LONG)
                .setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        moveTaskToBack(true);
                    }
                });
        snackbarNoInternet = Snackbar.make(contentLayout, R.string.no_internet, Snackbar.LENGTH_LONG);

        // Load app settings
        setupNavigationSlider();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        String url = urls.getPodUrl();
        if (newWebView) {
            if (WebHelper.isOnline(MainActivity.this)) {
                AppLog.v(this, "setupUI: reload url");
                webView.loadData("", "text/html", null);
                webView.loadUrlNew(url);
            } else {
                snackbarNoInternet.show();
            }
        }

        if (!appSettings.isIntellihideToolbars()) {
            AppLog.v(this, "Disable intelligent hiding of toolbars");
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarTop.getLayoutParams();
            params.setScrollFlags(0);  // clear all scroll flags
        }

        AppLog.v(this, "UI successfully set up");
        handleIntent(getIntent());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        AppLog.i(this, "onConfigurationChanged()");
        if (webView != null) {
            // Remove the WebView from the old placeholder
            AppLog.v(this, "removeView from placeholder in order to prevent recreation");
            webviewPlaceholder.removeView(webView);
        }

        super.onConfigurationChanged(newConfig);

        // Load the layout resource for the new configuration
        setContentView(R.layout.main__activity);

        // Reinitialize the UI
        AppLog.v(this, "Rebuild the UI");
        setupUI(null);
    }

    private void setupWebView(Bundle savedInstanceState) {

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMinimumFontSize(appSettings.getMinimumFontSize());
        webSettings.setLoadsImagesAutomatically(appSettings.isLoadImages());
        webSettings.setAppCacheEnabled(true);

        if (savedInstanceState != null) {
            AppLog.v(this, "restore WebView state");
            webView.restoreState(savedInstanceState);
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            WebView.enableSlowWholeDocumentDraw();
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        this.registerForContextMenu(webView);
        webView.setParentActivity(this);
        webView.setOverScrollMode(WebView.OVER_SCROLL_ALWAYS);

        // Setup WebView
        webView.addJavascriptInterface(new JavaScriptInterface(), "AndroidBridge");

        //Set proxy
        if (appSettings.isProxyEnabled()) {
            if (!setProxy())
                Toast.makeText(this, R.string.toast_set_proxy_failed, Toast.LENGTH_LONG).show();
        }

        /*
         * WebViewClient
         */
        webViewClient = new CustomWebViewClient(app, webView);
        webView.setWebViewClient(webViewClient);

        /*
         * WebChromeClient
         */
        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView wv, int progress) {
                progressBar.setProgress(progress);

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
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                AppLog.v(this, "openFileChooser(ValCallback<Uri>, String, String");
                imageUploadFilePathCallbackOld = uploadMsg;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("return-data", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                AppLog.v(this, "startActivityForResult");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), INPUT_FILE_REQUEST_CODE_OLD);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(R.string.permissions_image)
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (android.os.Build.VERSION.SDK_INT >= 23)
                                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    })
                                    .show();
                            return false;
                        }
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return false;
                    }
                }

                AppLog.v(MainActivity.this, "onOpenFileChooser");
                imageUploadFilePathCallbackNew = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile;
                    try {
                        photoFile = Helpers.createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        AppLog.e(this, "ERROR creating temp file: " + ex.toString());
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

                AppLog.v(this, "startActivityForResult");
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE_NEW);
                return true;
            }
        });
    }

    private void setupNavigationSlider() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navDrawer, toolbarTop, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();

        //NavigationView navView = ButterKnife.findById(this, R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        View navHeader = navView.getHeaderView(0);
        LinearLayout navheaderProfileSection = ButterKnife.findById(navHeader, R.id.nav_profile_picture);
        navheaderProfileSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navDrawer.closeDrawer(GravityCompat.START);
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getProfileUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
        });
        navheaderTitle = ButterKnife.findById(navHeader, R.id.navheader_title);
        navheaderDescription = ButterKnife.findById(navHeader, R.id.podselection__podupti_notice);
        navheaderImage = ButterKnife.findById(navHeader, R.id.navheader_user_image);

        if (!appSettings.getName().equals("")) {
            navheaderTitle.setText(appSettings.getName());
        }
        if (!appSettings.getPodDomain().equals("")) {
            navheaderDescription.setText(appSettings.getPodDomain());
        }
        String avatarUrl = appSettings.getAvatarUrl();
        if (!avatarUrl.equals("")) {
            //Display app launcher icon instead of default avatar asset
            //(Which would by the way not load because of missing pod domain prefix in the url)
            if (avatarUrl.startsWith("/assets/user/default")) {
                AppLog.v(this, "Avatar appears to be an asset. Display launcher icon instead (avatarUrl=" + avatarUrl + ")");
                navheaderImage.setImageResource(R.drawable.ic_launcher);
            } else {
                // Try to load image
                if (!app.getAvatarImageLoader().loadToImageView(navheaderImage)) {
                    // If not yet loaded, start download
                    AppLog.v(this, "Avatar not cached. Start download: " + avatarUrl);
                    app.getAvatarImageLoader().startImageDownload(navheaderImage, avatarUrl);
                }
            }
        }

        // Set visibility
        Menu navMenu = navView.getMenu();
        navMenu.findItem(R.id.nav_exit).setVisible(appSettings.isVisibleInNavExit());
        navMenu.findItem(R.id.nav_activities).setVisible(appSettings.isVisibleInNavActivities());
        navMenu.findItem(R.id.nav_aspects).setVisible(appSettings.isVisibleInNavAspects());
        navMenu.findItem(R.id.nav_commented).setVisible(appSettings.isVisibleInNavCommented());
        navMenu.findItem(R.id.nav_followed_tags).setVisible(appSettings.isVisibleInNavFollowed_tags());
        navMenu.findItem(R.id.nav_help_license).setVisible(appSettings.isVisibleInNavHelp_license());
        navMenu.findItem(R.id.nav_liked).setVisible(appSettings.isVisibleInNavLiked());
        navMenu.findItem(R.id.nav_mentions).setVisible(appSettings.isVisibleInNavMentions());
        navMenu.findItem(R.id.nav_profile).setVisible(appSettings.isVisibleInNavProfile());
        navMenu.findItem(R.id.nav_public).setVisible(appSettings.isVisibleInNavPublic_activities());
    }

    @OnClick(R.id.toolbar)
    public void onToolBarClicked(View view) {
        AppLog.i(this, "onToolBarClicked()");
        onNavigationItemSelected(navView.getMenu().findItem(R.id.nav_stream));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        AppLog.i(this, "handleIntent()");
        if (intent == null) {
            AppLog.v(this, "Intent was null");
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();
        String loadUrl = null;
        AppLog.v(this, "Action: " + action + " Type: " + type);
        if (ACTION_OPEN_URL.equals(action)) {
            loadUrl = intent.getStringExtra(URL_MESSAGE);
        } else if (Intent.ACTION_VIEW.equals(action) && intent.getDataString() != null) {
            Uri data = intent.getData();
            if (data != null && data.toString().startsWith(CONTENT_HASHTAG)) {
                handleHashtag(intent);
                return;
            } else {
                loadUrl = intent.getDataString();
            }
        } else if (ACTION_CHANGE_ACCOUNT.equals(action)) {
            AppLog.v(this, "Reset pod data and animate to PodSelectionActivity");
            app.resetPodData(webView);
            Helpers.animateToActivity(MainActivity.this, PodSelectionActivity.class, true);
        } else if (ACTION_CLEAR_CACHE.equals(action)) {
            AppLog.v(this, "Clear WebView cache");
            webView.clearCache(true);
        } else if (ACTION_RELOAD_ACTIVITY.equals(action)) {
            AppLog.v(this, "Recreate activity");
            recreate();
            return;
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {
            switch (type) {
                case "text/plain":
                    if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
                        handleSendSubject(intent);
                    } else {
                        handleSendText(intent);
                    }
                    break;
                case "image/*":
                    handleSendImage(intent); //TODO: Add intent filter to Manifest and implement method
                    break;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            //TODO: Implement and add filter to manifest
        }

        if (loadUrl != null) {
            webView.stopLoading();
            navDrawer.closeDrawers();
            webView.loadUrlNew(loadUrl);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.v(this, "onActivityResult()");
        switch (requestCode) {
            case INPUT_FILE_REQUEST_CODE_NEW: {
                AppLog.v(this, "Upload image using recent method (Lollipop+)");
                if (imageUploadFilePathCallbackNew == null || resultCode != Activity.RESULT_OK) {
                    AppLog.e(this, "Callback is null: " + (imageUploadFilePathCallbackNew == null)
                            + " resultCode: " + resultCode);
                    if(imageUploadFilePathCallbackNew != null)
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
                    }
                    AppLog.w(this, "dataString is null");
                }
                AppLog.v(this, "handle received result over to callback");
                imageUploadFilePathCallbackNew.onReceiveValue(results);
                imageUploadFilePathCallbackNew = null;
                return;
            }
            case INPUT_FILE_REQUEST_CODE_OLD: {
                AppLog.v(this, "Upload image using legacy method (Jelly Bean, Kitkat)");
                if (imageUploadFilePathCallbackOld == null || resultCode != Activity.RESULT_OK) {
                    AppLog.e(this, "Callback is null: " + (imageUploadFilePathCallbackOld == null)
                            + " resultCode: " + resultCode);
                    if(imageUploadFilePathCallbackOld != null)
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
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        AppLog.v(this, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        AppLog.v(this, "Save WebView state");
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        AppLog.v(this, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
        AppLog.v(this, "Restore state of WebView");
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        AppLog.v(this, "onBackPressed()");
        if (navDrawer.isDrawerOpen(navView)) {
            navDrawer.closeDrawer(navView);
            return;
        }

        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }

        if (!snackbarExitApp.isShown()) {
            snackbarExitApp.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onPause() {
        AppLog.v(this, "onPause()");
        AppLog.v(this, "Unregister BroadcastReceivers");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brSetTitle);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brOpenExternalLink);
        super.onPause();
    }

    @Override
    protected void onResume() {
        AppLog.v(this, "onResume()");
        super.onResume();
        AppLog.v(this, "Register BroadcastReceivers");
        LocalBroadcastManager.getInstance(this).registerReceiver(brSetTitle, new IntentFilter(ACTION_UPDATE_TITLE_FROM_URL));
        LocalBroadcastManager.getInstance(this).registerReceiver(brOpenExternalLink, new IntentFilter(ACTION_OPEN_EXTERNAL_URL));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AppLog.v(this, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.main__menu_top, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;

        if ((item = menu.findItem(R.id.action_notifications)) != null) {
            LayerDrawable icon = (LayerDrawable) item.getIcon();
            BadgeDrawable.setBadgeCount(this, icon, podUserProfile.getNotificationCount());
        }

        if ((item = menu.findItem(R.id.action_conversations)) != null) {
            LayerDrawable icon = (LayerDrawable) item.getIcon();
            BadgeDrawable.setBadgeCount(this, icon, podUserProfile.getUnreadMessagesCount());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.i(this, "onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.action_notifications: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getNotificationsUrl());
                    return true;
                } else {
                    snackbarNoInternet.show();
                    return false;
                }
            }

            case R.id.action_conversations: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getConversationsUrl());
                    return true;
                } else {
                    snackbarNoInternet.show();
                    return false;
                }
            }

            case R.id.action_reload: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.reload();
                    return true;
                } else {
                    snackbarNoInternet.show();
                    return false;
                }
            }

            case R.id.action_exit: {
                moveTaskToBack(true);
                finish();
                return true;
            }

            case R.id.action_toggle_desktop_page: {
                webView.loadUrlNew(urls.getToggleMobileUrl());
                return true;
            }

            case R.id.action_compose: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getNewPostUrl());
                } else {
                    snackbarNoInternet.show();
                }
                return true;
            }

            case R.id.action_go_to_top: {
                // Scroll to top (animated)
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

            case R.id.action_search: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    View layout = getLayoutInflater().inflate(R.layout.ui__dialog_search__people_tags, null, false);
                    final EditText input = (EditText) layout.findViewById(R.id.dialog_search__input);
                    final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            String query = input.getText().toString().trim().replaceAll((which == DialogInterface.BUTTON_NEGATIVE ? "\\*" : "\\#"), "");
                            if (query.equals("")) {
                                Snackbar.make(contentLayout, R.string.search_alert_bypeople_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                            } else {
                                webView.loadUrl(which == DialogInterface.BUTTON_NEGATIVE ? urls.getSearchPeopleUrl(query) : urls.getSearchTagsUrl(query));
                            }
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    };

                    final android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(this)
                            .setView(layout).setTitle(R.string.search_alert_title)
                            .setCancelable(true)
                            .setPositiveButton(R.string.search_alert_tag, clickListener)
                            .setNegativeButton(R.string.search_alert_people, clickListener)
                            .create();

                    input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                dialog.hide();
                                clickListener.onClick(null, 0);
                                return true;
                            }
                            return false;
                        }
                    });

                    // Popup keyboard
                    dialog.show();
                    input.requestFocus();
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                } else {
                    snackbarNoInternet.show();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean makeScreenshotOfWebView(boolean hasToShareScreenshot) {
        AppLog.i(this, "makeScreenshotOfWebView()");
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.permissions_screenshot)
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23)
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            })
                            .show();
                    return false;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }

        Date dateNow = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd--HH_mm_ss", Locale.getDefault());
        File fileSaveDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Diaspora");

        String fileSaveName = hasToShareScreenshot ? ".DfA_share.jpg" : String.format("DfA_%s.jpg", dateFormat.format(dateNow));
        if (!fileSaveDirectory.exists()) {
            if (!fileSaveDirectory.mkdirs()) {
                AppLog.w(this, "Could not mkdir " + fileSaveDirectory.getAbsolutePath());
            }
        }

        if (!hasToShareScreenshot) {
            Snackbar.make(contentLayout, getString(R.string.share__toast_screenshot) + " " + fileSaveName, Snackbar.LENGTH_LONG).show();
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
            sendBroadcast(intent);
        }
        return true;
    }

    @Override
    public void onUserProfileNameChanged(String name) {
        AppLog.i(this, "onUserProfileNameChanged()");
        navheaderTitle.setText(name);
    }

    @Override
    public void onUserProfileAvatarChanged(String avatarUrl) {
        AppLog.i(this, "onUserProfileAvatarChanged()");
        app.getAvatarImageLoader().startImageDownload(navheaderImage, avatarUrl);
    }

    private void handleHashtag(Intent intent) {
        AppLog.v(this, "handleHashtag()");
        try {
            setSharedTexts(null, intent.getData().toString().split("/")[3]);
        } catch (Exception e) {
            AppLog.e(this, e.toString());
        }
        webView.loadUrlNew(urls.getNewPostUrl());
    }

    private void handleSendText(Intent intent) {
        AppLog.v(this, "handleSendText()");
        try {
            setSharedTexts(null, intent.getStringExtra(Intent.EXTRA_TEXT));
        } catch (Exception e) {
            AppLog.e(this, e.toString());
        }
        webView.loadUrlNew(urls.getBlankUrl());
        webView.loadUrlNew(urls.getNewPostUrl());
    }

    /**
     * Handle sent text + subject
     *
     * @param intent intent
     */
    private void handleSendSubject(Intent intent) {
        AppLog.v(this, "handleSendSubject()");
        try {
            setSharedTexts(intent.getStringExtra(Intent.EXTRA_SUBJECT), intent.getStringExtra(Intent.EXTRA_TEXT));
        } catch (Exception e) {
            AppLog.e(this, e.toString());
        }
        webView.loadUrlNew(urls.getBlankUrl()); //TODO: Necessary?
        webView.loadUrlNew(urls.getNewPostUrl());
    }

    /**
     * Set sharedText variable to escaped and formatted subject + body.
     * If subject is null, only the body will be set. Else the subject will be set as header.
     * Depending on whether the user has the setting isAppendSharedViaApp set, a reference to
     * the app will be added at the bottom
     *
     * @param sharedSubject post subject or null
     * @param sharedBody    post text
     */
    private void setSharedTexts(String sharedSubject, String sharedBody) {
        AppLog.i(this, "setSharedTexts()");
        String body = WebHelper.replaceUrlWithMarkdown(sharedBody);
        if (appSettings.isAppendSharedViaApp()) {
            AppLog.v(this, "Append app reference to shared text");
            body = body + "\n\n" + getString(R.string.shared_by_diaspora_android);
        }
        final String escapedBody = WebHelper.escapeHtmlText(body);
        if (sharedSubject != null) {
            AppLog.v(this, "Append subject to shared text");
            String escapedSubject = WebHelper.escapeHtmlText(WebHelper.replaceUrlWithMarkdown(sharedSubject));
            textToBeShared = "**" + escapedSubject + "** " + escapedBody;
        } else {
            AppLog.v(this, "Set shared text; Subject: \"" + sharedSubject + "\" Body: \"" + sharedBody + "\"");
            textToBeShared = escapedBody;
        }


    }

    //TODO: Implement?
    private void handleSendImage(Intent intent) {
        AppLog.i(this, "handleSendImage()");
        final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            AppLog.v(this, "imageUri is not null. Handle shared image");
            // TODO: Update UI to reflect text being shared
        } else {
            AppLog.w(this, "imageUri is null. Cannot precede.");
        }
        Toast.makeText(this, "Not yet implemented.", Toast.LENGTH_SHORT).show();
    }

    // TODO: Move from Javascript interface
    @Override
    public void onNotificationCountChanged(int notificationCount) {
        AppLog.i(this, "onNotificationCountChanged()");
        // Count saved in PodUserProfile
        invalidateOptionsMenu();
    }

    // TODO: Move from Javascript interface
    @Override
    public void onUnreadMessageCountChanged(int unreadMessageCount) {
        AppLog.i(this, "onUnreadMessageCountChanged()");
        // Count saved in PodUserProfile
        invalidateOptionsMenu();
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void setUserProfile(final String webMessage) throws JSONException {
            AppLog.spam(this, "JavaScriptInterface.setUserProfile()");
            if (podUserProfile.isRefreshNeeded()) {
                AppLog.spam(this, "PodUserProfile needs refresh; Try to parse JSON");
                podUserProfile.parseJson(webMessage);
            } else {
                AppLog.spam(this, "No PodUserProfile refresh needed");
            }
        }

        @JavascriptInterface
        public void contentHasBeenShared() {
            textToBeShared = null;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        AppLog.v(this, "onNavigationItemsSelected()");
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_stream: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getStreamUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_profile: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getProfileUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_followed_tags: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    WebHelper.showFollowedTagsList(webView, app);
                    setTitle(R.string.nav_followed_tags);
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_aspects: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(DiasporaUrlHelper.URL_BLANK);
                    WebHelper.showAspectList(webView, app);
                    setTitle(R.string.aspects);
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_activities: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getActivityUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_liked: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getLikedPostsUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_commented: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getCommentedUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_mentions: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getMentionsUrl());
                } else {
                    snackbarNoInternet.show();
                }
                break;
            }

            case R.id.nav_public: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    webView.loadUrlNew(urls.getPublicUrl());
                } else {
                    snackbarNoInternet.show();
                }
                break;
            }

            case R.id.nav_exit: {
                moveTaskToBack(true);
                finish();
                break;
            }

            case R.id.nav_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            break;

            case R.id.nav_help_license: {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
            break;
        }

        navDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE__ACCESS_EXTERNAL_STORAGE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AppLog.i(this, "onRequestPermissionsResult: Permission to access external storage granted");
                    Toast.makeText(this, R.string.permission_granted_try_again, Toast.LENGTH_SHORT).show();
                } else {
                    AppLog.w(this, "onRequestPermissionsResult: Permission to access external storage denied");
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                return;

            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
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
        AppLog.v(this, "setProxy()");
        if (host != null && !host.equals("") && port >= 0) {
            AppLog.v(this, "Set proxy to " + host + ":" + port);
            //Temporary change thread policy
            AppLog.v(this, "Set temporary ThreadPolicy");
            StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            StrictMode.ThreadPolicy tmp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(tmp);

            AppLog.v(this, "Apply NetCipher proxy settings");
            NetCipher.setProxy(host, port); //Proxy for HttpsUrlConnections
            try {
                //Proxy for the ui__webview
                AppLog.v(this, "Apply Webkit proxy settings");
                WebkitProxy.setProxy(MainActivity.class.getName(), getApplicationContext(), null, host, port);
            } catch (Exception e) {
                AppLog.e(this, "Could not apply WebKit proxy settings:\n" + e.toString());
            }
            AppLog.v(this, "Save changes in appSettings");
            appSettings.setProxyEnabled(true);
            appSettings.setProxyWasEnabled(true);

            AppLog.v(this, "Reset old ThreadPolicy");
            StrictMode.setThreadPolicy(old);
            AppLog.v(this, "Success! Reload WebView");
            webView.reload();
            return true;
        } else {
            AppLog.w(this, "Invalid proxy configuration. Host: " + host + " Port: " + port + "\nRefuse to set proxy");
            return false;
        }
    }

    private boolean setProxy() {
        return setProxy(appSettings.getProxyHost(), appSettings.getProxyPort());
    }

    private void resetProxy() {
        AppLog.i(this, "resetProxy()");
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
            WebkitProxy.resetProxy(MainActivity.class.getName(), this);
        } catch (Exception e) {
            AppLog.e(this, "Could not clear WebKit proxy:\n" + e.toString());
        }
        AppLog.v(this, "Reset old ThreadPolicy");
        StrictMode.setThreadPolicy(old);

        //Restart app
        AppLog.i(this, "Success! Restart app due to proxy reset");
        Intent restartActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 12374, restartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }
}

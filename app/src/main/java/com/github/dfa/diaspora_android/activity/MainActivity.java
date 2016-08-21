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
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
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
import com.github.dfa.diaspora_android.ui.ContextMenuWebView;
import com.github.dfa.diaspora_android.ui.CustomWebViewClient;
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
import info.guardianproject.netcipher.web.WebkitProxy;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WebUserProfileChangedListener {


    static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public static final int REQUEST_CODE_ASK_PERMISSIONS_SAVE_IMAGE = 124;

    public static final String ACTION_OPEN_URL = "com.github.dfa.diaspora_android.MainActivity.open_url";
    public static final String ACTION_CHANGE_ACCOUNT = "com.github.dfa.diaspora_android.MainActivity.change_account";
    public static final String ACTION_CLEAR_CACHE = "com.github.dfa.diaspora_android.MainActivity.clear_cache";
    public static final String ACTION_UPDATE_TITLE_FROM_URL = "com.github.dfa.diaspora_android.MainActivity.set_title";
    public static final String ACTION_RELOAD_ACTIVITY = "com.github.dfa.diaspora_android.MainActivity.reload_activity";
    public static final String URL_MESSAGE = "URL_MESSAGE";
    public static final String EXTRA_URL = "com.github.dfa.diaspora_android.extra_url";

    private App app;
    private String podDomain;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private WebSettings webSettings;
    private AppSettings appSettings;
    private DiasporaUrlHelper urls;
    private PodUserProfile podUserProfile;
    private final Handler uiHandler = new Handler();
    private CustomWebViewClient webViewClient;
    private Snackbar snackbarExitApp;
    private Snackbar snackbarNewNotification;
    private Snackbar snackbarNoInternet;
    public String textToBeShared = null;

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

    @BindView(R.id.webView)
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

        // Bind UI
        setContentView(R.layout.main__activity);
        ButterKnife.bind(this);

        app = (App) getApplication();
        appSettings = app.getSettings();
        podUserProfile = app.getPodUserProfile();
        podUserProfile.setCallbackHandler(uiHandler);
        podUserProfile.setListener(this);
        urls = new DiasporaUrlHelper(appSettings);

        if (appSettings.isProxyEnabled()) {
            if (!setProxy(appSettings.getProxyHost(), appSettings.getProxyPort())) {
                Toast.makeText(MainActivity.this, R.string.toast_set_proxy_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (appSettings.wasProxyEnabled()) {
            resetProxy();
        }

        setupWebView(savedInstanceState);

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
        snackbarNewNotification = Snackbar
                .make(contentLayout, R.string.new_notifications, Snackbar.LENGTH_LONG)
                .setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (WebHelper.isOnline(MainActivity.this)) {
                            webView.loadUrlNew(urls.getNotificationsUrl());
                        } else {
                            Snackbar.make(contentLayout, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
        snackbarNoInternet = Snackbar.make(contentLayout, R.string.no_internet, Snackbar.LENGTH_LONG);

        // Load app settings
        setupNavigationSlider();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        podDomain = appSettings.getPodDomain();

        String url = urls.getPodUrl();
        if (savedInstanceState == null) {
            if (WebHelper.isOnline(MainActivity.this)) {
                webView.loadData("", "text/html", null);
                webView.loadUrlNew(url);
            } else {
                snackbarNoInternet.show();
            }
        }

        if (!appSettings.isIntellihideToolbars()) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarTop.getLayoutParams();
            params.setScrollFlags(0);  // clear all scroll flags
        }

        handleIntent(getIntent());
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

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);

                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
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

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
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
        if (!appSettings.getAvatarUrl().equals("")) {
            Log.d(App.TAG, "AVATAR URL != \"\": "+appSettings.getAvatarUrl());
            //Display app launcher icon instead of default avatar asset
            //(Which would by the way not load because of missing pod domain prefix in the url)
            if(appSettings.getAvatarUrl().startsWith("/assets/user/default")) {
                navheaderImage.setImageResource(R.drawable.ic_launcher);
            } else {
                // Try to load image
                if (!app.getAvatarImageLoader().loadToImageView(navheaderImage)) {
                    // If not yet loaded, start download
                    app.getAvatarImageLoader().startImageDownload(navheaderImage, appSettings.getAvatarUrl());
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
        onNavigationItemSelected(navView.getMenu().findItem(R.id.nav_stream));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("dd-MM-yy_HH-mm", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();
        String loadUrl = null;


        if (ACTION_OPEN_URL.equals(action)) {
            loadUrl = intent.getStringExtra(URL_MESSAGE);
        } else if (Intent.ACTION_VIEW.equals(action) && intent.getDataString() != null) {
            loadUrl = intent.getDataString();
        } else if (ACTION_CHANGE_ACCOUNT.equals(action)) {
            app.resetPodData(webView);
            Helpers.animateToActivity(MainActivity.this, PodSelectionActivity.class, true);
        } else if (ACTION_CLEAR_CACHE.equals(action)) {
            webView.clearCache(true);
        } else if (ACTION_RELOAD_ACTIVITY.equals(action)) {
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
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                if (mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(brSetTitle, new IntentFilter(ACTION_UPDATE_TITLE_FROM_URL));
    }

    @Override
    public void onBackPressed() {
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

    private final BroadcastReceiver brSetTitle = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra(EXTRA_URL);
            if (url != null && url.startsWith(urls.getPodUrl())) {
                String subUrl = url.substring((urls.getPodUrl()).length());
                if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_STREAM)) {
                    setTitle(R.string.nav_stream);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_POSTS)) {
                    setTitle(R.string.diaspora); //TODO: Extract posts title somehow?
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_NOTIFICATIONS)) {
                    setTitle(R.string.notifications);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_CONVERSATIONS)) {
                    setTitle(R.string.conversations);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_NEW_POST)) {
                    setTitle(R.string.new_post);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_PEOPLE + appSettings.getProfileId())) {
                    setTitle(R.string.nav_profile);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_ACTIVITY)) {
                    setTitle(R.string.nav_activities);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_LIKED)) {
                    setTitle(R.string.nav_liked);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_COMMENTED)) {
                    setTitle(R.string.nav_commented);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_MENTIONS)) {
                    setTitle(R.string.nav_mentions);
                } else if (subUrl.startsWith(DiasporaUrlHelper.SUBURL_PUBLIC)) {
                    setTitle(R.string.public_);
                } else if (urls.isAspectUrl(url)){
                    setTitle(urls.getAspectNameFromUrl(url, app));
                }
            }
        }
    };

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brSetTitle);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main__menu_top, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemNotification = menu.findItem(R.id.action_notifications);
        if (itemNotification != null) {
            if (podUserProfile.getNotificationCount() > 0) {
                itemNotification.setIcon(R.drawable.ic_notifications_colored_48px);
            } else {
                itemNotification.setIcon(R.drawable.ic_notifications_white_48px);
            }

            MenuItem itemConversation = menu.findItem(R.id.action_conversations);
            if (podUserProfile.getUnreadMessagesCount() > 0) {
                itemConversation.setIcon(R.drawable.ic_email_colored_48px);
            } else {
                itemConversation.setIcon(R.drawable.ic_mail_white_48px);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                    LinearLayout layout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    final EditText input = new EditText(this);
                    input.setSingleLine(true);
                    layout.setPadding(50, 0, 50, 0);
                    input.setHint(R.string.app_hashtag);
                    layout.addView(input);

                    final DialogInterface.OnClickListener onSearchAccepted = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean wasClickedOnSearchForPeople = which == DialogInterface.BUTTON_NEGATIVE;

                            String inputTag = input.getText().toString().trim();
                            String cleanTag = inputTag.replaceAll(wasClickedOnSearchForPeople ? "\\*" : "\\#", "");
                            // this validate the input data for tagfind
                            if (cleanTag == null || cleanTag.equals("")) {
                                Snackbar.make(contentLayout, R.string.search_alert_bypeople_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                            } else { // User have added a search tag
                                if (wasClickedOnSearchForPeople) {
                                    webView.loadUrlNew(urls.getSearchPeopleUrl(cleanTag));
                                } else {
                                    webView.loadUrlNew(urls.getSearchTagsUrl(cleanTag));
                                }
                            }

                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    };

                    final AlertDialog dialog = new AlertDialog.Builder(this)
                            .setView(layout)
                            .setTitle(R.string.search_alert_title)
                            .setCancelable(true)
                            .setPositiveButton(R.string.search_alert_tag, onSearchAccepted)
                            .setNegativeButton(R.string.search_alert_people, onSearchAccepted)
                            .create();

                    input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                dialog.hide();
                                onSearchAccepted.onClick(null, 0);
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
            fileSaveDirectory.mkdirs();
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
        navheaderTitle.setText(name);
    }

    @Override
    public void onUserProfileAvatarChanged(String avatarUrl) {
        app.getAvatarImageLoader().startImageDownload(navheaderImage, avatarUrl);
    }

    void handleSendText(Intent intent) {
        String content = WebHelper.replaceUrlWithMarkdown(intent.getStringExtra(Intent.EXTRA_TEXT));
        if (appSettings.isAppendSharedViaApp()) {
            // &#10; = \n
            content = content + "\n\n" + getString(R.string.shared_by_diaspora_android);
        }

        final String sharedText = WebHelper.escapeHtmlText(content);
        if (sharedText != null) {
            textToBeShared = sharedText;
        }

        webView.loadUrlNew(urls.getBlankUrl());
        webView.loadUrlNew(urls.getNewPostUrl());
    }

    /**
     * Handle sent text + subject
     *
     * @param intent
     */
    void handleSendSubject(Intent intent) {
        webView.loadUrlNew(urls.getNewPostUrl());
        String content = WebHelper.replaceUrlWithMarkdown(intent.getStringExtra(Intent.EXTRA_TEXT));
        String subject = WebHelper.replaceUrlWithMarkdown(intent.getStringExtra(Intent.EXTRA_SUBJECT));

        if (appSettings.isAppendSharedViaApp()) {
            // &#10; = \n
            content = content + "\n\n" + getString(R.string.shared_by_diaspora_android);
        }

        final String sharedSubject = WebHelper.escapeHtmlText(subject);
        final String sharedContent = WebHelper.escapeHtmlText(content);
        textToBeShared = "**" + sharedSubject + "** " + sharedContent;

        webView.loadUrlNew(urls.getBlankUrl());
        webView.loadUrlNew(urls.getNewPostUrl());
    }

    //TODO: Implement?
    private void handleSendImage(Intent intent) {
        final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect text being shared
        }
        Toast.makeText(this, "Not yet implemented.", Toast.LENGTH_SHORT).show();
    }

    // TODO: Move from Javascript interface
    @Override
    public void onNotificationCountChanged(int notificationCount) {
        // Count saved in PodUserProfile
        invalidateOptionsMenu();

        if (notificationCount > 0 && !snackbarNewNotification.isShown()
                && !webView.getUrl().equals(urls.getNotificationsUrl())) {
            snackbarNewNotification.show();
        }
    }

    // TODO: Move from Javascript interface
    @Override
    public void onUnreadMessageCountChanged(int unreadMessageCount) {
        // Count saved in PodUserProfile
        invalidateOptionsMenu();

        if (unreadMessageCount > 0 && !snackbarNewNotification.isShown()
                && !webView.getUrl().equals(urls.getNotificationsUrl())) {
            snackbarNewNotification.show();
        }
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void setUserProfile(final String webMessage) throws JSONException {
            if (podUserProfile.isRefreshNeeded()) {
                podUserProfile.parseJson(webMessage);
            }
        }

        @JavascriptInterface
        public void contentHasBeenShared() {
            textToBeShared = null;
        }

        @JavascriptInterface
        public void log(final String log) {
            //Log.d(App.TAG, "[wv] " + log);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

            case R.id.nav_settings_app: {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            break;

            case R.id.nav_help_license: {
                final CharSequence[] options = {getString(R.string.help_license__name), getString(R.string.help_markdown__name)};
                new AlertDialog.Builder(MainActivity.this)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals(getString(R.string.help_license__name))) {
                                    final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.help_license__content)));
                                    Linkify.addLinks(s, Linkify.WEB_URLS);
                                    final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.help_license__years)
                                            .setMessage(s)
                                            .setPositiveButton(android.R.string.yes, null).show();
                                    d.show();
                                    ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                }
                                if (options[item].equals(getString(R.string.help_markdown__name))) {
                                    Helpers.loadUrlInExternalBrowser(MainActivity.this, getString(R.string.help_markdown__weblink));
                                }
                            }
                        }).show();
            }
            break;
        }

        navDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS_SAVE_IMAGE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_granted_try_again, Toast.LENGTH_SHORT).show();
                } else {
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
        if (host != null && !host.equals("") && port >= 0) {
            //Temporary change thread policy
            StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            StrictMode.ThreadPolicy tmp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(tmp);

            NetCipher.setProxy(host, port); //Proxy for HttpsUrlConnections
            try {
                //Proxy for the webview
                WebkitProxy.setProxy(MainActivity.class.getName(), getApplicationContext(), null, host, port);
            } catch (Exception e) { /*Nothing we can do*/ }

            appSettings.setProxyEnabled(true);
            appSettings.setProxyWasEnabled(true);

            StrictMode.setThreadPolicy(old);
            webView.reload();
            return true;
        } else {
            return false;
        }
    }

    private boolean setProxy() {
        return setProxy(appSettings.getProxyHost(), appSettings.getProxyPort());
    }

    private void resetProxy() {
        appSettings.setProxyEnabled(false);
        appSettings.setProxyWasEnabled(false);

        //Temporary change thread policy
        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.ThreadPolicy tmp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tmp);

        NetCipher.clearProxy();
        try {
            WebkitProxy.resetProxy(MainActivity.class.getName(), this);
        } catch (Exception e) {/*Nothing*/}

        StrictMode.setThreadPolicy(old);

        //Restart app
        Intent restartActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 12374, restartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }
}

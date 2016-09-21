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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.data.PodUserProfile;
import com.github.dfa.diaspora_android.fragment.CustomFragment;
import com.github.dfa.diaspora_android.fragment.StreamFragment;
import com.github.dfa.diaspora_android.listener.WebUserProfileChangedListener;
import com.github.dfa.diaspora_android.receiver.OpenExternalLinkReceiver;
import com.github.dfa.diaspora_android.receiver.UpdateTitleReceiver;
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


    public static final int INPUT_FILE_REQUEST_CODE_NEW = 1;
    public static final int INPUT_FILE_REQUEST_CODE_OLD = 2;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
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
    private AppSettings appSettings;
    private DiasporaUrlHelper urls;
    private PodUserProfile podUserProfile;
    private final Handler uiHandler = new Handler();
    private OpenExternalLinkReceiver brOpenExternalLink;
    private BroadcastReceiver brSetTitle;
    private Snackbar snackbarExitApp;
    private Snackbar snackbarNoInternet;

    private FragmentManager fm;

    /**
     * UI Bindings
     */
    @BindView(R.id.main__topbar)
    Toolbar toolbarTop;

    @BindView(R.id.main__bottombar)
    ActionMenuView toolbarBottom;

    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;

    @BindView(R.id.main__navigaion_view)
    NavigationView navView;

    @BindView(R.id.main__navdrawer)
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
        ButterKnife.bind(this);

        if ((app = (App) getApplication()) == null) AppLog.e(this, "App is null!");
        if ((appSettings = app.getSettings()) == null) AppLog.e(this, "AppSettings is null!");
        if ((podUserProfile = app.getPodUserProfile()) == null)
            AppLog.e(this, "PodUserProfile is null!");
        podUserProfile.setCallbackHandler(uiHandler);
        podUserProfile.setListener(this);
        urls = new DiasporaUrlHelper(appSettings);
        customTabActivityHelper = new CustomTabActivityHelper();

        fm = getSupportFragmentManager();
        StreamFragment sf = getStreamFragment();
        fm.beginTransaction().replace(R.id.fragment_container, sf, StreamFragment.TAG).commit();
        sf.onCreateBottomOptionsMenu(toolbarBottom.getMenu(), getMenuInflater());

        setupUI(savedInstanceState);

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
                .make(fragmentContainer, R.string.confirm_exit, Snackbar.LENGTH_LONG)
                .setAction(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        moveTaskToBack(true);
                    }
                });
        snackbarNoInternet = Snackbar.make(fragmentContainer, R.string.no_internet, Snackbar.LENGTH_LONG);

        // Load app settings
        setupNavigationSlider();

        if (!appSettings.isIntellihideToolbars()) {
            AppLog.v(this, "Disable intelligent hiding of toolbars");
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarTop.getLayoutParams();
            params.setScrollFlags(0);  // clear all scroll flags
        }

        AppLog.v(this, "UI successfully set up");
        handleIntent(getIntent());
    }

    public void openDiasporaUrl(String url) {
        StreamFragment streamFragment = getStreamFragment();
        if(!streamFragment.isVisible()) {
            AppLog.d(this, "StreamFragment not visible");
            fm.beginTransaction().replace(R.id.fragment_container, streamFragment, StreamFragment.TAG).commit();
            streamFragment.onCreateBottomOptionsMenu(toolbarBottom.getMenu(), getMenuInflater());
        }
        streamFragment.loadUrl(url);
    }

    public StreamFragment getStreamFragment() {
        StreamFragment streamFragment = (StreamFragment) fm.findFragmentByTag(StreamFragment.TAG);
        if(streamFragment == null) {
            AppLog.d(this, "StreamFragment was null");
            streamFragment = new StreamFragment();
        }
        return streamFragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        AppLog.i(this, "onConfigurationChanged()");

        super.onConfigurationChanged(newConfig);
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
        //Handle clicks on profile picture
        navheaderProfileSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navDrawer.closeDrawer(GravityCompat.START);
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getProfileUrl());
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

    @OnClick(R.id.main__topbar)
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
            app.resetPodData(getStreamFragment().getWebView());
            Helpers.animateToActivity(MainActivity.this, PodSelectionActivity.class, true);
        } else if (ACTION_CLEAR_CACHE.equals(action)) {
            AppLog.v(this, "Clear WebView cache");
            getStreamFragment().getWebView().clearCache(true);
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
            navDrawer.closeDrawers();
            openDiasporaUrl(loadUrl);
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
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        AppLog.v(this, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private Fragment getTopFragment() {
        for(Fragment f : fm.getFragments()) {
            if(f.isVisible()) {
                return f;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        AppLog.v(this, "onBackPressed()");
        if (navDrawer.isDrawerOpen(navView)) {
            navDrawer.closeDrawer(navView);
            return;
        }
        CustomFragment top = (CustomFragment) getTopFragment();
        if(top != null) {
            if(!top.onBackPressed()) {
                //TODO: Go back in Fragment backstack
                return;
            } else {
                return;
            }
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
                    openDiasporaUrl(urls.getNotificationsUrl());
                    return true;
                } else {
                    snackbarNoInternet.show();
                    return false;
                }
            }

            case R.id.action_conversations: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getConversationsUrl());
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

            case R.id.action_compose: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getNewPostUrl());
                } else {
                    snackbarNoInternet.show();
                }
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
                                Snackbar.make(fragmentContainer, R.string.search_alert_bypeople_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                            } else {
                                openDiasporaUrl(which == DialogInterface.BUTTON_NEGATIVE ? urls.getSearchPeopleUrl(query) : urls.getSearchTagsUrl(query));
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
        openDiasporaUrl(urls.getNewPostUrl());
    }

    private void handleSendText(Intent intent) {
        AppLog.v(this, "handleSendText()");
        try {
            setSharedTexts(null, intent.getStringExtra(Intent.EXTRA_TEXT));
        } catch (Exception e) {
            AppLog.e(this, e.toString());
        }
        openDiasporaUrl(urls.getBlankUrl());
        openDiasporaUrl(urls.getNewPostUrl());
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
        openDiasporaUrl(urls.getBlankUrl()); //TODO: Necessary?
        openDiasporaUrl(urls.getNewPostUrl());
    }

    /**
     * TODO: MOVE
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
            //textToBeShared = "**" + escapedSubject + "** " + escapedBody;
        } else {
            AppLog.v(this, "Set shared text; Subject: \"" + sharedSubject + "\" Body: \"" + sharedBody + "\"");
            //textToBeShared = escapedBody;
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        AppLog.v(this, "onNavigationItemsSelected()");
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_stream: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getStreamUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_profile: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getProfileUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            //TODO: Replace with fragment
            case R.id.nav_followed_tags: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getBlankUrl());
                    WebHelper.showFollowedTagsList(getStreamFragment().getWebView(), app);
                    setTitle(R.string.nav_followed_tags);
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            //TODO: Replace with fragment
            case R.id.nav_aspects: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(DiasporaUrlHelper.URL_BLANK);
                    WebHelper.showAspectList(getStreamFragment().getWebView(), app);
                    setTitle(R.string.aspects);
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_activities: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getActivityUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_liked: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getLikedPostsUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_commented: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getCommentedUrl());
                } else {
                    snackbarNoInternet.show();
                }
            }
            break;

            case R.id.nav_mentions: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getMentionsUrl());
                } else {
                    snackbarNoInternet.show();
                }
                break;
            }

            case R.id.nav_public: {
                if (WebHelper.isOnline(MainActivity.this)) {
                    openDiasporaUrl(urls.getPublicUrl());
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
}

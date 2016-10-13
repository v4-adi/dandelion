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

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.ui.IntellihideToolbarActivityListener;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.DiasporaUrlHelper;
import com.github.dfa.diaspora_android.util.ProxyHandler;
import com.github.dfa.diaspora_android.util.theming.ColorPalette;
import com.github.dfa.diaspora_android.util.theming.ThemeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

/**
 * @author vanitas
 */
public class SettingsActivity extends ThemedActivity implements IntellihideToolbarActivityListener {
    @BindView(R.id.settings__appbar)
    protected AppBarLayout appBarLayout;

    @BindView(R.id.settings__toolbar)
    protected Toolbar toolbar;

    private ProxyHandler.ProxySettings oldProxySettings;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings__activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.onBackPressed();
            }
        });
        oldProxySettings = getAppSettings().getProxySettings();
        getFragmentManager().beginTransaction().replace(R.id.settings__fragment_container, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    protected void applyColorToViews() {
        ThemeHelper.updateToolbarColor(toolbar);
    }

    @Override
    public void enableToolbarHiding() {
        AppLog.d(this, "Enable Intellihide");
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        //scroll|enterAlways|snap
        params.setScrollFlags(toolbarDefaultScrollFlags);
        appBarLayout.setExpanded(true, true);
    }

    @Override
    public void disableToolbarHiding() {
        AppLog.d(this, "Disable Intellihide");
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);  // clear all scroll flags
        appBarLayout.setExpanded(true, true);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences sharedPreferences;

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences);
            sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            setPreferenceSummaries();
            sharedPreferences.edit().putBoolean(getString(R.string.pref_key__proxy_was_enabled),
                    sharedPreferences.getBoolean(getString(R.string.pref_key__http_proxy_enabled), false)).apply();
        }

        private void setPreferenceSummaries() {
            String[] editTextKeys = new String[]{
                    getString(R.string.pref_key__http_proxy_host), getString(R.string.pref_key__http_proxy_port)
            };
            for (String key : editTextKeys) {
                EditTextPreference p = (EditTextPreference) findPreference(key);
                p.setSummary(p.getText());
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key));
            if (key.equals(getString(R.string.pref_key__intellihide_toolbars))) {
                if (sharedPreferences.getBoolean(getString(R.string.pref_key__intellihide_toolbars), false)) {
                    ((SettingsActivity) getActivity()).enableToolbarHiding();
                } else {
                    ((SettingsActivity) getActivity()).disableToolbarHiding();
                }
            }
        }

        private void updatePreference(Preference preference) {
            if (preference == null) {
                return;
            }
            if (preference instanceof EditTextPreference) {
                EditTextPreference textPref = (EditTextPreference) preference;
                textPref.setSummary(textPref.getText());
                return;
            }
            if (preference instanceof ListPreference) {
                ListPreference listPref = (ListPreference) preference;
                listPref.setSummary(listPref.getEntry());
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            App app = ((App) getActivity().getApplication());
            AppSettings appSettings = app.getSettings();
            if (Build.VERSION.SDK_INT >= 21) {
                if (preference instanceof PreferenceScreen && ((PreferenceScreen) preference).getDialog() != null) {
                    Window window = ((PreferenceScreen) preference).getDialog().getWindow();
                    if (window != null) {
                        window.setStatusBarColor(ThemeHelper.getPrimaryDarkColor());
                    }
                }
            }

            Intent intent = new Intent(getActivity(), MainActivity.class);
            DiasporaUrlHelper diasporaUrlHelper = new DiasporaUrlHelper(app.getSettings());

            switch (preference.getTitleRes()) {
                case R.string.pref_title__primary_color: {
                    showColorPickerDialog(1);
                    intent = null;
                    break;
                }
                case R.string.pref_title__accent_color: {
                    showColorPickerDialog(2);
                    intent = null;
                    break;
                }
                case R.string.pref_title__personal_settings: {
                    intent.setAction(MainActivity.ACTION_OPEN_URL);
                    intent.putExtra(MainActivity.URL_MESSAGE, diasporaUrlHelper.getPersonalSettingsUrl());
                    break;
                }
                case R.string.pref_title__manage_tags: {
                    intent.setAction(MainActivity.ACTION_OPEN_URL);
                    intent.putExtra(MainActivity.URL_MESSAGE, diasporaUrlHelper.getManageTagsUrl());
                    break;
                }
                case R.string.pref_title__manage_contacts: {
                    intent.setAction(MainActivity.ACTION_OPEN_URL);
                    intent.putExtra(MainActivity.URL_MESSAGE, diasporaUrlHelper.getManageContactsUrl());
                    break;
                }
                case R.string.pref_title__change_account: {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.confirmation))
                            .setMessage(getString(R.string.pref_warning__change_account))
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                            intent.setAction(MainActivity.ACTION_CHANGE_ACCOUNT);
                                            startActivity(intent);
                                            getActivity().finish();
                                        }
                                    })
                            .show();
                    return true;
                }
                case R.string.pref_title__http_proxy_load_tor_preset: {
                    ((EditTextPreference) findPreference(getString(R.string.pref_key__http_proxy_host))).setText("127.0.0.1");
                    ((EditTextPreference) findPreference(getString(R.string.pref_key__http_proxy_port))).setText("8118");
                    return true;
                }

                case R.string.pref_title__clear_cache: {
                    intent.setAction(MainActivity.ACTION_CLEAR_CACHE);
                    break;
                }

                default: {
                    intent = null;
                    break;
                }
            }
            if (intent != null) {
                startActivity(intent);
                getActivity().finish();
                return true;
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        /**
         * Show a colorPicker Dialog
         *
         * @param type 1 -> Primary Color, 2 -> Accent Color
         */
        public void showColorPickerDialog(final int type) {
            final AppSettings appSettings = ((App) getActivity().getApplication()).getSettings();
            final Context context = getActivity();

            //Inflate dialog layout
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.color_picker__dialog, null);
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
            builder.setView(dialogLayout);

            final FrameLayout titleBackground = (FrameLayout) dialogLayout.findViewById(R.id.color_picker_dialog__title_background);
            final TextView title = (TextView) dialogLayout.findViewById(R.id.color_picker_dialog__title);
            final LineColorPicker base = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_dialog__base_picker);
            final LineColorPicker shade = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_dialog__shade_picker);

            title.setText(type == 1 ? R.string.pref_title__primary_color : R.string.pref_title__accent_color);
            title.setTextColor(getResources().getColor(R.color.white));
            final int[] current = (type == 1 ? appSettings.getPrimaryColorSettings() : appSettings.getAccentColorSettings());
            base.setColors((type == 1 ? ColorPalette.getBaseColors(context) : ColorPalette.getAccentColors(context)));
            base.setSelectedColor(current[0]);
            shade.setColors(ColorPalette.getColors(context, current[0]));
            shade.setSelectedColor(current[1]);
            titleBackground.setBackgroundColor(shade.getColor());
            base.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {
                    AppLog.d(this, "Selected Base color changed: " + i);
                    shade.setColors(ColorPalette.getColors(context, i));
                    titleBackground.setBackgroundColor(i);
                    if (i == current[0]) {
                        shade.setSelectedColor(current[1]);
                        titleBackground.setBackgroundColor(shade.getColor());
                    }
                }
            });
            shade.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {
                    titleBackground.setBackgroundColor(i);
                }
            });

            //Build dialog
            builder
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (type == 1) {
                                appSettings.setPrimaryColorSettings(base.getColor(), shade.getColor());
                                if (Build.VERSION.SDK_INT >= 21) {
                                    getActivity().getWindow().setStatusBarColor(ThemeHelper.getPrimaryDarkColor());
                                }
                                ((ThemedActivity) getActivity()).applyColorToViews();
                            } else {
                                appSettings.setAccentColorSettings(base.getColor(), shade.getColor());
                            }
                        }
                    }).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Reset logging
        AppSettings settings = new AppSettings(getApplicationContext());
        AppLog.setLoggingEnabled(settings.isLoggingEnabled());
        AppLog.setLoggingSpamEnabled(settings.isLoggingSpamEnabled());
    }

    @Override
    protected void onStop() {
        ProxyHandler.ProxySettings newProxySettings = getAppSettings().getProxySettings();
        if (!oldProxySettings.equals(newProxySettings)) {
            AppLog.d(this, "ProxySettings changed.");
            //Proxy on-off? => Restart app
            if (oldProxySettings.isEnabled() && !newProxySettings.isEnabled()) {
                Intent restartActivity = new Intent(SettingsActivity.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(SettingsActivity.this, 12374, restartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) SettingsActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                System.exit(0);
            } //Proxy changed? => Update
            else {
                ProxyHandler.getInstance().updateProxySettings(this);
            }
        }
        super.onStop();
    }
}

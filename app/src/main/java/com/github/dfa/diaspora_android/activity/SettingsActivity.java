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
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.ProxyHandler;
import com.github.dfa.diaspora_android.util.AppLog;

/**
 * @author vanitas
 */
public class SettingsActivity extends AppCompatActivity {
    private ProxyHandler.ProxySettings oldProxySettings;
    private AppSettings appSettings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setDisplayHomeAsUpEnabled(true);
        this.appSettings = new AppSettings(this);
        oldProxySettings = appSettings.getProxySettings();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
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
            Intent intent = new Intent(getActivity(), MainActivity.class);
            String podDomain = appSettings.getPodDomain();

            switch (preference.getTitleRes()) {
                case R.string.pref_title__personal_settings: {
                    intent.setAction(MainActivity.ACTION_OPEN_URL);
                    intent.putExtra(MainActivity.URL_MESSAGE, "https://" + podDomain + "/user/edit");
                    break;
                }
                case R.string.pref_title__manage_tags: {
                    intent.setAction(MainActivity.ACTION_OPEN_URL);
                    intent.putExtra(MainActivity.URL_MESSAGE, "https://" + podDomain + "/tag_followings/manage");
                    break;
                }
                case R.string.pref_title__manage_contacts: {
                    intent.setAction(MainActivity.ACTION_OPEN_URL);
                    intent.putExtra(MainActivity.URL_MESSAGE, "https://" + podDomain + "/contacts");
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

                case R.string.pref_title__clear_cache:
                {
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
        ProxyHandler.ProxySettings newProxySettings = appSettings.getProxySettings();
        if(!oldProxySettings.equals(newProxySettings)) {
            AppLog.d(this, "ProxySettings changed.");
            //Proxy on-off? => Restart app
            if(oldProxySettings.isEnabled() && !newProxySettings.isEnabled()) {
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

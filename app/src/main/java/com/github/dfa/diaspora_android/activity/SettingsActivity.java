package com.github.dfa.diaspora_android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;

/**
 * @author vanitas
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Intent settingsChangedIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setEditTextPreferenceSummaries();
        settingsChangedIntent = new Intent("SettingsChanged");
    }

    private void setEditTextPreferenceSummaries() {
        String[] prefKeys = new String[]{"pref_key_proxy_host", "pref_key_proxy_port"};
        for(String key : prefKeys) {
            EditTextPreference p = (EditTextPreference) findPreference(key);
            p.setSummary(p.getText());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key), key);
        settingsChangedIntent.putExtra(key, true);
        switch (key) {
            case AppSettings.PREF.MINIMUM_FONT_SIZE:
                int newFontSize = Integer.parseInt(((ListPreference)findPreference(key)).getValue().substring(1));
                Log.d(App.TAG, newFontSize+"");
        }
    }

    private void updatePreference(Preference preference, String key) {
        if (preference == null) return;
        if (preference instanceof EditTextPreference) {
            EditTextPreference textPref = (EditTextPreference) preference;
            textPref.setSummary(textPref.getText());
            return;
        }
        if(preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            listPref.setSummary(listPref.getEntry());
            return;
        }
    }

    @Override
    public void finish() {
        Log.d(App.TAG, "finish()"); //TODO: remove
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(settingsChangedIntent);
        super.finish();
    }
}
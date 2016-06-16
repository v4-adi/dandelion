package com.github.dfa.diaspora_android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;

/**
 * @author vanitas
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("app");
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setPreferenceSummaries();
    }

    private void setPreferenceSummaries() {
        String[] editTextKeys = new String[]{"pref_key_proxy_host", "pref_key_proxy_port"};
        for(String key : editTextKeys) {
            EditTextPreference p = (EditTextPreference) findPreference(key);
            p.setSummary(p.getText());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key), key);
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
}
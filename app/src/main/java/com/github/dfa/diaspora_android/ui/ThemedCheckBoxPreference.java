package com.github.dfa.diaspora_android.ui;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.theming.ThemeHelper;

/**
 * Created by vanitas on 24.10.16.
 */

public class ThemedCheckBoxPreference extends CheckBoxPreference implements Themeable {
    protected View rootLayout;
    @SuppressWarnings("unused")
    public ThemedCheckBoxPreference(Context context) {
        super(context);
    }
    @SuppressWarnings("unused")
    public ThemedCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @SuppressWarnings("unused")
    public ThemedCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        rootLayout = super.onCreateView(parent);
        setColors();
        return rootLayout;
    }

    @Override
    public void setColors() {
        CheckBox checkBox = (CheckBox) rootLayout.findViewById(android.R.id.checkbox);
        ThemeHelper.getInstance(new AppSettings(getContext()));
        ThemeHelper.updateCheckBoxColor(checkBox);
    }
}
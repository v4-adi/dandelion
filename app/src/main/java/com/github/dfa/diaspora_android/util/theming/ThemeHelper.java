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
    This class is inspired by org.horasapps.LeafPic
 */
package com.github.dfa.diaspora_android.util.theming;

import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.dfa.diaspora_android.data.AppSettings;

/**
 * Singleton that can be used to color views
 * Created by vanitas on 06.10.16.
 */

public class ThemeHelper {
    private AppSettings appSettings;
    private static ThemeHelper instance;

    private ThemeHelper(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public static ThemeHelper getInstance(AppSettings appSettings) {
        if(instance == null) {
            instance = new ThemeHelper(appSettings);
        }
        return instance;
    }

    public static ThemeHelper getInstance() {
        if(instance == null) throw new IllegalStateException("ThemeHelper must be initialized using getInstance(AppSettings) before it can be used!");
        return instance;
    }

    public static void updateEditTextColor(EditText editText) {
        if(editText != null) {
            editText.setHighlightColor(getInstance().appSettings.getAccentColor());
        }
    }

    public static void updateCheckBoxColor(CheckBox checkBox) {
        if(checkBox != null) {
            checkBox.setHighlightColor(getInstance().appSettings.getAccentColor());
        }
    }

    public static void updateTabLayoutColor(TabLayout tabLayout) {
        if(tabLayout != null) {
            tabLayout.setBackgroundColor(getInstance().appSettings.getPrimaryColor());
            tabLayout.setSelectedTabIndicatorColor(getInstance().appSettings.getAccentColor());
        }
    }

    public static void updateTextViewColor(TextView textView) {
        if(textView != null) {
            textView.setHighlightColor(getInstance().appSettings.getAccentColor());
            textView.setLinkTextColor(getInstance().appSettings.getAccentColor());
        }
    }

    public static void updateToolbarColor(Toolbar toolbar) {
        if(toolbar != null) {
            toolbar.setBackgroundColor(getInstance().appSettings.getPrimaryColor());
        }
    }

    public static void updateActionMenuViewColor(ActionMenuView actionMenuView) {
        if(actionMenuView != null) {
            actionMenuView.setBackgroundColor(getInstance().appSettings.getPrimaryColor());
        }
    }

    public static int getPrimaryColor() {
        return getInstance().appSettings.getPrimaryColor();
    }

    public static int getAccentColor() {
        return getInstance().appSettings.getAccentColor();
    }

    public static void setPrimaryColorAsBackground(View view) {
        if(view != null) {
            view.setBackgroundColor(getPrimaryColor());
        }
    }

    public static int getPrimaryDarkColor() {
        return ColorPalette.getObscuredColor(getPrimaryColor());
    }

    public static void updateActionBarColor(ActionBar actionBar) {
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getInstance().appSettings.getPrimaryColor()));
        }
    }

    public static void updateProgressBarColor(ProgressBar progressBar) {
        if(progressBar != null && progressBar.getProgressDrawable() != null) {
            progressBar.getProgressDrawable().setColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN);
        }
    }
}

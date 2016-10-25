package com.github.dfa.diaspora_android.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.util.Helpers;

/**
 * Preference that shows selected Color in a circle
 * Created by vanitas on 25.10.16.
 */

public class ThemedColorPickerPreference extends Preference implements Themeable {
    protected ImageView colorPreview;

    @SuppressWarnings("unused")
    public ThemedColorPickerPreference(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public ThemedColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public ThemedColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        colorPreview = (ImageView) view.findViewById(android.R.id.icon);
        setColors();
    }

    @Override
    public void setColors() {
        Drawable circle;
        if (colorPreview != null && (circle = colorPreview.getDrawable()) != null) {
            Context c = getContext();
            AppSettings appSettings = new AppSettings(getContext());
            String key = getKey();

            int color = Helpers.getColorFromRessource(c, R.color.primary);
            if ((appSettings.isKeyEqual(key, R.string.pref_key__primary_color_shade))) {
                color = appSettings.getPrimaryColor();
            } else if ((appSettings.isKeyEqual(key, R.string.pref_key__accent_color_shade))) {
                color = appSettings.getAccentColor();
            } else {
                color = appSettings.getColor(getSharedPreferences(), key, color);
            }
            circle.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }
}
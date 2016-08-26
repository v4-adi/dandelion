package com.github.dfa.diaspora_android.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Patterns;
import android.widget.TextView;

import com.github.dfa.diaspora_android.activity.MainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTextView extends TextView {

    public HtmlTextView(Context context) {
        super(context);
        init();
    }

    public HtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setText(new SpannableString(Html.fromHtml(getText().toString())));
        Linkify.TransformFilter filter = new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group();
            }
        };

        Pattern hashtagPattern = Pattern.compile("[#]+[A-Za-z0-9-_]+\\b");
        String hashtagScheme = MainActivity.CONTENT_HASHTAG;
        Linkify.addLinks(this, hashtagPattern, hashtagScheme, null, filter);

        Pattern urlPattern = Patterns.WEB_URL;
        Linkify.addLinks(this, urlPattern, null, null, filter);

    }
}
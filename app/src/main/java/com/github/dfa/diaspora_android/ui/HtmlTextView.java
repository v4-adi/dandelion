package com.github.dfa.diaspora_android.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.dfa.diaspora_android.R;

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
        final SpannableString content = new SpannableString(Html.fromHtml(getText().toString()));
        Linkify.addLinks(content, Linkify.WEB_URLS);
        setText(content);
    }
}
package com.github.dfa.diaspora_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.util.AppLog;

/**
 * Created by vanitas on 23.09.16.
 */

public class TestFragment extends CustomFragment {

    public static final String TAG = "com.github.dfa.diaspora_android.TestFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d(this, "onCreateView()");
        return inflater.inflate(R.layout.test__fragment, container, false);
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onCreateBottomOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}

package com.github.dfa.diaspora_android.fragment;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Customized abstract Fragment class with some useful methods
 * Created by vanitas on 21.09.16.
 */

public abstract class CustomFragment extends Fragment {
    /**
     * Return the tag used to identify the Fragment.
     * @return tag
     */
    public abstract String getFragmentTag();

    /**
     * Add fragment-dependent options to the bottom options toolbar
     * @param menu bottom menu
     * @param inflater inflater
     */
    public abstract void onCreateBottomOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * Return true if the fragment reacted to a back button press, false else.
     * In case the fragment returned false, the parent activity should handle the backPress.
     * @return did we react to the back press?
     */
    public abstract boolean onBackPressed();
}


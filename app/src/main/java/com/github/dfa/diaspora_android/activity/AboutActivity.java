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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.ui.HtmlTextView;
import com.github.dfa.diaspora_android.util.Helpers;
import com.github.dfa.diaspora_android.util.Log;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity that holds some fragments that show information about the app in a tab layout
 */
public class AboutActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.linearlayout)
    protected LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.this.onBackPressed();
            }
        });
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Apply intellihide
        if(!((App)getApplication()).getSettings().isIntellihideToolbars()) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) linearLayout.getLayoutParams();
            params.setScrollFlags(0);
        }
    }

    /**
     * Fragment that shows general information about the app
     */
    public static class AboutFragment extends Fragment {

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            TextView appVersion = (TextView) rootView.findViewById(R.id.fragment_about__app_version);

            if (isAdded()) {
                try {
                    PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    appVersion.setText(getString(R.string.fragment_debug__app_version, pInfo.versionName + " (" + pInfo.versionCode + ")"));

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return rootView;
        }
    }

    /**
     * Fragment that shows information about the license of the app and used 3rd party libraries
     */
    public static class LicenseFragment extends Fragment {
        @BindView(R.id.fragment_license__licensetext)
        HtmlTextView textLicenseBox;

        @BindView(R.id.fragment_license__3rdparty)
        HtmlTextView textLicense3partyBox;


        public LicenseFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_license, container, false);
            ButterKnife.bind(this, rootView);
            final Context context = rootView.getContext();
            accentColor = Helpers.hexColorFromRessourceColor(context, R.color.colorAccent);

            textLicenseBox.setTextFormatted(getString(R.string.fragment_license__license_content,
                    getMaintainersHtml(context),
                    getContributorsHtml(context),
                    getLicenseHtml(context)
            ));

            textLicense3partyBox.setTextFormatted(
                    getLicense3dPartyHtml(context)
            );
            return rootView;
        }

        private String accentColor;

        public String getContributorsHtml(Context context) {
            String text = Helpers.readTextfileFromRawRessource(context, R.raw.contributors,
                    "<font color='" + accentColor + "'><b>*</b></font> ", "<br>");
            return text;
        }

        public String getMaintainersHtml(Context context) {
            String text = Helpers.readTextfileFromRawRessource(context, R.raw.maintainers, "", "<br>");
            text = text
                    .replace("NEWENTRY", "<font color='" + accentColor + "'><b>*</b></font> ")
                    .replace("SUBTABBY", "&nbsp;&nbsp;");
            return text;
        }

        public String getLicenseHtml(Context context) {
            String text = Helpers.readTextfileFromRawRessource(context, R.raw.license,
                    "", "").replace("\n\n", "<br><br>");
            return text;
        }

        public String getLicense3dPartyHtml(Context context) {
            String text = Helpers.readTextfileFromRawRessource(context, R.raw.license_third_party, "", "<br>");
            text = text.replace("NEWENTRY", "<font color='" + accentColor + "'><b>*</b></font> ");
            return text;
        }
    }

    /**
     * Fragment that shows debug information like app version, pod version...
     */
    public static class DebugFragment extends Fragment implements Observer {
        private TextView logBox;
        public DebugFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_debug, container, false);
            TextView packageName = (TextView) rootView.findViewById(R.id.fragment_debug__package_name);
            TextView appVersion = (TextView) rootView.findViewById(R.id.fragment_debug__app_version);
            TextView osVersion = (TextView) rootView.findViewById(R.id.fragment_debug__android_version);
            TextView deviceName = (TextView) rootView.findViewById(R.id.fragment_debug__device_name);
            TextView podDomain = (TextView) rootView.findViewById(R.id.fragment_debug__pod_domain);
            logBox = (TextView) rootView.findViewById(R.id.fragment_debug__log_box);

            Log.addLogObserver(this);
            update(Log.getInstance(), null);

            if (isAdded()) {
                try {
                    PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    AppSettings settings = ((App) getActivity().getApplication()).getSettings();

                    packageName.setText(pInfo.packageName);
                    appVersion.setText(getString(R.string.fragment_debug__app_version, pInfo.versionName + " (" + pInfo.versionCode + ")"));

                    osVersion.setText(getString(R.string.fragment_debug__android_version, Build.VERSION.RELEASE));
                    deviceName.setText(getString(R.string.fragment_debug__device_name, Build.MANUFACTURER+" "+Build.MODEL));
                    podDomain.setText(getString(R.string.fragment_debug__pod_domain, settings.getPodDomain()));

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
            return rootView;
        }

        @Override
        public void onDestroyView() {
            Log.removeLogObserver(this);
            super.onDestroyView();
        }

        @Override
        public void update(Observable observable, Object o) {
            if(logBox != null) {
                ArrayList<String> logs = Log.getLogBuffer();
                String log = "";
                for(String s : logs) {
                    log = log + s+"\n";
                }
                logBox.setText(log);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: //About
                    return new AboutFragment();
                case 1: //License
                    return new LicenseFragment();
                case 3: //Debug
                default:
                    return new DebugFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.about_activity__title_about_app);
                case 1:
                    return getString(R.string.about_activity__title_about_license);
                case 2:
                    return getString(R.string.about_activity__title_debug_info);
            }
            return null;
        }
    }
}

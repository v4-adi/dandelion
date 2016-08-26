package com.github.dfa.diaspora_android.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.data.AppSettings;

public class AboutActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

    }

    /**
     * Fragment that shows information about the app
     */
    public static class AboutFragment extends Fragment {

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            TextView appVersion = (TextView) rootView.findViewById(R.id.fragment_about__app_version);

            if(isAdded()) {
                try {
                    PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    appVersion.setText(getString(R.string.fragment_debug__app_version, pInfo.versionName+ " ("+pInfo.versionCode+")"));

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return rootView;
        }
    }

    /**
     * Fragment that shows information about the app
     */
    public static class LicenseFragment extends Fragment {

        public LicenseFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_license, container, false);
            return rootView;
        }
    }

    /**
     * Fragment that shows information about the app
     */
    public static class DebugFragment extends Fragment {

        public DebugFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_debug, container, false);
            TextView packageName = (TextView) rootView.findViewById(R.id.fragment_debug__package_name);
            TextView appVersion = (TextView) rootView.findViewById(R.id.fragment_debug__app_version);
            TextView podDomain = (TextView) rootView.findViewById(R.id.fragment_debug__pod_domain);

            if(isAdded()) {
                try {
                    PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    AppSettings settings = ((App) getActivity().getApplication()).getSettings();

                    packageName.setText(pInfo.packageName);
                    appVersion.setText(getString(R.string.fragment_debug__app_version, pInfo.versionName+ " ("+pInfo.versionCode+")"));
                    podDomain.setText(getString(R.string.fragment_debug__pod_domain, settings.getPodDomain()));

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
            return rootView;
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

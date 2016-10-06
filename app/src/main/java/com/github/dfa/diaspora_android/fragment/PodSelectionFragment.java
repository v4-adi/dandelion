package com.github.dfa.diaspora_android.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.activity.MainActivity;
import com.github.dfa.diaspora_android.data.AppSettings;
import com.github.dfa.diaspora_android.data.DiasporaPodList;
import com.github.dfa.diaspora_android.data.DiasporaPodList.DiasporaPod;
import com.github.dfa.diaspora_android.task.GetPodsService;
import com.github.dfa.diaspora_android.ui.PodSelectionDialog;
import com.github.dfa.diaspora_android.util.AppLog;
import com.github.dfa.diaspora_android.util.DiasporaUrlHelper;
import com.github.dfa.diaspora_android.util.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that lets the user choose a Pod
 * Created by vanitas on 01.10.16.
 */

public class PodSelectionFragment extends CustomFragment implements SearchView.OnQueryTextListener, PodSelectionDialog.PodSelectionDialogResultListener {
    public static final String TAG = "com.github.dfa.diaspora_android.PodSelectionFragment";

    @BindView(R.id.podselection__fragment__listpods)
    protected ListView listViewPod;

    protected App app;
    protected AppSettings appSettings;
    private DiasporaPodList podList;
    private ArrayAdapter<String> listViewPodAdapter;
    private String filterString = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d(this, "onCreateView()");
        View view = inflater.inflate(R.layout.podselection__fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        app = (App) getActivity().getApplication();
        appSettings = app.getSettings();

        // Load local podlist
        podList = new DiasporaPodList();
        mergePodlistWithRessources(podList);
        podList.setTrackMergeChanges(true);
        updateListedPods();


        listViewPod.setTextFilterEnabled(true);
        listViewPod.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showPodSelectionDialog(podList.getPodAt(i));
            }
        });
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(podListReceiver, new IntentFilter(GetPodsService.MESSAGE_PODS_RECEIVED));
        Helpers.showInfoIfUserNotConnectedToInternet(getContext(), listViewPod);
    }

    public void mergePodlistWithRessources(DiasporaPodList podlist) {
        String sPodlist = Helpers.readTextfileFromRawRessource(getContext(), R.raw.podlist, "", "");
        try {
            JSONObject jPodlist = new JSONObject(sPodlist);
            podlist.mergeWithNewerEntries(new DiasporaPodList().fromJson(jPodlist));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.podselection__fragment__button_use_custom_pod)
    public void onPodButtonClicked(View v) {
        showPodSelectionDialog(new DiasporaPod());
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    private final BroadcastReceiver podListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(GetPodsService.EXTRA_PODLIST)) {
                Bundle extras = intent.getExtras();
                DiasporaPodList newPods = (DiasporaPodList) extras.get(GetPodsService.EXTRA_PODLIST);
                if (newPods != null && newPods.getPods().size() > 0) {
                    try {
                        podList.mergeWithNewerEntries(newPods);
                        updateListedPods();
                    } catch (JSONException ignored) {
                    }
                } else {
                    Snackbar.make(listViewPod, R.string.podlist_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Intent i = new Intent(getContext(), GetPodsService.class);
        getContext().startService(i);
    }

    private void updateListedPods() {
        final ArrayList<String> listedPodsList = new ArrayList<>();
        for (DiasporaPod pod : this.podList) {
            listedPodsList.add(pod.getPodUrls().get(0).getHost());
        }

        listViewPodAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                listedPodsList);

        // save index and top position
        int index = listViewPod.getFirstVisiblePosition();
        View v = listViewPod.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - listViewPod.getPaddingTop());
        listViewPod.setAdapter(listViewPodAdapter);
        listViewPod.setSelectionFromTop(index, top);

        listViewPodAdapter.getFilter().filter(filterString);
    }

    private void showPodSelectionDialog(final DiasporaPod selectedPod) {
        PodSelectionDialog dialog = PodSelectionDialog.newInstance(selectedPod, this);
        dialog.show(getFragmentManager(), PodSelectionDialog.TAG);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(podListReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.podselection__menu, menu);

        MenuItem searchItem = menu.findItem(R.id.podselection__action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnQueryTextListener(this);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload: {
                if (!Helpers.showInfoIfUserNotConnectedToInternet(getContext(), listViewPod)) {
                    Intent i = new Intent(getContext(), GetPodsService.class);
                    getContext().startService(i);
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (listViewPodAdapter != null) {
            (listViewPodAdapter).getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public void onPodSelectionDialogResult(DiasporaPod pod, boolean accepted) {
        System.out.println(accepted + ": " + pod.toString());
        if (accepted) {
            //TODO: Rework for new pod url system ;)
            app.getSettings().setPodDomain(pod.getPodUrls().get(0).getHost());

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().removeSessionCookies(null);
                } else {
                    //noinspection deprecation
                    CookieManager.getInstance().removeAllCookie();
                    //noinspection deprecation
                    CookieManager.getInstance().removeSessionCookie();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((MainActivity) getActivity()).openDiasporaUrl(new DiasporaUrlHelper(appSettings).getPodUrl());
        }
    }


    /*
     *  Dummy implementations
     */

    @Override
    public void onCreateBottomOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
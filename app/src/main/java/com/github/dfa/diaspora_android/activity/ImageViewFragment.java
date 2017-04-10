package com.github.dfa.diaspora_android.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.service.ImageDownloadTask;
import com.github.dfa.diaspora_android.ui.theme.ThemedFragment;
import com.github.dfa.diaspora_android.util.AppLog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vanitas on 10.04.17.
 */

public class ImageViewFragment extends ThemedFragment {
    public static final String TAG = "com.github.dfa.diaspora_android.ImageViewFragment";
    public static final String IMAGE_SOURCE = "IMAGE_SOURCE";

    @BindView(R.id.imageView)
    SubsamplingScaleImageView imageView;
    @BindView(R.id.marker_progress)
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d(this, "onCreateView()");
        View view = inflater.inflate(R.layout.image_view__fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new ImageDownloadTask.ImageViewFragmentDownloadTask(this)
                .execute(getArguments().getString(IMAGE_SOURCE));
        imageView.setMaxScale(5);
        imageView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        imageView.setZoomEnabled(true);
    }

    @Override
    protected void applyColorToViews() {

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

    public void showBitmap(Bitmap bitmap) {
        progressBar.setVisibility(View.GONE);
        imageView.setImage(ImageSource.bitmap(bitmap));
    }
}

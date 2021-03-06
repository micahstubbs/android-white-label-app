package com.votinginfoproject.VotingInformationProject.activities.aboutActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.MotionEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.votinginfoproject.VotingInformationProject.R;
import com.votinginfoproject.VotingInformationProject.models.CoordinatePair;

/**
 * Created by max on 4/8/16.
 */
public class AboutVIPPresenterImpl extends AboutVIPPresenter {
    private static final String TAG = AboutVIPPresenterImpl.class.getSimpleName();

    private final Context mContext;

    private boolean mHasAttachedView = false;
    private boolean mPresentingLegalNotices = false;

    public AboutVIPPresenterImpl(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedState) {
        //Required onCreate override
    }

    @Override
    public void onSaveState(@NonNull Bundle state) {
        //Required onSaveState override
    }

    @Override
    public void onDestroy() {
        setView(null);
    }

    @Override
    public void onAttachView(AboutVIPView aboutVIPView) {
        super.onAttachView(aboutVIPView);

        if (!mHasAttachedView) {
            mHasAttachedView = true;
            navigateToInitialAboutView();
        }
    }

    @Override
    void termsOfUseClicked(MotionEvent event) {
        navigateToNewAboutView(R.string.about_title_terms,
                R.string.about_terms_description,
                false,
                new CoordinatePair((int) event.getRawX(), (int) event.getRawY()));
    }

    @Override
    void privacyPolicyClicked(MotionEvent event) {
        navigateToNewAboutView(R.string.about_title_privacy,
                R.string.about_privacy_description,
                false,
                new CoordinatePair((int) event.getRawX(), (int) event.getRawY()));
    }

    @Override
    void onBackPressed() {
        mPresentingLegalNotices = false;
        getView().navigateToPreviousView();
    }

    @Override
    void viewTransitionEnded() {

        if (mPresentingLegalNotices) {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            if (apiAvailability.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {
                getView().setInformationText(apiAvailability.getOpenSourceSoftwareLicenseInfo(mContext));
            } else {
                getView().setInformationText(mContext.getString(R.string.about_error_legal_notices));

            }
            getView().setLoading(false);
        }
    }

    private void navigateToNewAboutView(@StringRes int titleTextKey, @StringRes int descriptionTextKey, boolean isLoading, CoordinatePair transitionPoint) {
        navigateToNewAboutView(
                mContext.getString(titleTextKey),
                mContext.getString(descriptionTextKey),
                isLoading,
                transitionPoint);
    }

    private void navigateToInitialAboutView() {
        String defaultTitle = mContext.getString(R.string.about_app_title);
        String defaultInfoText = mContext.getString(R.string.about_app_description);

        getView().navigateToAboutView(defaultTitle, defaultInfoText, false, true, null);
    }

    private void navigateToNewAboutView(String titleText, String infoText, boolean isLoading, CoordinatePair transitionPoint) {
        getView().navigateToAboutView(titleText, infoText, isLoading, false, transitionPoint);
    }
}

package com.votinginfoproject.VotingInformationProject.fragments.pollingSitesFragment;

import android.support.annotation.LayoutRes;

import com.votinginfoproject.VotingInformationProject.activities.BasePresenter;
import com.votinginfoproject.VotingInformationProject.models.Election;
import com.votinginfoproject.VotingInformationProject.models.PollingLocation;

import java.util.ArrayList;

/**
 * Created by marcvandehey on 4/11/16.
 */
public abstract class PollingSitesPresenter extends BasePresenter<PollingSitesView> {
    abstract ArrayList<PollingLocation> getAllLocations();

    abstract Election getElection();

    abstract void menuItemClicked(@LayoutRes int sortType);

    abstract void itemClickedAtIndex(int index);
}
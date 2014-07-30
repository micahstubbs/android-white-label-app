package com.votinginfoproject.VotingInformationProject.fragments;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.votinginfoproject.VotingInformationProject.R;
import com.votinginfoproject.VotingInformationProject.activities.VIPTabBarActivity;
import com.votinginfoproject.VotingInformationProject.adapters.LocationInfoWindow;
import com.votinginfoproject.VotingInformationProject.models.CivicApiAddress;
import com.votinginfoproject.VotingInformationProject.models.PollingLocation;
import com.votinginfoproject.VotingInformationProject.models.VIPAppContext;
import com.votinginfoproject.VotingInformationProject.models.VoterInfo;

import java.util.ArrayList;
import java.util.HashMap;


public class VIPMapFragment extends SupportMapFragment {

    private static final String LOCATION_ID = "location_id";
    VoterInfo voterInfo;
    VIPTabBarActivity mActivity;
    static final Resources mResources = VIPAppContext.getContext().getResources();
    View rootView;
    LayoutInflater layoutInflater;
    ArrayList<PollingLocation> allLocations;
    GoogleMap map;
    String locationId;
    PollingLocation selectedLocation;
    LatLng thisLocation;
    LatLng homeLocation;
    HashMap<String, MarkerOptions> markers;


    public static VIPMapFragment newInstance(String tag) {
        // instantiate with map options
        GoogleMapOptions options = new GoogleMapOptions();
        VIPMapFragment fragment = VIPMapFragment.newInstance(options);

        Bundle args = new Bundle();
        args.putString(LOCATION_ID, tag);
        fragment.setArguments(args);

        return fragment;
    }

    public static VIPMapFragment newInstance(GoogleMapOptions options) {
        Bundle args = new Bundle();
        // need to send API key to initialize map
        args.putParcelable(mResources.getString(R.string.google_api_android_key), options);
        VIPMapFragment fragment = new VIPMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public VIPMapFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (VIPTabBarActivity) this.getActivity();

        voterInfo = ((VIPTabBarActivity) mActivity).getVoterInfo();
        allLocations = mActivity.getAllLocations();
        homeLocation = mActivity.getHomeLatLng();

        // set selected location to zoom to
        if (locationId  == "home") {
            thisLocation = homeLocation;
        } else {
            selectedLocation = mActivity.getLocationForId(locationId);
            CivicApiAddress address = selectedLocation.address;
            thisLocation = new LatLng(address.latitude, address.longitude);
        }

        // check if already instantiated
        if (map == null) {
            map = getMap();
            map.setInfoWindowAdapter(new LocationInfoWindow(layoutInflater));

            // start asynchronous task to add markers to map
            new AddMarkersTask().execute(locationId);

            // wait for map layout to occur before zooming to location
            final ViewTreeObserver observer = rootView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (observer.isAlive()) {
                        observer.removeGlobalOnLayoutListener(this);
                        // TODO: switch to below command for next API level
                        //observer.removeOnGlobalLayoutListener(this);
                    }

                    // add marker for user-entered address
                    map.addMarker(new MarkerOptions()
                                    .position(homeLocation)
                                    .title(mResources.getString(R.string.locations_map_user_address_label))
                    );
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(thisLocation, 15));
                }
            });
        } else {
            map.clear();
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        layoutInflater = getLayoutInflater(savedInstanceState);

        if (getArguments() != null) {
            locationId = getArguments().getString(LOCATION_ID);
        }
    }

    private class AddMarkersTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... select_locations) {
            markers = new HashMap<String, MarkerOptions>(allLocations.size());
            String showId = select_locations[0];

            // use green markers for early voting sites
            for (PollingLocation location : voterInfo.earlyVoteSites) {
                if (location.address.latitude == 0) {
                    Log.d("VIPMapFragment", "Skipping adding to map location " + location.name);
                    continue;
                }
                markers.put(location.address.toGeocodeString(), createMarkerOptions(location, BitmapDescriptorFactory.HUE_GREEN));
            }

            // use blue markers for polling locations
            for (PollingLocation location : voterInfo.pollingLocations) {
                if (location.address.latitude == 0) {
                    Log.d("VIPMapFragment", "Skipping adding to map location " + location.name);
                    continue;
                }
                markers.put(location.address.toGeocodeString(), createMarkerOptions(location, BitmapDescriptorFactory.HUE_AZURE));
            }

            return locationId;
        }

        @Override
        protected  void onPostExecute(String checkId) {
            for (String key : markers.keySet()) {
                Marker marker = map.addMarker(markers.get(key));
                if (key.equals(locationId)) {
                    // show popup for marker at selected location
                    marker.showInfoWindow();
                }
            }
        }
    }

    private MarkerOptions createMarkerOptions(PollingLocation location, float color) {

        String showTitle = location.name;
        if (showTitle == null || showTitle.isEmpty()) {
            showTitle = location.address.locationName;
        }

        String showSnippet = location.address.toGeocodeString();
        showSnippet += "\n\nHours:\n" + location.pollingHours;

        return new MarkerOptions()
                .position(new LatLng(location.address.latitude, location.address.longitude))
                .title(showTitle)
                .snippet(showSnippet)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
        ;
    }

}
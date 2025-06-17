package com.livestreaming.common.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.DialogFragment;

import com.livestreaming.common.R;
import com.livestreaming.common.bean.Cities;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.UserCurrentLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class FragmentLocationDetector extends DialogFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static FragmentLocationDetector instance;
    private ProgressBar loader;
    public OnLocationSet onLocationSet;

    public static FragmentLocationDetector getInstance() {
        if (instance == null) {
            instance = new FragmentLocationDetector();
        }
        return instance;
    }

    private ImageButton btnDoneMap;
    private ImageButton imageButtonMyLocation;
    private SearchView searchView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private SimpleCursorAdapter searchAdapter;
    private boolean pinSelected = false;
    private boolean myLocation = false;
    private String[] from = {SearchManager.SUGGEST_COLUMN_TEXT_1};
    private int[] to = {R.id.searchItemID};
    private List<String> cityes;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_location_detector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnDoneMap = view.findViewById(R.id.imageButtonMapConfirm);
        searchView = view.findViewById(R.id.searchViewPlace);
        searchAdapter = new SimpleCursorAdapter(getContext(),
                R.layout.suggestion_item_layout,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        searchView.setSuggestionsAdapter(searchAdapter);

        mMapView = view.findViewById(R.id.mapView);
        imageButtonMyLocation = view.findViewById(R.id.imageButtonMyLocation);
        cityes = new Cities().cities;

        loader = view.findViewById(R.id.loader);
        loader.setVisibility(View.INVISIBLE);
        imageButtonMyLocation.setOnClickListener(v -> {
            Toast.makeText(
                    requireContext(),
                    (LanguageUtil.getInstance().getLanguage().equals("ar") ? "جاري تحديد موقعك" : "Loading Your Location"),
                    Toast.LENGTH_SHORT).show();
            imageButtonMyLocation.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.VISIBLE);
            myLocation = true;
            getLastLocation();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public boolean onQueryTextSubmit(String query) {
                LatLng latlng = (query != null) ? UserCurrentLocation.getLatLonForAddress(query, requireContext().getApplicationContext()) : null;
                if (latlng != null) {
                    CameraUpdateFactory.newLatLngZoom(latlng, 10F);

                    pinSelected = true;
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(new MarkerOptions().position(latlng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10F));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1});
                    for (int i = 0; i < cityes.size(); i++) {
                        String s = cityes.get(i);
                        if (s.toLowerCase().startsWith(newText.toLowerCase())) {
                            cursor.addRow(new Object[]{i, s});
                        }
                    }
                    searchAdapter.changeCursor(cursor);
                }
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @SuppressLint("Range")
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String selection = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(selection, false);

                LatLng latlng = UserCurrentLocation.getLatLonForAddress(selection, requireContext().getApplicationContext());
                if (latlng != null) {
                    CameraUpdateFactory.newLatLngZoom(latlng, 10F);

                    pinSelected = true;
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(new MarkerOptions().position(latlng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10F));
                }
                return false;
            }
        });

        btnDoneMap.setOnClickListener(v -> {
            if (pinSelected) {
                onLocationSet.onLocation(UserCurrentLocation.getAddress(requireContext()));
                mMapView.onDestroy();
                dismiss();
            }
        });

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mGoogleApiClient = new GoogleApiClient.Builder(requireContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        setUpMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (myLocation) {
            loader.setVisibility(View.VISIBLE);
            getLastLocation();
        }
        getDialog().getWindow().getAttributes().x = -1;
        getDialog().getWindow().getAttributes().y = -1;
    }

    @SuppressLint("MissingPermission")
    public Task<Location> getCurrentLocation(CurrentLocationRequest request, CancellationToken token) {
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this.requireActivity());
        return fusedLocationProviderClient.getCurrentLocation(request, token);
    }

    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                requestNewLoaction();
            } else {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            loader.setVisibility(View.GONE);
            requestPermissions();
        }
    }

    private static final int PermissionID = 10;

    private void requestNewLoaction() {
        CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder().setPriority(
                Priority.PRIORITY_HIGH_ACCURACY
        ).setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL).build();
        getCurrentLocation(currentLocationRequest, null).addOnSuccessListener(location -> {
            try {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                UserCurrentLocation.setLatitude(String.valueOf(latitude));
                UserCurrentLocation.setLongitude(String.valueOf(longitude));

                fusedLocationProviderClient.removeLocationUpdates(FragmentLocationDetector.this);
                loader.setVisibility(View.GONE);
                if (myLocation) {
                    myLocation = false;
                    imageButtonMyLocation.setVisibility(View.VISIBLE);
                    CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10F);

                    pinSelected = true;
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(
                            new MarkerOptions().position(
                                    new LatLng(latitude,
                                            longitude
                                    )
                            ));
                    mGoogleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(latitude,
                                            longitude
                                    ), 10F
                            ));
                }
            } catch (IllegalStateException e) {
                Log.e("error", e.toString());
            }
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(
                Context.LOCATION_SERVICE
        );
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PermissionID
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loader.setVisibility(View.VISIBLE);
                getLastLocation();
            }
        }
    }

    private void setUpMap() {
        mMapView.getMapAsync(googleMap -> {
            mGoogleMap = googleMap;
            mGoogleMap.setOnMapLongClickListener(latlng -> {
                double latitude = latlng.latitude;
                double longitude = latlng.longitude;
                UserCurrentLocation.setLatitude(String.valueOf(latitude));
                UserCurrentLocation.setLongitude(String.valueOf(longitude));
                pinSelected = true;
                mGoogleMap.clear();
                mGoogleMap.addMarker(new MarkerOptions().position(latlng));
            });
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkPermissions()) getLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("", "Suspended Connection");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("", "Failed Connection");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e("", "Location Changed");
    }
}
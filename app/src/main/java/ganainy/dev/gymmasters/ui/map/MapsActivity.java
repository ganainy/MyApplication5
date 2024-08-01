package ganainy.dev.gymmasters.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ganainy.dev.gymmasters.BuildConfig;
import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.ActivityMapsBinding;
import ganainy.dev.gymmasters.databinding.ContentMapsBinding;
import ganainy.dev.gymmasters.databinding.MapHintBinding;
import ganainy.dev.gymmasters.databinding.PlaceInfoCardBinding;
import ganainy.dev.gymmasters.models.app_models.Gym;
import ganainy.dev.gymmasters.utils.SharedPrefUtils;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static ganainy.dev.gymmasters.utils.SharedPrefUtils.IS_FIRST_SHOWING_MAP;

public class MapsActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    private static final int RC_LOCATION = 1;
    public static final int REQUEST_GPS_CODE = 101;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final float DEFAULT_ZOOM = 18f;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private MapViewModel mViewModel;

    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient mPlacesClient;
    private List<AutocompletePrediction> placesPredictionList;
    private Location mLastKnownLocation;
    private LocationCallback mLocationCallback;
    private View mapView;
    private BottomSheetBehavior mBottomSheetBehavior;

    private ActivityMapsBinding binding;
    private ContentMapsBinding contentMapsBinding;
    private MapHintBinding mapHintBinding;
    private PlaceInfoCardBinding placeInfoCardBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        contentMapsBinding = ContentMapsBinding.bind(binding.contentMaps.getRoot());
        placeInfoCardBinding = PlaceInfoCardBinding.bind(binding.placeInfoCard.getRoot());
        mapHintBinding = MapHintBinding.bind(binding.contentMaps.hintLayout.getRoot());

        setContentView(binding.getRoot());

        checkShowHint();
        enableFullScreen();
        initViewModel();
        initPlacesApi();
        initBottomSheet();
        showMapFragment();
        checkLocationPermission();

        /*hide the hint about the map activity on confirm click*/
        mapHintBinding.buttonHint.setOnClickListener(view ->
                contentMapsBinding.hintLayout.getRoot().setVisibility(View.GONE));


        contentMapsBinding.buttonFindNearbyGym.setOnClickListener(v ->
                // Get last known location
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        mLastKnownLocation = location;
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        mViewModel.findNearbyGyms(new LatLng(location.getLatitude(),location.getLongitude()));
                    }
                })

                );

        placeInfoCardBinding.closeImage.setOnClickListener(v ->
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

    }

    /**only show hint about the map activity if this is first time*/
    private void checkShowHint() {
        if (SharedPrefUtils.getBoolean(this, IS_FIRST_SHOWING_MAP)) {
            // this is first time
            contentMapsBinding.hintLayout.getRoot().setVisibility(View.VISIBLE);
            SharedPrefUtils.putBoolean(this, false, IS_FIRST_SHOWING_MAP);
        }
    }





    private void initBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
    }

    private void showMapFragment() {
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
            mapView = mMapFragment.getView();
        }
    }

    private void initPlacesApi() {
        String placesApiKey = BuildConfig.PLACES_API_KEY;
        Places.initialize(this, placesApiKey);
        mPlacesClient = Places.createClient(this);
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        mViewModel.getGymInfoLiveData().observe(this, gymEvent -> {
            Gym gym = gymEvent.getContentIfNotHandled();
            if (gym != null) {
                placeInfoCardBinding.textViewRate.setText(getString(R.string.gym_rate, String.format("%.1f", gym.getRate())));
                placeInfoCardBinding.openingHours.setText(gym.getOpeningHours());
                placeInfoCardBinding.addressText.setText(gym.getAddress());
                placeInfoCardBinding.nameShimmer.setText(gym.getName());
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        mViewModel.getLocationLiveData().observe(this, locationEvent -> {
            LatLng location = locationEvent.getContentIfNotHandled();
            if (location != null) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM));
            }
        });

        mViewModel.getToastLiveData().observe(this, toastEvent -> {
            String toast = toastEvent.getContentIfNotHandled();
            if (toast != null) {
                Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
            }
        });

        mViewModel.getRippleLoadingLiveData().observe(this, showRipple -> {
            if (showRipple) {
                contentMapsBinding.rippleBackground.startRippleAnimation();
            } else {
                contentMapsBinding.rippleBackground.stopRippleAnimation();
            }
        });
    }

    /**get device country or return null if device don't have sim card*/
    private String getCountry() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    /**hide status bar*/
    private void enableFullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }







    /**calculate an area of 30km around current user location*/
        private LatLng[] calculateBounds(double centerLat, double centerLng) {
            final double distanceKm=30.0;
            final double EARTH_RADIUS_KM = 6371.0;

            double deltaLat = distanceKm / EARTH_RADIUS_KM;
            double deltaLng = distanceKm / (EARTH_RADIUS_KM * Math.cos(Math.PI * centerLat / 180));

            double deltaLatDeg = Math.toDegrees(deltaLat);
            double deltaLngDeg = Math.toDegrees(deltaLng);

            double southwestLat = centerLat - deltaLatDeg;
            double southwestLng = centerLng - deltaLngDeg;
            double northeastLat = centerLat + deltaLatDeg;
            double northeastLng = centerLng + deltaLngDeg;

            LatLng southwest = new LatLng(southwestLat, southwestLng);
            LatLng northeast = new LatLng(northeastLat, northeastLng);

            return new LatLng[]{southwest, northeast};
        }

    /**fetch place details from places API*/
    private void fetchPlace(String placeId) {
        final List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        mPlacesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            LatLng latLng = place.getLatLng();
            if (latLng != null) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        }).addOnFailureListener(exception -> Log.d(TAG, exception.getMessage()));
    }

    @SuppressLint("MissingPermission")
    private void checkLocationPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            setUpMap();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        }
    }

    private void setUpMap() {
        if (mGoogleMap == null) {
            Toast.makeText(this, R.string.map_error, Toast.LENGTH_LONG).show();
            return;
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION); // Define REQUEST_LOCATION_PERMISSION as a constant
            return;
        }

        // Get last known location
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mLastKnownLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            }
        });

        // Set up location updates
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        mLocationCallback = new LocationCallback() {
            private boolean hasFoundNearestGym = false; // Flag to track if nearest gym has been found

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || hasFoundNearestGym) {
                    return; // Exit if no location or nearest gym already found
                }

                Location nearestLocation = null;
                double nearestDistance = Double.MAX_VALUE;

                for (Location location : locationResult.getLocations()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Calculate distance
                    double distance = calculateDistance(latLng, mLastKnownLocation); // Assuming mLastKnownLocation is your initial location

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestLocation = location;
                    }
                }

                if (nearestLocation != null) {
                    LatLng nearestLatLng = new LatLng(nearestLocation.getLatitude(), nearestLocation.getLongitude());
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestLatLng, DEFAULT_ZOOM));
                    mViewModel.findNearbyGyms(nearestLatLng);
                    hasFoundNearestGym = true; // Set flag to prevent further calls
                }
            }
        };

        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, getMainLooper());
        mGoogleMap.setMyLocationEnabled(true);
    }


    // Helper function to calculate distance using the Haversine formula
    private double calculateDistance(LatLng latLng1, Location location2) {
        if (location2 == null) {
            return Double.MAX_VALUE; // Handle cases where location2 is null
        }

        double lat1 = Math.toRadians(latLng1.latitude);
        double lon1 = Math.toRadians(latLng1.longitude);
        double lat2 = Math.toRadians(location2.getLatitude());
        double lon2 = Math.toRadians(location2.getLongitude());

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 6371 for kilometers
        double r = 6371;

        // Calculate the result
        return(c * r);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try setting up the map again
                setUpMap();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setUpMap();
    }
}
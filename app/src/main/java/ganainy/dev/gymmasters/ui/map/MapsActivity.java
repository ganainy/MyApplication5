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
        initMaterialSearchBar();
        initViewModel();
        initPlacesApi();
        initBottomSheet();
        showMapFragment();
        checkLocationPermission();
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
        Places.initialize(this, getString(R.string.google_places_key));
        mPlacesClient = Places.createClient(this);
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        mViewModel.getGymInfoLiveData().observe(this, gymEvent -> {
            Gym gym = gymEvent.getContentIfNotHandled();
            if (gym != null) {
                placeInfoCardBinding.textViewRate.setText(getString(R.string.gym_rate, String.format("%.1f", gym.getRate())));
                placeInfoCardBinding.textViewOpeningHours.setText(gym.getOpeningHours());
                placeInfoCardBinding.textViewAddress.setText(gym.getAddress());
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

    private void initMaterialSearchBar() {
        contentMapsBinding.searchBar.setOnClickListener(view -> contentMapsBinding.searchBar.enableSearch());

        contentMapsBinding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //todo findPlaceByName(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                /*handle back button of material search bar*/
                if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    contentMapsBinding.searchBar.disableSearch();
                    contentMapsBinding.searchBar.clearSuggestions();
                }
            }
        });

        contentMapsBinding.searchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                /*we only have place id (from FindAutocompletePredictionsRequest) and we need lat lng to be able to show place on map using places API*/
                if (position >= placesPredictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = placesPredictionList.get(position);
                String suggestion = contentMapsBinding.searchBar.getLastSuggestions().get(position).toString();
                contentMapsBinding.searchBar.setText(suggestion);

                //wait one second before using clearSuggestions for it to work properly
                new Handler().postDelayed(() -> contentMapsBinding.searchBar.clearSuggestions(), (1000));

                //close soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(contentMapsBinding.searchBar.getWindowToken(), 0);

                fetchPlace(selectedPrediction.getPlaceId());
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        contentMapsBinding.searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //find prediction using autocomplete prediction
                if (s.length() >= 2) {
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(s.toString())
                            .setTypeFilter(TypeFilter.ADDRESS)
                            .build();

                    mPlacesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener(response -> {
                                placesPredictionList = response.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (AutocompletePrediction prediction : placesPredictionList) {
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                contentMapsBinding.searchBar.updateLastSuggestions(suggestionsList);
                                contentMapsBinding.searchBar.showSuggestionsList();
                            })
                            .addOnFailureListener(exception -> Log.d(TAG, exception.getMessage()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mLastKnownLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        });

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                    //todo mViewModel.updateLocation(latLng);
                }
            }
        };

        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, getMainLooper());
        mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
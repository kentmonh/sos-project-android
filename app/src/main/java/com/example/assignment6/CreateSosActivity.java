package com.example.assignment6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Mapbox
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

// Google Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateSosActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    // Location Engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    // Variables needed to listen to location updates
    private CreateSosActivityLocationCallback callback = new CreateSosActivityLocationCallback(this);

    // Variables for add data to firebase
    private EditText etName, etPhone, etAddress, etNote;
    private Button btnCreateSos;

    private double lat = 181, lng = 181;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sos);

        mapView = (MapView) findViewById(R.id.mapView_createSos);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Log.d("MINH", "Lat: "+ lat);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etNote = findViewById(R.id.etNote);

        // Event handling
        btnCreateSos = findViewById(R.id.btnCreateSos);
        CreateSosActivity.EventHandler eventHandler = new CreateSosActivity.EventHandler();
        btnCreateSos.setOnClickListener(eventHandler);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("sos");
    }

    // Click listener
    class EventHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btnCreateSos) {
                AddSos();
                Intent intent = new Intent(CreateSosActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    // Pass back data
    public void AddSos() {
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();
        String address = etAddress.getText().toString();
        String note = etNote.getText().toString();

        if (lat > 180) {
            lat = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
            lng = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
        }

        Sos newSos = new Sos(name, phone, address, note, lat, lng);
        Log.d("MINH", "new Sos: "+ newSos);

        databaseReference.push().setValue(newSos);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        CreateSosActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapboxMap.addOnMapClickListener(CreateSosActivity.this);
                enableLocationComponent(style);
            }
        });
    }

    public boolean onMapClick(@NonNull LatLng point) {
        mapboxMap.clear();

        lat = point.getLatitude();
        lng = point.getLongitude();

        // Add icon
        IconFactory iconFactory = IconFactory.getInstance(CreateSosActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);

        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(icon));

        return true;
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

        // Get an instance of the component
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Set the LocationComponent activation options
        LocationComponentActivationOptions locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                        .useDefaultLocationEngine(false)
                        .build();

        // Activate with the LocationComponentActivationOptions object
        locationComponent.activateLocationComponent(locationComponentActivationOptions);

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

        initLocationEngine();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Do not have permission!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "Do not have permission!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    private static class CreateSosActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<CreateSosActivity> activityWeakReference;

        CreateSosActivityLocationCallback(CreateSosActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            CreateSosActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("MINH", exception.getLocalizedMessage());
            CreateSosActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MINH", "Activity B - onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("MINH", "Activity B - onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MINH", "Activity B - onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MINH", "Activity B - onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MINH", "Activity B - onStop()");
    }

    @Override
    protected void onDestroy() {
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }

        super.onDestroy();
        Log.d("MINH", "Activity B - onDestroy()");
    }
}
package com.example.assignment6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<Sos> sosItems = new ArrayList<Sos>();
    private ArrayList<String> sosKeys = new ArrayList<String>();
    private ArrayList<Rescue> rescueItems = new ArrayList<Rescue>();
    private ArrayList<String> rescueKeys = new ArrayList<String>();

    private MapView mapView;
    private MapboxMap mapboxMap;
    private Toolbar toolbar;

    private DatabaseReference databaseReference;

    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //NOTE: You will have to put your own MapBox access token in the strings.xml file
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_main);

        // Toolbar
        toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
            }
        });

        ProcessFirebase();
    }

    public void ProcessFirebase() {
        FirebaseProcessingTask firebaseProcessingTask = new FirebaseProcessingTask();
        firebaseProcessingTask.execute();
    }

    class FirebaseProcessingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        // Add icon
                        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                        Icon iconSos = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);
                        Icon iconRescue = iconFactory.fromAsset("marker3ss.bmp");

                        String type = dataSnapshot1.getKey();
                        Log.d("MINH", "Test data type: " + type);
                        if (type.equals("sos")) {
                            sosItems = new ArrayList<Sos>();
                            sosKeys = new ArrayList<String>();

                            for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                                String key = dataSnapshot2.getKey();
                                Log.d("MINH", "Key of sos: " + key);
                                Sos s = dataSnapshot2.getValue(Sos.class);
                                sosItems.add(s);
                                sosKeys.add(key);

                                Log.d("MINH", "sosItems size: " + sosItems.size());

                                // mapboxMap.clear();

                                for (int i = 0; i < sosItems.size(); i++) {
                                    mapboxMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(sosItems.get(i).getLat(), sosItems.get(i).getLng()))
                                            .title(sosItems.get(i).getName() + " / " + sosItems.get(i).getMobilePhone()
                                                    + "\r\n" + sosItems.get(i).getAddress() + " / " + sosItems.get(i).getNote())
                                            .snippet(type + ":" + sosKeys.get(i))
                                            .icon(iconSos));
                                }
                            }
                        }
                        else if (type.equals("rescue")) {
                            rescueItems = new ArrayList<Rescue>();
                            rescueKeys = new ArrayList<String>();

                            for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                                String key = dataSnapshot2.getKey();
                                Log.d("MINH", "Key of rescue: " + key);
                                Rescue r = dataSnapshot2.getValue(Rescue.class);
                                rescueItems.add(r);
                                rescueKeys.add(key);

                                Log.d("MINH", "rescueItems size: " + rescueItems.size());

                                mapboxMap.clear();

                                for (int i = 0; i < rescueItems.size(); i++) {
                                    mapboxMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(rescueItems.get(i).getLat(), rescueItems.get(i).getLng()))
                                            .title(rescueItems.get(i).getName() + " / " + rescueItems.get(i).getMobilePhone())
                                            .snippet(type + ":" + rescueKeys.get(i))
                                            .icon(iconRescue));
                                }
                            }
                        }


                        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                            Marker lastMarker = null;

                            public boolean onMarkerClick(@NonNull final Marker marker) {
                                if (marker != lastMarker) {

                                    marker.showInfoWindow(mapboxMap, mapView);
                                    btnDelete.setVisibility(View.VISIBLE);

                                    if (lastMarker != null) {
                                        lastMarker.hideInfoWindow();
                                    }
                                } else {
                                    if (marker.isInfoWindowShown()) {
                                        marker.hideInfoWindow();
                                        btnDelete.setVisibility(View.GONE);
                                    } else {
                                        marker.showInfoWindow(mapboxMap, mapView);
                                        btnDelete.setVisibility(View.VISIBLE);
                                    }
                                }
                                lastMarker = marker;

                                // Deal with delete function.
                                String[] keys = marker.getSnippet().split(":");

                                Log.d("MINH", "Location type: " + keys[0]);
                                Log.d("MINH", "Location key: " + keys[1]);
                                databaseReference = FirebaseDatabase.getInstance().getReference().child(keys[0]).child(keys[1]);
                                Log.d("MINH", "Database Reference: " + databaseReference);

                                btnDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("MINH", "Delete success!");
                                                    marker.remove();
                                                    btnDelete.setVisibility(View.GONE);
                                                } else {
                                                    Log.d("MINH", "S.t happen in delete!");
                                                }
                                            }

                                        });
                                    }
                                });

                                return true;
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // set code to show an error
                    Toast.makeText(getApplicationContext(), "Cannot retrieve data", Toast.LENGTH_SHORT).show();
                }
            });
            return null;

//            // Markers for Sos
//            databaseReference = FirebaseDatabase.getInstance().getReference().child("sos");
//            databaseReference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    // set code to retrieve data and replace layout
//                    sosItems.clear();
//
//                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
//                    {
//                        Sos s = dataSnapshot1.getValue(Sos.class);
//                        sosItems.add(s);
//                        sosKeys.add(dataSnapshot1.getKey());
//                        Log.d("MINH", "get key of sos items: " + dataSnapshot1.getKey());
//                    }
//
//                    Log.d("MINH", "sosItems size: " + sosItems.size());
//
//                    // Add icon
//                    IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
//                    Icon iconSos = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);
//
//                    mapboxMap.clear();
//
//                    for (int i = 0; i < sosItems.size(); i++) {
//                        mapboxMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(sosItems.get(i).getLat(), sosItems.get(i).getLng()))
//                                .title(sosItems.get(i).getName() + " / " + sosItems.get(i).getMobilePhone()
//                                    + "\r\n" + sosItems.get(i).getAddress() + " / " + sosItems.get(i).getNote())
//                                .snippet(sosKeys.get(i))
//                                .icon(iconSos));
//                    }
//
//                    mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
//
//                        Marker lastMarker = null;
//                        public boolean onMarkerClick(@NonNull final Marker marker) {
//                            if (marker != lastMarker) {
//
//                                marker.showInfoWindow(mapboxMap, mapView);
//                                btnDeleteSos.setVisibility(View.VISIBLE);
//
//                                if (lastMarker != null) {
//                                    lastMarker.hideInfoWindow();
//                                }
//                            }
//                            else {
//                                if (marker.isInfoWindowShown()) {
//                                    marker.hideInfoWindow();
//                                    btnDeleteSos.setVisibility(View.GONE);
//                                }
//                                else {
//                                    marker.showInfoWindow(mapboxMap, mapView);
//                                    btnDeleteSos.setVisibility(View.VISIBLE);
//                                }
//                            }
//                            lastMarker = marker;
//
//                            // Deal with delete function.
//                            Log.d("MINH", "Location key: " + marker.getSnippet());
//                            databaseReference = FirebaseDatabase.getInstance().getReference().child("sos").child(marker.getSnippet());
//                            Log.d("MINH", "Database Reference: " + databaseReference);
//
//                            btnDeleteSos.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                Log.d("MINH", "Delete success!");
//                                                Log.d("MINH", "sosItems size: " + sosItems.size());
//                                                marker.remove();
//                                                btnDeleteSos.setVisibility(View.GONE);
//                                            }
//                                            else {
//                                                Log.d("MINH", "S.t happen in delete!");
//                                            }
//                                        }
//
//                                    });
//                                }
//                            });
//
//                            return true;
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    // set code to show an error
//                    Toast.makeText(getApplicationContext(), "Cannot retrieve data", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            // Markers for Rescue
////            databaseReferenceRescue = FirebaseDatabase.getInstance().getReference().child("rescue");
////            databaseReferenceRescue.addValueEventListener(new ValueEventListener() {
////                @Override
////                public void onDataChange(DataSnapshot dataSnapshot) {
////                    // set code to retrieve data and replace layout
////                    rescueItems.clear();
////
////                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
////                    {
////                        Rescue r = dataSnapshot1.getValue(Rescue.class);
////                        rescueItems.add(r);
////                        rescueKeys.add(dataSnapshot1.getKey());
////                        Log.d("MINH", "get key of rescue items: " + dataSnapshot1.getKey());
////                    }
////
////                    Log.d("MINH", "rescueItems size: " + rescueItems.size());
////
////                    // Add icon
////                    IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
////                    Icon iconRescue = iconFactory.fromResource(R.drawable.mapbox_compass_icon);
////
////                    mapboxMap.clear();
////
////                    for (int i = 0; i < rescueItems.size(); i++) {
////                        mapboxMap.addMarker(new MarkerOptions()
////                                .position(new LatLng(rescueItems.get(i).getLat(), rescueItems.get(i).getLng()))
////                                .title(rescueItems.get(i).getName() + " / " + rescueItems.get(i).getMobilePhone())
////                                .snippet(rescueKeys.get(i))
////                                .icon(iconRescue));
////                    }
////
////                    mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
////
////                        Marker lastMarker = null;
////                        public boolean onMarkerClick(@NonNull final Marker marker) {
////                            if (marker != lastMarker) {
////
////                                marker.showInfoWindow(mapboxMap, mapView);
////                                btnDeleteRescue.setVisibility(View.VISIBLE);
////
////                                if (lastMarker != null) {
////                                    lastMarker.hideInfoWindow();
////                                }
////                            }
////                            else {
////                                if (marker.isInfoWindowShown()) {
////                                    marker.hideInfoWindow();
////                                    btnDeleteRescue.setVisibility(View.GONE);
////                                }
////                                else {
////                                    marker.showInfoWindow(mapboxMap, mapView);
////                                    btnDeleteRescue.setVisibility(View.VISIBLE);
////                                }
////                            }
////                            lastMarker = marker;
////
////                            // Deal with delete function.
////                            Log.d("MINH", "Location key: " + marker.getSnippet());
////                            databaseReferenceRescue = FirebaseDatabase.getInstance().getReference().child("rescue").child(marker.getSnippet());
////                            Log.d("MINH", "Database Reference: " + databaseReferenceRescue);
////
////                            btnDeleteRescue.setOnClickListener(new View.OnClickListener() {
////                                @Override
////                                public void onClick(View view) {
////                                    databaseReferenceRescue.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
////                                        @Override
////                                        public void onComplete(@NonNull Task<Void> task) {
////                                            if (task.isSuccessful()) {
////                                                Log.d("MINH", "Delete success!");
////                                                Log.d("MINH", "rescueItems size: " + rescueItems.size());
////                                                marker.remove();
////                                                btnDeleteRescue.setVisibility(View.GONE);
////                                            }
////                                            else {
////                                                Log.d("MINH", "S.t happen in delete!");
////                                            }
////                                        }
////
////                                    });
////                                }
////                            });
////
////                            return true;
////                        }
////                    });
////                }
////
////                @Override
////                public void onCancelled(DatabaseError databaseError) {
////                    // set code to show an error
////                    Toast.makeText(getApplicationContext(), "Cannot retrieve data", Toast.LENGTH_SHORT).show();
////                }
////            });
//
//            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }



    // To inflate the xml menu file for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return true;
    }

    //to handle events
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_sos:
                Intent intentCreateSosActivity = new Intent(this, CreateSosActivity.class);
                startActivity(intentCreateSosActivity);
                break;
            case R.id.create_rescue:
                Intent intentCreateRescueActivity = new Intent(this, CreateRescueActivity.class);
                startActivity(intentCreateRescueActivity);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
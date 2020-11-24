package com.example.assignment6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<Sos> sosItems = new ArrayList<Sos>();
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Toolbar toolbar;
    private DatabaseReference databaseReference;

    private int numberOfSos;

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
            // Markers
            databaseReference = FirebaseDatabase.getInstance().getReference().child("sos");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // set code to retrieve data and replace layout
                    sosItems.clear();
                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        Sos p = dataSnapshot1.getValue(Sos.class);
                        Log.d("MINH", "Firebase name: " + p.getName());
                        sosItems.add(p);
                    }

                    // Add icon
                    IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                    Icon icon = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);

                    for (int i = 0; i < sosItems.size(); i++) {
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(sosItems.get(i).getLat(), sosItems.get(i).getLng()))
                                .title(sosItems.get(i).getName() + " / " + sosItems.get(i).getMobilePhone())
                                .snippet(sosItems.get(i).getAddress() + " / " + sosItems.get(i).getNote())
                                .icon(icon));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // set code to show an error
                    Toast.makeText(getApplicationContext(), "No retrieve data", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
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
            // Add new Sos Location
            case R.id.create_sos:
                Intent intent = new Intent(this, CreateSosActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
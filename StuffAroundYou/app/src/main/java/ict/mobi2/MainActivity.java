package ict.mobi2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Range;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    @Bind(R.id.fab2)
    FloatingActionButton mFloatingActionButton2;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.nearby_locations_switch)
    Switch nearbyLocationSwitch;

    private Constants c = new Constants();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mGoogleMap;
    private GoogleLocationListener mGoogleLocationListener;
    private RegionBootstrap mRegionBootstrap;

    private HashMap<LatLng, String> mHashMap = new HashMap<>();
    public BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6 Permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    c.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            initGoogleClient();
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(new OnMapReady());
            createLocationRequest();
            mGoogleLocationListener = new GoogleLocationListener();
        }

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("nfc_data")) {
            String[] items = bundle.getStringArray("nfc_data");
            if (items != null && items.length > 0) {
                double latitude = Double.parseDouble(items[1]);
                double longitude = Double.parseDouble(items[2]);
                String locationText = items[0];

                LatLng latLng = new LatLng(latitude, longitude);

                mHashMap.put(latLng, locationText);
            }
        }

        mHashMap.put(c.CASA_ARENA, c.CASA_ARENA_TITLE);
        mHashMap.put(c.TOWN_HALL, c.TOWN_HALL_TITLE);
        mHashMap.put(c.INDUSTRIAL_MUSEUM, c.INDUSTRIAL_MUSEUM_TITLE);
        mHashMap.put(c.VIA_UNIVERSITY, c.VIA_UNIVERSITY_TITLE);
        mHashMap.put(c.FÆNGSLET, c.FÆNGSLET_TITLE);
        mHashMap.put(c.ART_GALLERY, c.ART_GALLERY_TITLE);


        mFloatingActionButton2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#303F9F")));
        mFloatingActionButton2.setRippleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#303F9F")));
        mFloatingActionButton.setRippleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        nearbyLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {

                    if (mGoogleApiClient != null && servicesAvailable()) {
                        mGoogleApiClient.connect();
                    }
                    initBeaconManager();
                } else {
                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.disconnect();
                    }
                    if (beaconManager != null) {
                        mRegionBootstrap.disable();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initGoogleClient();
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
                    mapFragment.getMapAsync(new OnMapReady());
                    createLocationRequest();
                    mGoogleLocationListener = new GoogleLocationListener();
                } else {
                    Toast.makeText(getApplicationContext(), "You need GPS Location for this app", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @OnClick(R.id.fab)
    public void click() {
        Intent intent = new Intent(MainActivity.this, RangeActivity.class);
        // use android:launchMode="singleInstance" or you will get two instances of the activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity.this.startActivity(intent);
    }

    @OnClick(R.id.fab2)
    public void click2() {
        startActivity(new Intent(MainActivity.this, NfcActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private class BootstrapNotifier implements org.altbeacon.beacon.startup.BootstrapNotifier, RangeNotifier {

        @Override
        public Context getApplicationContext() {
            return MainActivity.this.getApplicationContext();
        }

        @Override
        public void didEnterRegion(Region region) {
            try {
                beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
                beaconManager.setRangeNotifier(this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void didExitRegion(Region region) {

        }

        @Override
        public void didDetermineStateForRegion(int i, Region region) {

        }


        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
            ArrayList<String> bacons = new ArrayList<>();
            bacons.add("Laboratory 3");
            bacons.add("Laboratory 9");

            for (Beacon item : collection) {
                if (bacons.contains(item.getBluetoothName()) && item.getDistance() < 2) {
                    mRegionBootstrap.disable();
                    Intent intent = new Intent(MainActivity.this, PlaceActivity.class);
                    intent.putExtra("BeaconName", item.getBluetoothName());
                    // use android:launchMode="singleInstance" or you will get two instances of the activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                }
            }
        }
    }

    private class OnMapReady implements OnMapReadyCallback {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            mGoogleMap.setMyLocationEnabled(true);
            buildMarkers(mGoogleMap);
        }
    }

    private class GoogleLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            initCamera(location);
            stopLocationUpdates();
        }
    }

    private class GoogleConnectionListener implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle bundle) {
            getLocation();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d("MainActivity", "onConnectionSuspended");
        }
    }

    private class GoogleConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d("MainActivity", "onConnectionFailed");
        }
    }

    private void getLocation() {
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            initCamera(currentLocation);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mGoogleLocationListener);
        }
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleConnectionListener())
                .addOnConnectionFailedListener(new GoogleConnectionFailedListener())
                .addApi(LocationServices.API)
                .build();
    }

    private void initCamera(Location location) {
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13f));
        }
    }

    private void initBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //detect Kontakt type for beacons
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //scan every 5 seconds
        beaconManager.setBackgroundBetweenScanPeriod(0l);
        //scan lengh of scanning
        beaconManager.setBackgroundScanPeriod(1100l);
        Region region = new Region("ict.mobi2", null, null, null);
        mRegionBootstrap = new RegionBootstrap(new BootstrapNotifier(), region);
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mGoogleLocationListener);
    }

    private boolean servicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this, 0).show();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nearbyLocationSwitch.setChecked(false);
        if (mGoogleApiClient != null && servicesAvailable()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (beaconManager != null) {
            mRegionBootstrap.disable();
        }
    }

    private void buildMarkers(GoogleMap googleMap) {
        if (googleMap != null) {
            Iterator it = mHashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                googleMap.addMarker(new MarkerOptions()
                        .position((LatLng) pair.getKey())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .title((String) pair.getValue()));
            }
        }
    }
}

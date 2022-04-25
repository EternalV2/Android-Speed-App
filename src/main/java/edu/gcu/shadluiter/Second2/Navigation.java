package edu.gcu.shadluiter.Second2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Permission;
import java.util.List;

// This thread helps with the Google Maps problem
// https://stackoverflow.com/questions/41704179/fused-location-gps-doesnt-update-until-opening-google-maps

public class Navigation extends AppCompatActivity {

    public static final int Default_Update_Interval = 30;
    public static final int Fast_Update_Interval = 5;

    // Any Number Works. It Accesses Permissions
    public static final int Permission_Fine_Location = 99;

    // Declares UI Variables
    TextView address, sensor, lon, lat, speed, tv_Updates, countCrumbs;
    Button  newWayPoint, showWayPoint;
    Switch LocUpdates, GPS;

    // Current Location
    Location currentLocation;

    // List of Saved Locations
    List<Location> savedLocations;

    // Googles API for Location Services. Hub for Most Location Functionality
    FusedLocationProviderClient fusedLocationProviderClient;

    // Tells us if we are Tracking Location
    boolean updateOn = false;

    // Config File for Fused Location Provider Client (Holds Parameters and Initial Settings)
    LocationRequest locationRequest;

    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Connects UI Variables To Front End
        address = (TextView) findViewById(R.id.address);
        speed = (TextView) findViewById(R.id.speed);
        lon = (TextView) findViewById(R.id.lon);
        lat = (TextView) findViewById(R.id.lat);
        sensor = (TextView) findViewById(R.id.sensor);
        tv_Updates = (TextView) findViewById(R.id.tv_Updates);
        countCrumbs = (TextView) findViewById(R.id.countCrumbs);
        LocUpdates = (Switch) findViewById(R.id.LocUpdates);
        GPS = (Switch) findViewById(R.id.GPS);
        newWayPoint = (Button) findViewById(R.id.newWayPoint);
        showWayPoint = (Button) findViewById(R.id.showWayPoint);

        address.setText("Address: ");
        speed.setText("speed: ");
        lon.setText("Longitude: ");
        lat.setText("Latitude: ");
        sensor.setText("Using: Towers + Wifi");
        tv_Updates.setText("Tracking Off");
        LocUpdates.setText("Location Updates");
        GPS.setText("GPS");

        // Set All properties of Location Request

        locationRequest = LocationRequest.create()
                .setInterval(Default_Update_Interval * 1000)
                .setFastestInterval(Fast_Update_Interval * 1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Event is triggered whenever the update interval is met
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Save location
                Location location = locationResult.getLastLocation();
                UpdateUIValues(location);
            }
        };

        updateGPS();

    } // End On Create

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case Permission_Fine_Location:
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }else{
                Toast.makeText(this, "This app requires permission to be granted in order to work correctly", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    public void isOn(View view) {
        if(GPS.isChecked()){
            // Most Accurate Use GPS
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            sensor.setText("Using: GPS Sensor");
        }else{
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            sensor.setText("Using: Tower + WIFI");
        }
    }



    public void updateGPS(){
        // Get Permission From User
        // Get the Current Location From Fused Client
        // Update the UI

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Navigation.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // User Provided Permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                    // We Got Permissions. Put the Values of Location On To UI.
                        UpdateUIValues(location);
                        currentLocation = location;
                        // USING THIS LINE TO TEST
                        //lat.setText(String.valueOf(location.getLatitude()));
                    }else{
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            requestPermissions(new String[]  {Manifest.permission.ACCESS_FINE_LOCATION}, Permission_Fine_Location);
                        }
                    }
                }
            });
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]  {Manifest.permission.ACCESS_FINE_LOCATION}, Permission_Fine_Location);
            }
            // Permission Not Granted
        }
    }

    private void UpdateUIValues(Location location){
        // Update TextView Objects with a new Location

        lat.setText("Latitude: " + location.getLatitude());
        lon.setText("Longitude: " + location.getLongitude());

        if(location.hasSpeed()){
            speed.setText("Speed: " + location.getSpeed());
        }else{
            speed.setText("Not Available");
        }

        // Gets Address of Last Known Location
        Geocoder geocoder = new Geocoder(Navigation.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address.setText(addresses.get(0).getAddressLine(0));
        }catch(Exception e) {
            address.setText("Unable To Get Street Address");
        }

        /*MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocation();*/

        // Show the Number of WayPoints saved

       countCrumbs.setText(Integer.toString(savedLocations.size()));

    }

    public void updateLoc(View view) {
        if (LocUpdates.isChecked()) {
            // Turn on Location Tracking
            startLocationUpdates();
            tv_Updates.setText("Tracking On");
        } else {
            // Turn off Location Tracking
            stopLocationUpdates();
            tv_Updates.setText("Tracking Off");
        }
    }

    // Remember: LocationRequest is all the parameters for the locationProvider Client (Normal Frequency, Fast Frequency, Priority)

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        tv_Updates.setText("Tracking Enabled");

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        tv_Updates.setText("Tracking Disabled");
        lat.setText("Tracking Disabled");
        lon.setText("Tracking Disabled");
        speed.setText("Tracking Disabled");
        address.setText("Tracking Disabled");
        sensor.setText("Tracking Disabled");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void createPoint(View view) {
        // Get The GPS Location


        // Add Location to the List

        // Creates Reference to Global Class (Class all files can access)
        /*MyApplication myApplication = (MyApplication) getApplicationContext();

        // Initializes savedLocations with reference of global list
        savedLocations = myApplication.getMyLocation();
        savedLocations.add(currentLocation);*/
    }

}
package com.example.mindmate;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;


public class LocationHelper {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10001;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Activity activity) {
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void requestLocation() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        //error handling in case location is being called on user who is not signed in
                        if (user == null) {
                            Log.e("LocationHelper", "No user signed in");
                            return;
                        }

                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            Map<String, Object> locationData = new HashMap<>();
                            locationData.put("lat", lat);
                            locationData.put("lon", lon);
                            locationData.put("timestamp", FieldValue.serverTimestamp());

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user.getUid())
                                    .update("location", locationData)
                                    .addOnSuccessListener(aVoid -> Log.d("LocationHelper", "Location updated"))
                                    .addOnFailureListener(e -> Log.e("LocationHelper", "Failed to update location", e));


                            Toast.makeText(activity, "Lat: " + lat + ", Lon: " + lon, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, "Location is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
//To call the function of this class use this code to create an instance:
// LocationHelper locationHelper = new LocationHelper(this);
//locationHelper.requestLocation();

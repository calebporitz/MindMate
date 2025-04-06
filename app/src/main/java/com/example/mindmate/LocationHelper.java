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

    public interface LocationCallback {
        void onLocationRetrieved(Location location);
    }

    public LocationHelper(Activity activity) {
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void requestLocation(LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation(callback);
        }
    }

    private void getLastLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            callback.onLocationRetrieved(location);
                        } else {
                            callback.onLocationRetrieved(null);  // Call the callback with null to handle it in the activity
                        }
                    }
                });
    }
}

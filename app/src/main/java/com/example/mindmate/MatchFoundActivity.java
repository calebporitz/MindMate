package com.example.mindmate;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MatchFoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchfound);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.matchfound); // Make sure this image is in your drawable folder

        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);

        new Handler().postDelayed(() -> {
            Toast.makeText(this, "Location: Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_LONG).show();
            finish();
        }, 5000);
    }
}

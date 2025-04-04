package com.example.mindmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Initialize ViewPager2 and BottomNavigationView
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the adapter for ViewPager2 (you should have a ViewPagerAdapter to manage your fragments)
        viewPager.setAdapter(new NavigationAdapter(this));

        // Set up BottomNavigationView and link it with ViewPager2
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Using if-else instead of switch to avoid the constant expression issue
            if (item.getItemId() == R.id.home) {
                viewPager.setCurrentItem(0);  // Navigate to home
                return true;
            } else if (item.getItemId() == R.id.search) {
                viewPager.setCurrentItem(1);  // Navigate to search
                return true;
            } else if (item.getItemId() == R.id.profile) {
                viewPager.setCurrentItem(2);  // Navigate to profile
                return true;
            }
            return false;
        });

        // Link ViewPager2 with BottomNavigationView for page navigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update BottomNavigationView item selection based on the current page
                if (position == 0) {
                    bottomNavigationView.setSelectedItemId(R.id.home);
                } else if (position == 1) {
                    bottomNavigationView.setSelectedItemId(R.id.search);
                } else if (position == 2) {
                    bottomNavigationView.setSelectedItemId(R.id.profile);
                }
            }
        });
    }
}

package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.localloopapp_android.R;

public class MyTicketsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        TabLayout tabLayout = findViewById(R.id.tabLayoutTickets);
        ViewPager2 viewPager = findViewById(R.id.viewPagerTickets);
        viewPager.setAdapter(new TicketsPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Upcoming");
            } else {
                tab.setText("Past");
            }
        }).attach();
    }

    private static class TicketsPagerAdapter extends FragmentStateAdapter {
        public TicketsPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return TicketsListFragment.newInstance(true); // Upcoming
            } else {
                return TicketsListFragment.newInstance(false); // Past
            }
        }
        @Override
        public int getItemCount() {
            return 2;
        }
    }
}

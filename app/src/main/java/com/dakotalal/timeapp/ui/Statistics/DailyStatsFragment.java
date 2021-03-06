package com.dakotalal.timeapp.ui.Statistics;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dakotalal.timeapp.R;
import com.dakotalal.timeapp.room.entities.Day;
import com.dakotalal.timeapp.viewmodel.TimeViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyStatsFragment extends IntervalStatsFragment {
    ViewPager2 viewPager;
    TimeViewModel viewModel;
    public DailyStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interval_stats, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = requireView().findViewById(R.id.daily_stats_view_pager);
        viewPager.setAdapter(createStatsCollectionAdapter());
        viewModel = new ViewModelProvider(this).get(TimeViewModel.class);

        TabLayout tabLayout = requireView().findViewById(R.id.daily_stats_tab_layout);
        tabLayout.setTabMode(TabLayout.MODE_AUTO);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(" " + (position + 1))
        ).attach();

        // make sure today is loaded
        List<PieStatFragment> fragments = new ArrayList<>();
        fragments.add(PieStatFragment.newInstance(LocalDate.now().toEpochDay(), LocalDate.now().toEpochDay(), "Today"));
        adapter.setPieStatFragments(fragments);
        adapter.notifyDataSetChanged();

        // get the days and insert them into the adapter
        viewModel.getAllDays().observe(requireActivity(), new Observer<List<Day>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChanged(List<Day> days) {
                List<PieStatFragment> fragments = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                for (Day d: days) {
                    LocalDate date = d.getDate();
                    String label;
                    String dayOfWeek = date.getDayOfWeek().toString();
                    String year = Integer.toString(date.getYear());
                    String month = Integer.toString(date.getMonthValue());
                    String monthString = date.getMonth().toString();
                    String day = Integer.toString(date.getDayOfMonth());
                    fragments.add(PieStatFragment.newInstance(d.getDate().toEpochDay(), d.getDate().toEpochDay(), MessageFormat.format("{0} {1} {2}, {3}", dayOfWeek, monthString, day, year)));
                    if (date.isAfter(LocalDate.now().minusDays(7))) {
                        if (date.equals(LocalDate.now())) {
                            label = MessageFormat.format("Today ({0}/{1}/{2})", day, month, year);
                        } else if (date.equals(LocalDate.now().minusDays(1))) {
                            label = MessageFormat.format("Yesterday ({0}/{1}/{2})", day, month, year);
                        } else {
                            label = MessageFormat.format("{0} ({1}/{2}/{3})", dayOfWeek, day, month, year);
                        }
                    } else { // show only the date for anything further than a week back
                        label = MessageFormat.format("{0}/{1}/{2}", day, month, year);
                    }
                    labels.add(label);
                }
                adapter.setPieStatFragments(fragments);
                viewPager.setCurrentItem(fragments.size());
                adapter.notifyDataSetChanged();
                for (int i = 0; i < fragments.size(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null) {
                        tab.setText(labels.get(i));
                    }
                }

            }
        });
    }
}
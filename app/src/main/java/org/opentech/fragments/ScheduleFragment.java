package org.opentech.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.common.view.SlidingTabLayout;

import org.opentech.R;
import org.opentech.db.DatabaseManager;
import org.opentech.model.Day;

import java.util.ArrayList;


/**
 * Created by Abhishek on 24/02/15.
 */
public class ScheduleFragment extends Fragment {

    public final static String TAG = "ScheduleFragment";

    private DayLoader daysAdapter;
    private ViewHolder holder;
    private String track;

    public static Fragment newInstance(String track) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle bundle = new Bundle();
        bundle.putString("TRACK", track);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            track = getArguments().getString("TRACK");

            ArrayList<Day> staticDays = new ArrayList<>();
            staticDays.add(new Day(1, "May 14"));
            daysAdapter = new DayLoader(getChildFragmentManager(), track, staticDays);
        }


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        DatabaseManager db = DatabaseManager.getInstance();
        ArrayList<Day> days = db.getDates(track);

        String subTitle = "";
        for (Day day : days) {
            if (days.indexOf(day) != 0) {
                subTitle += ", ";
            }
            subTitle += day.getDate();

        }
        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(subTitle);
        holder = new ViewHolder();
        holder.contentView = view.findViewById(R.id.content);
        holder.emptyView = view.findViewById(android.R.id.empty);
        holder.pager = (ViewPager) view.findViewById(R.id.pager);
        holder.slidingTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        holder.contentView.setVisibility(View.VISIBLE);
        holder.emptyView.setVisibility(View.GONE);
        if (holder.pager.getAdapter() == null) {
            holder.pager.setAdapter(daysAdapter);
        }
        holder.slidingTabs.setViewPager(holder.pager);
        if (days.size() > 0) {
            String[] date = days.get(0).getDate().split(" ");
            int position = Integer.parseInt(date[1]) - 13;
            holder.pager.setCurrentItem(position);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holder = null;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directions:
                launchDirections();
                return true;
        }
        return false;
    }

    private void launchDirections() {

        DatabaseManager db = DatabaseManager.getInstance();

        String map = db.getTrackMapUrl(track);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(intent);
    }

    private static class ViewHolder {
        View contentView;
        View emptyView;
        ViewPager pager;
        SlidingTabLayout slidingTabs;
    }

    private static class DayLoader extends FragmentStatePagerAdapter {

        private ArrayList<String> mPageTitle;
        private ArrayList<Day> days;
        private String track;

        public DayLoader(FragmentManager fm, String track, ArrayList<Day> days) {
            super(fm);
            mPageTitle = new ArrayList<String>();
            this.track = track;
            this.days = days;

        }

        @Override
        public Fragment getItem(int position) {

            return ScheduleListFragment.newInstance(days.get(position).getDate(), track);
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return days.get(position).getDate();
        }


    }
}

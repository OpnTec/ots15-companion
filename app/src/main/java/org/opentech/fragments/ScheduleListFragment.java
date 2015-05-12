package org.opentech.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.opentech.activities.EventDetailsActivity;
import org.opentech.adapters.ScheduleAdapter;
import org.opentech.db.DatabaseManager;
import org.opentech.model.FossasiaEvent;

import java.util.ArrayList;

/**
 * Created by Abhishek on 20/02/15.
 */
public class ScheduleListFragment extends SmoothListFragment {

    private ArrayList<FossasiaEvent> events;
    private String track;

    public static Fragment newInstance(String day, String track) {
        Fragment fragment = new ScheduleListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("DAY", day);
        bundle.putString("TRACK", track);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String day = getArguments().getString("DAY");
        track = null;
        track = getArguments().getString("TRACK");
        DatabaseManager dbManager = DatabaseManager.getInstance();
        if (track != null) {
            events = dbManager.getEventsByDateandTrack(day, track);
        } else {
            events = dbManager.getEventsByDate(day);
        }
        setListAdapter(new ScheduleAdapter(getActivity(), events));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        long idNew = (long) v.getTag();
        Toast.makeText(getActivity(), "Position: " + idNew, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity().getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("event", events.get(position));
        DatabaseManager db = DatabaseManager.getInstance();
        String map = db.getTrackMapUrl(track);
;        intent.putExtra("MAP", map);
        startActivity(intent);
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("No sessions on this day");

    }
}

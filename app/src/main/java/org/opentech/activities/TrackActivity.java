package org.opentech.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.opentech.R;
import org.opentech.db.DatabaseManager;
import org.opentech.fragments.ScheduleFragment;

public class TrackActivity extends ActionBarActivity {

    private String track;
    private String map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        track = getIntent().getStringExtra("TRACK");

        getSupportFragmentManager().beginTransaction().replace(R.id.container, ScheduleFragment.newInstance(track), ScheduleFragment.TAG).addToBackStack(null).commit();
        setTitle("Tracks: " + track);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.directions) {
            //Get Map location from Database made from getting data from sheet.
            DatabaseManager db = DatabaseManager.getInstance();
            map = db.getTrackMapUrl(track);
            Log.d("TRACK ACTIVITY",map);

            if (!map.equals("") ){

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                startActivity(intent);
            }
            else{
                Snackbar.with(getApplicationContext())
                        .text("No Location specified")
                        .color(getResources().getColor(R.color.color_primary))
                        .show(this);
            }


            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

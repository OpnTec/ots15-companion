package org.opentech.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opentech.api.FossasiaUrls;
import org.opentech.model.FossasiaEvent;
import org.opentech.model.Speaker;
import org.opentech.model.Sponsor;
import org.opentech.model.Venue;
import org.opentech.utils.StringUtils;
import org.opentech.utils.VolleySingleton;

import java.util.ArrayList;

/**
 * Created by Abhishek on 17/02/15.
 */
public class JsonToDatabase {

    private final static String TAG = "JSON_TO_DATABASE";
    private final static String VERSION_DB = "VERSION";
    private Context context;
    private boolean tracks;
    private ArrayList<String> queries;
    private JsonToDatabaseCallback mCallback;
    private int count;
    private int version;
    private int default_version = 0;
    private int current_version;
    SharedPreferences version_database;
    SharedPreferences.Editor editor;

    public JsonToDatabase(Context context) {
        count = 0;
        this.context = context;
        queries = new ArrayList<String>();
        tracks = false;


    }

    public void setOnJsonToDatabaseCallback(JsonToDatabaseCallback callback) {
        this.mCallback = callback;
    }


    public void startDataDownload() {
        //fetchTracks(FossasiaUrls.TRACKS_URL);

        startTrackUrlFetch(FossasiaUrls.VERSION_TRACK_URL);
        SponsorUrl(FossasiaUrls.SPONSOR_URL);

    }

    //
    private void SponsorUrl(final String sponsorUrl) {
        RequestQueue queue = VolleySingleton.getReqQueue(context);

        //Request string reponse from the url

        StringRequest stringRequest = new StringRequest(sponsorUrl, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray jsonArray1 = removePaddingFromString(response);
                String name;
                String img;
                String url;
                Sponsor temp;

                for (int i = 1; i < jsonArray1.length(); i++) {

                    try {
                        name = jsonArray1.getJSONObject(i).getJSONArray("c").getJSONObject(0).getString("v");
                        img = jsonArray1.getJSONObject(i).getJSONArray("c").getJSONObject(1).getString("v");
                        url = jsonArray1.getJSONObject(i).getJSONArray("c").getJSONObject(2).getString("v");

                        temp = new Sponsor((i + 1), name, img, url);
                        String ab = temp.generatesql();
                        queries.add(ab);
                        Log.d(TAG, ab);
                    } catch (JSONException e) {

                        // Log.e(TAG, "JSON error: " + e.getMessage() + "\nResponse: " + response);

                    }
                }
            }
        }
                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //  Log.d(TAG, "VOLLEY ERROR :" + error.getMessage());

            }
        }

        );
        queue.add(stringRequest);
    }

    private void startTrackUrlFetch(String url) {


        RequestQueue queue = VolleySingleton.getReqQueue(context);
//        editor = version_database.edit();
//        editor.putInt(VERSION_DB, default_version);
//        editor.commit();
//        Log.d(TAG, version_database.getInt(VERSION_DB, default_version) + "");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
                String name;
                String url;
                String venue;
                String address;
                String howToReach;
                String link;
                String room;
                String mapLocation;
                String forceTrack;
                Venue temp;


                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        name = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        url = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("f");
                        venue = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(2)
                                .getString("v");
                        room = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(3)
                                .getString("v");
                        link = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(4)
                                .getString("v");
                        address = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(6)
                                .getString("v");
                        howToReach = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(7)
                                .getString("v");

                        version = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(8)
                                .getInt("v");

                        mapLocation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(5)
                                .getString("v");
                        String query = "INSERT INTO %s VALUES (%d, '%s', '%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_TRACK_VENUE, i, name, venue, mapLocation);
                        queries.add(query);
                        temp = new Venue(name, venue, mapLocation, room, link, address, howToReach);
                        queries.add(temp.generateSql());
                        version_database = context.getSharedPreferences(VERSION_DB, Context.MODE_PRIVATE);
                        current_version = version_database.getInt(VERSION_DB, default_version);
                        if (version != current_version) {
                            //Log.d(TAG, "datafetch" + current_version);
                            //Log.d(TAG, "version" + version);
                            DatabaseManager db = DatabaseManager.getInstance();
                            db.clearDatabase();
                            fetchData(FossasiaUrls.PART_URL + url, venue, name, (i + 50) * 100);
                            editor = version_database.edit();
                            editor.putInt(VERSION_DB, version);
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        //  Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }
                // Log.d(TAG + " STARTT", version + "");
                count--;
                checkStatus();

            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                count--;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        count++;
        fetchTracks(FossasiaUrls.TRACKS_URL);

        if (version != 1) {

        }
    }


    private void fetchData(String url, final String venue, final String forceTrack, final int id) {

        final RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);

                String firstName;
                String lastName;
                String time;
                String date;
                String organization;
                String email;
                String blog;
                String twitter;
                String typeOfProposal;
                String topicName;
                String field;
                String day;
                String proposalAbstract;
                String description;
                String url;
                String fullName;
                String linkedIn;
                String moderator;


                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        firstName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.FIRST_NAME)
                                .getString("v");
                        lastName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.LAST_NAME)
                                .getString("v");
                        time = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TIME)
                                .getString("f");
                        date = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.DATE)
                                .getString("v");
                        organization = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.ORGANIZATION)
                                .getString("v");
                        email = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.EMAIL)
                                .getString("v");
                        blog = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.BLOG)
                                .getString("v");
                        twitter = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TWITTER)
                                .getString("v");
                        typeOfProposal = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TYPE_OF_PROPOSAL)
                                .getString("v");
                        topicName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TOPIC_NAME)
                                .getString("v");
                        field = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TRACK)
                                .getString("v");
                        proposalAbstract = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.ABSTRACT)
                                .getString("v");
                        description = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.DESCRIPTION)
                                .getString("v");
                        url = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.URL)
                                .getString("v");
                        linkedIn = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.LINKEDIN)
                                .getString("v");
                        moderator = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.MODERATOR)
                                .getString("v");
//                        String logData = "First Name: %s\nLast Name: %s\nDate: %s\nTime: %s\nOrganization: %s\nEmail: %s\nBlog: %s\nTwitter: %s\nType Of Proposal: %s\nTopic Name:%s\nTrack: %s\nAbstarct: %s\nDescription: %s\nURL: %s";
//                        logData = String.format(logData, firstName, lastName, date, time, organization, email, blog, twitter, typeOfProposal, topicName, field, proposalAbstract, description, url);
                        int id2 = id + i;
                        if (date.equals("") || firstName.equals("") || time.equals("") || topicName.equals("")) {
                            continue;
                        }
                        String[] dayDate = date.split(" ");
                        day = dayDate[0];
                        date = dayDate[1] + " " + dayDate[2];
                        FossasiaEvent temp = new FossasiaEvent(id2, topicName, field, date, day, time, proposalAbstract, description, venue, forceTrack, moderator);

                        fullName = firstName + " " + lastName;
                        Speaker tempSpeaker = new Speaker(id2, fullName, "", linkedIn, twitter, organization, url, 0);
                        queries.add(tempSpeaker.generateSqlQuery());
                        queries.add(temp.generateSqlQuery());

                        String query = "INSERT INTO %s VALUES ('%s', %d, '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, fullName, id2, StringUtils.replaceUnicode(topicName));
                        queries.add(query);


                    } catch (JSONException e) {
                        //   Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }

                count--;
                checkStatus();

            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                count--;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        count++;

    }


    private void fetchTracks(String url) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
                String trackName;
                String trackInformation;

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        trackName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        trackInformation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("v");
                        String query = "INSERT INTO %s VALUES (%d, '%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_TRACK, i, StringUtils.replaceUnicode(trackName), StringUtils.replaceUnicode(trackInformation));
                        queries.add(query);
                    } catch (JSONException e) {
                        //  Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }
                tracks = true;
                checkStatus();
            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                tracks = true;
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void fetchSpeakerEventRelation(String url) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
                String speaker;
                String event;

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        speaker = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        event = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("v");
                        String query = "INSERT INTO %s VALUES ('%s', '%s');";
                        query = String.format(query, DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, speaker, event);
                        queries.add(query);
                    } catch (JSONException e) {
                        //      Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);
                    }

                }
                //                speakerEventRelation = true;
                checkStatus();
            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //                speakerEventRelation = true;
                checkStatus();
            }
        }

        );
        queue.add(stringRequest);
    }


    private void fetchKeySpeakers(String url) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = removePaddingFromString(response);
                String name;
                String designation;
                String profilePicUrl;
                String information;
                String twitterHandle;
                String linkedInUrl;
                int isKeySpeaker;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        name = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
                                .getString("v");
                        designation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
                                .getString("v");
                        information = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(2)
                                .getString("v");
                        twitterHandle = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(3)
                                .getString("v");
                        linkedInUrl = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(4)
                                .getString("v");
                        profilePicUrl = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(5)
                                .getString("v");
                        isKeySpeaker = (int) jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(6)
                                .getLong("v");
                        Speaker temp = new Speaker(i + 1, name, information, linkedInUrl, twitterHandle, designation, profilePicUrl, isKeySpeaker);
                        queries.add(temp.generateSqlQuery());
                    } catch (JSONException e) {
                        //         Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse: " + response);
                    }

                }
                checkStatus();
            }
        }

                , new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                checkStatus();
            }
        }

        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void checkStatus() {
        if (tracks && count == 0) {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            //Temporary clearing database for testing only
            dbManager.clearDatabase();
            dbManager.performInsertQueries(queries);
            if (version != 1) {

            }
            //Implement callbacks
            if (mCallback != null) {
                mCallback.onDataLoaded();
            }
        }
    }

    private JSONArray removePaddingFromString(String response) {
        response = response.replaceAll("\"v\":null", "\"v\":\"\"");
        response = response.replaceAll("null", "{\"v\": \"\"}");

        response = response.substring(response.indexOf("(") + 1, response.length() - 2);
        response = response.replaceAll("\"v\":\\bnew\\b(.*?)\\)", "\"v\":\"\"");
        try {

            JSONObject jObj = new JSONObject(response);
            jObj = jObj.getJSONObject("table");
            JSONArray jArray = jObj.getJSONArray("rows");
            return jArray;
        } catch (JSONException e) {
            // Log.e(TAG, "JSON Error: " + e.getMessage() + "\nResponse" + response);

        }

        return null;

    }

    public static interface JsonToDatabaseCallback {
        public void onDataLoaded();
    }


}

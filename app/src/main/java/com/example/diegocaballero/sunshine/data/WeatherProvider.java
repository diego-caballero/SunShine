package com.example.diegocaballero.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by admin on 9/9/14.
 */
public class WeatherProvider extends ContentProvider {
    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    public static UriMatcher matcher = buildUriMatcher();


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, WeatherContract.WEATHER_PATH, WEATHER);
        matcher.addURI(authority, WeatherContract.WEATHER_PATH + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.WEATHER_PATH + "/*" + "/*", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority, WeatherContract.LOCATION_PATH , LOCATION);
        matcher.addURI(authority, WeatherContract.LOCATION_PATH + "/#", LOCATION_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}

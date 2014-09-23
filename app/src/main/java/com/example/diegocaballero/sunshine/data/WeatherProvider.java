package com.example.diegocaballero.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
    private WeatherDBHelper helper;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ? ";

    private static final String sLocationSettingWithExactDate =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ? ";
    private Cursor getWeatherByLocationAndDate(Uri uri, String[] projection, String sortOrder){

        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (date == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, date};
            selection = sLocationSettingWithExactDate;
        }

        return sWeatherByLocationSettingQueryBuilder.query(helper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(helper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }



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

        helper = new WeatherDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int type = matcher.match(uri);
        Cursor cursor = null;
        switch (type){
            case WEATHER:{
                cursor = helper.getReadableDatabase().query(WeatherContract.WeatherEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION:{
                cursor = getWeatherByLocationSetting(uri,projection,sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION_AND_DATE:{
                cursor = getWeatherByLocationAndDate(uri, projection, sortOrder);
                break;
            }
            case LOCATION:{
                cursor = helper.getReadableDatabase().query(WeatherContract.LocationEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            case LOCATION_ID:{
                cursor = helper.getReadableDatabase().query(WeatherContract.LocationEntry.TABLE_NAME, projection,WeatherContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",null,null,null,sortOrder);
                break;

            }

        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int type = matcher.match(uri);

        switch (type){
            case WEATHER:                        return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:          return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE: return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;

            case LOCATION:                       return WeatherContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:                    return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Uknown URI: " + uri);
        }


    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        final int match = matcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;


            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        final int match = matcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case WEATHER:
                rowsDeleted = db.delete(
                        WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(
                        WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        final int match = matcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        final int match = matcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case WEATHER:
                rowsDeleted = db.update(
                        WeatherContract.WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.update(
                        WeatherContract.LocationEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
}

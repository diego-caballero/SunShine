package com.example.diegocaballero.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.diegocaballero.sunshine.data.WeatherContract;
import com.example.diegocaballero.sunshine.data.WeatherDBHelper;

/**
 * Created by admin on 9/9/14.
 */
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = AndroidTestCase.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDBHelper dbHelper = new WeatherDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = getLocationValues(testLocationSetting, testCityName, testLatitude, testLongitude);

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);


        Cursor cursor = queryLocationData(db);

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            validateLocationData(testLocationSetting, testCityName, testLatitude, testLongitude, cursor);


            // Fantastic.  Now that we have a location, add some weather!
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }
        ContentValues weatherValues = generateWeatherValues(locationRowId);

        long newRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);

        cursor = queryWeatherData(db);
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            int name = cursor.getInt(nameIndex);
            assertEquals(75, name);
        } else {
            fail("No data inserted");
        }
    }

    private Cursor queryWeatherData(SQLiteDatabase db) {
        Cursor cursor;
        String[] weatherColumns = {
                WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP

        };
        cursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,  // Table to Query
                weatherColumns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        return cursor;
    }

    private void validateLocationData(String testLocationSetting, String testCityName, double testLatitude, double testLongitude, Cursor cursor) {
        // Get the value in each column by finding the appropriate column index.
        int locationIndex = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
        String location = cursor.getString(locationIndex);

        int nameIndex = cursor.getColumnIndex((WeatherContract.LocationEntry.COLUMN_CITY_NAME));
        String name = cursor.getString(nameIndex);

        int latIndex = cursor.getColumnIndex((WeatherContract.LocationEntry.COLUMN_COORD_LAT));
        double latitude = cursor.getDouble(latIndex);

        int longIndex = cursor.getColumnIndex((WeatherContract.LocationEntry.COLUMN_COORD_LONG));
        double longitude = cursor.getDouble(longIndex);

        // Hooray, data was returned!  Assert that it's the right data, and that the database
        // creation code is working as intended.
        // Then take a break.  We both know that wasn't easy.
        assertEquals(testCityName, name);
        assertEquals(testLocationSetting, location);
        assertEquals(testLatitude, latitude);
        assertEquals(testLongitude, longitude);
    }

    private Cursor queryLocationData(SQLiteDatabase db) {
        String[] columns = {
                WeatherContract.LocationEntry._ID,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        return db.query(
                WeatherContract.LocationEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
    }

    private ContentValues getLocationValues(String testLocationSetting, String testCityName, double testLatitude, double testLongitude) {
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, testLongitude);
        return values;
    }

    private ContentValues generateWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }
}

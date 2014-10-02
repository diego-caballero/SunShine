package com.example.diegocaballero.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements Callback{
    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean tabletMode = false;
    private String currentDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null){
            tabletMode = true;


            DetailFragment fragment = new DetailFragment();
            if(savedInstanceState.getString("CURRENT_DATE",null)!= null){
                Bundle arguments = new Bundle();
                arguments.putString("CURRENT_DATE",savedInstanceState.getString("CURRENT_DATE"));
                fragment.setArguments(arguments);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, fragment).commit();

        }else
            tabletMode = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("CURRENT_DATE",this.currentDate);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will

        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.geolocation){
            this.openPreferredGeolocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }


    private void openPreferredGeolocationInMap(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = preferences.getString(this.getString(R.string.pref_location_key),this.getString(R.string.pref_location_default));
        Uri geolocation = Uri.parse("geo:0,0?q=" + location);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);
        if(intent.resolveActivity(getPackageManager())!= null){
            startActivity(intent);
        }else{
            Log.d(LOG_TAG,"Could not start activity");
        }
    }

    @Override
    public void onItemSelected(String date) {
        if (tabletMode){
            DetailFragment fragment = new DetailFragment();
            Bundle arguments = new Bundle();
            arguments.putString(DetailFragment.DATE_KEY,date);
            fragment.setArguments(arguments);
            String tag = null;
            this.currentDate = date;
            android.support.v4.app.FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.weather_detail_container,fragment);
            transaction.addToBackStack(tag);
            transaction.commit();
        }else{
            Intent intent = new Intent(this,DetailActivity.class);
            intent.putExtra(DetailActivity.DATE_KEY,date);
            startActivity(intent);
        }
    }
}

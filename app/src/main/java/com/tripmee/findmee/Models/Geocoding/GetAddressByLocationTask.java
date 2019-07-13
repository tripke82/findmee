package com.tripmee.findmee.Models.Geocoding;

import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.tripmee.findmee.Utilities.LocationUtility;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetAddressByLocationTask extends AsyncTask<Location, Void, List<Address>> {

    private Listener listener;
    public GetAddressByLocationTask(final Listener listener) {
        this.listener = listener;
    }
    @Override
    protected List<Address> doInBackground(Location... locations) {
        // Logic to download an image from an URL
        String TAG = "GetAddressByLocationTask";
        Location l = locations[0];
        List<Address> list = new ArrayList<>();

        try{

            list = LocationUtility.getStringFromLocation(l.getLatitude(),l.getLongitude());

            //list = geocoder.getFromLocation(UserHandler.CurrentUser.getLatitude(), UserHandler.CurrentUser.getLongitude(), 1);
        }catch (JSONException e){
            Log.e(TAG, "geoLocate: JSONException: " + e.getMessage() );
        }catch(IOException e){
            Log.e(TAG, "geoLocate: UnsupportedEncodingException: " + e.getMessage() );
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<Address> list) {
        if (null != list) {
            listener.onCompleted(list);
        } else {
            listener.OnError();
        }
    }

    public static interface Listener {
        void onCompleted(final List<Address> list);
        void OnError();
    }
}
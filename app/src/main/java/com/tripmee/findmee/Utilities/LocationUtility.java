package com.tripmee.findmee.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationUtility {

    public static LatLng getLocationFromString(String address)
            throws JSONException, UnsupportedEncodingException {

        HttpGet httpGet = new HttpGet(
                "http://maps.google.com/maps/api/geocode/json?address="
                        + URLEncoder.encode(address, "UTF-8") + "&ka&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lng");

        double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lat");

        return new LatLng(lat, lng);
    }

    public static List<Address> getStringFromLocation(double lat, double lng)
            throws ClientProtocolException, IOException, JSONException {

        String address = String
                .format(Locale.ENGLISH,"https://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&key=AIzaSyDoJ1ibS1tstLyWk3V61reYFiXrj2M21Zc&language="
                        + Locale.getDefault().getCountry(), lat, lng);
        HttpGet httpGet = new HttpGet(address);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        List<Address> retList = null;

        response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        int b;
        while ((b = stream.read()) != -1) {
            stringBuilder.append((char) b);
        }

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        retList = new ArrayList<Address>();

        if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
            JSONArray results = jsonObject.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String indiStr = result.getString("formatted_address");
                Address addr = new Address(Locale.getDefault());
                addr.setAddressLine(0, indiStr);
                retList.add(addr);
            }
        }

        return retList;
    }

    public static List<Address> getLocationsFromString(String address)
            throws JSONException, UnsupportedEncodingException {

        HttpGet httpGet = new HttpGet(
                "https://maps.google.com/maps/api/geocode/json?address="
                        + URLEncoder.encode(address, "UTF-8") + "&ka&sensor=false" + "&key=AIzaSyDoJ1ibS1tstLyWk3V61reYFiXrj2M21Zc");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        List<Address> retList = new ArrayList<Address>();

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
            JSONArray results = jsonObject.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String indiStr = result.getString("formatted_address");
                Address addr = new Address(Locale.getDefault());
                addr.setAddressLine(0, indiStr);
                //addr.setAdminArea();
                //addr.setCountryCode();
                //addr.setCountryName();

                if(result.has("geometry")) {

                    JSONObject geometry = (JSONObject) result.get("geometry");
                    if(geometry.has("location")){
                        JSONObject location = (JSONObject) geometry.get("location");

                        String lat = location.get("lat").toString();
                        String lng = location.get("lng").toString();

                        addr.setLatitude(Double.parseDouble(lat));
                        addr.setLongitude(Double.parseDouble(lng));
                        retList.add(addr);
                    }
                }
            }
        }

        return retList;
    }

    public static void drawMarker(Location location, GoogleMap googleMap, Activity activity, String markerText, String mark) {
        if (googleMap != null) {
            googleMap.clear();

            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            String MarkerText = markerText;

            googleMap.setMyLocationEnabled(true);
            //mGoogleMap.setMapType();
            UiSettings uiSettings = googleMap.getUiSettings();
            uiSettings.setAllGesturesEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setCompassEnabled(true);
            //uiSettings.setIndoorLevelPickerEnabled(true);
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(gps)
                    .title(MarkerText));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, GlobalConstants.DEFAULT_ZOOM));

        }

    }

    //** calculates the distance between two locations in MILES */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    public static boolean IsLocationChanged(Location l1, Location l2, double Miles){

        if(l1 == null){ // handle null obj
            if(l2!=null) {
                return true;
            }else{
                return false;
            }
        }

        if(l2 == null){
            if(l1!=null) {
                return true;
            }else{
                return false;
            }
        }

        if (distance(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude()) > Miles) { // if distance < 0.1 miles we take locations as equal
            return true;
        }
        return false;
    }

    public static Location get_CurrentLocation(Context _Context, Context context){

        Location currentLocation = null;
        LocationManager locationManager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
        Boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Boolean isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable){
            Utility.displayMessage(context, "Please enable your GPS or Network");
        }else {

            if (isNetworkEnable){

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0, (LocationListener) context);
                if (locationManager!=null){
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (currentLocation!=null){

                        //Log.e("LUtility latitude",currentLocation.getLatitude()+"");
                        //Log.e("LUtility longitude",currentLocation.getLongitude()+"");

                        return currentLocation;
                    }
                }

            }


            if (isGPSEnable){
                //currentLocation = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,(LocationListener) context);
                if (locationManager!=null){
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (currentLocation!=null){
                        Log.e("LUtility latitude",currentLocation.getLatitude()+"");
                        Log.e("LUtility longitude",currentLocation.getLongitude()+"");

                        return currentLocation;
                    }
                }
            }


        }
        return null;
    }

    public static String GetUrlByLocation(Double lat, Double lng){

        String slat = String.valueOf(lat);
        String slng = String.valueOf(lng);
        return "https://www.google.com/maps/@" + slat + "," + slng + "," + "16z";
    }

}

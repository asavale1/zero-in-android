package com.scott.martin.zero_in.helper;

import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.scott.martin.zero_in.BaseActivity;
import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.listener.GDirectionsListener;
import com.scott.martin.zero_in.server.GDirections;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ameya on 4/29/15.
 */
public class MapHelper {
    private GoogleMap map;
    private BaseActivity base;

    public MapHelper(BaseActivity base){
        this.base = base;
        map = ((MapFragment) base.getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
    }

    public void setMapMarker(LatLng origin){
        map.addMarker(new MarkerOptions().position(origin));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
    }

    public void directions(LatLng origin, LatLng dest){

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        System.out.println(url);

        map.addMarker(new MarkerOptions().position(origin));
        map.addMarker(new MarkerOptions().position(dest));
        new GDirections(gdListener, url).execute();

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);


    }

    GDirectionsListener gdListener = new GDirectionsListener() {
        @Override
        public void onGDirectionsComplete(String result) {
            new ParseRoute().execute(result);
        }
    };

    public void drawRoute(List<List<HashMap<String, String>>> routes){

        if(routes.size() > 0){
            for(int i = 2; i < routes.get(0).size()-1; i++){
                double originLat = Double.parseDouble(routes.get(0).get(i).get("lat"));
                double originLng = Double.parseDouble(routes.get(0).get(i).get("lng"));
                double endLat = Double.parseDouble(routes.get(0).get(i+1).get("lat"));
                double endLng = Double.parseDouble(routes.get(0).get(i+1).get("lng"));
                map.addPolyline(new PolylineOptions()
                        .add(new LatLng(originLat, originLng), new LatLng(endLat, endLng))
                        .width(5)
                        .color(Color.RED));
            }

        }else{
            System.out.println("Could not find a route");
            Toast.makeText(base, "Could not find any route", Toast.LENGTH_LONG).show();
        }
    }

    class ParseRoute extends AsyncTask<String, Void, List<List<HashMap<String, String>>>>{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String...params) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                JSONObject jsonObject = new JSONObject(params[0]);
                DirectionsHelper directionsHelper = new DirectionsHelper();
                routes = directionsHelper.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes){
            drawRoute(routes);
        }
    }

}

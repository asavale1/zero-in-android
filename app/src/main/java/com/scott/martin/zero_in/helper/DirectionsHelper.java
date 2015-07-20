package com.scott.martin.zero_in.helper;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ameya on 7/3/15.
 */
public class DirectionsHelper {

    public List<List<HashMap<String, String>>> parse(JSONObject jObject){
        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONObject jDistance = null;
        JSONObject jDuration = null;

        try{
            routes = new ArrayList<>();
            jRoutes = jObject.getJSONArray("routes");
            System.out.println("Routes length: " + jRoutes.length());
            for(int i = 0; i < jRoutes.length(); i++){
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<>();
                for(int j = 0; j < jLegs.length(); j++){
                    jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                    HashMap<String, String> hmDistance = new HashMap<>();
                    hmDistance.put("distance", jDistance.getString("text"));

                    jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                    HashMap<String, String> hmDuration = new HashMap<>();
                    hmDuration.put("duration", jDuration.getString("text"));

                    path.add(hmDistance);

                    path.add(hmDuration);

                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        System.out.println("POLYLINE " + k + ": " + polyline);
                        List<LatLng> list = this.decodePoly(polyline);

                        for (int l = 0; l < list.size(); l++)
                        {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                }
                routes.add(path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return routes;
    }

    private List<LatLng> decodePoly(String encoded)
    {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len)
        {
            int b, shift = 0, result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((lat / 1E5)), ((lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}

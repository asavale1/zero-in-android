package com.scott.martin.zero_in.helper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scott.martin.zero_in.BaseActivity;
import com.scott.martin.zero_in.R;

/**
 * Created by ameya on 4/29/15.
 */
public class MapHelper {
    private BaseActivity base;
    private GoogleMap map;

    public MapHelper(BaseActivity base){
        this.base = base;
        map = ((MapFragment) base.getFragmentManager().findFragmentById(R.id.map)).getMap();
    }

    public void setMapMarker(double longitude, double latitude){
        LatLng coordinates = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(coordinates));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
    }


}

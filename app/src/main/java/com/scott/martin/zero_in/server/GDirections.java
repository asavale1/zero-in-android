package com.scott.martin.zero_in.server;

import android.content.Context;
import android.os.AsyncTask;

import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.listener.GDirectionsListener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by ameya on 7/4/15.
 */
public class GDirections extends AsyncTask<String, Void, String> {
    GDirectionsListener listener;
    String url;

    public GDirections(GDirectionsListener listener, String url){
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            URL url = new URL(GDirections.this.url);//"http://192.168.1.9:3000/send_location");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            InputStream is = urlConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            //System.out.println("SERVER RESPONSE: " + response.toString());
            result = response.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result){
        listener.onGDirectionsComplete(result);
    }
}

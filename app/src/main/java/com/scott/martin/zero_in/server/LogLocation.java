package com.scott.martin.zero_in.server;

import android.content.Context;
import android.os.AsyncTask;

import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.listener.LogLocationListener;

import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by ameya on 6/27/15.
 */
public class LogLocation extends AsyncTask<String, Void, String> {

    String senderPhone, recipientPhone;
    double longitude, latitude;
    Context context;
    LogLocationListener listener;
    String type;

    public LogLocation(LogLocationListener listener, Context context, String senderPhone, String recipientPhone, double longitude, double latitude, String type){
        this.context = context;
        this.senderPhone = senderPhone;
        this.recipientPhone = recipientPhone.replaceAll("[^?0-9]+", "");
        this.longitude = longitude;
        this.latitude = latitude;
        this.listener = listener;
        this.type = type;
    }

    @Override
    protected String doInBackground(String... strings) {

        HttpURLConnection urlConnection = null;
        String result = "";

        try {
            URL url = new URL(context.getString(R.string.log_location_url));//"http://192.168.1.9:3000/send_location");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes("sender_phone=" + senderPhone +
                    "&recipient_phone=" + recipientPhone +
                    "&longitude=" + Double.toString(longitude) +
                    "&latitude=" + Double.toString(latitude));
            wr.flush();
            wr.close();

            InputStream is = urlConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            System.out.println("SERVER RESPONSE: " + response.toString());
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
        boolean logged = false;
        try {
            JSONObject jsonObj = new JSONObject(result);
            logged = jsonObj.getBoolean("logged");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listener.onLocationLogged(type);

    }
}

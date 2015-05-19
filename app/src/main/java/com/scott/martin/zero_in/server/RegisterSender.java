package com.scott.martin.zero_in.server;

import android.content.Context;
import android.os.AsyncTask;

import com.scott.martin.zero_in.R;

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
 * Created by ameya on 4/25/15.
 */
public class RegisterSender extends AsyncTask<String, Void, Boolean> {
    //private static final String REGISTER_URL = "http://aqueous-meadow-1911.herokuapp.com/register_sender";
    //private static final String REGISTER_URL = "http://192.168.1.9:3000/register_sender";

    String phoneNoCC, phoneWithCC;
    String regid;
    private Context context;

    public RegisterSender(Context context, String phoneNoCC, String phoneWithCC, String regid){
        this.phoneNoCC = phoneNoCC;
        this.phoneWithCC = phoneWithCC;
        this.regid = regid;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(context.getString(R.string.register_url));//"http://192.168.1.9:3000/register_sender");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes("phone_no_cc=" + phoneNoCC +
                    "&phone_with_cc=" + phoneWithCC +
                    "&gcm_regid=" + regid);
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

            System.out.println("Server Response: " + response.toString());


        }catch(MalformedURLException e){
            System.out.println("MalformedURLException: " + e.getMessage());
        }catch(ProtocolException e){
            System.out.println("ProtocolException: " + e.getMessage());
        }catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return null;
    }
}


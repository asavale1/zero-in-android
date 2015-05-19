package com.scott.martin.zero_in.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;

import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.listener.SendLocationListener;

import org.json.JSONException;
import org.json.JSONObject;

public class SendLocation extends AsyncTask<String, Void, String> {
	//private static final String SEND_LOCATION_URL = "http://aqueous-meadow-1911.herokuapp.com/send_location";
	//private static final String SEND_LOCATION_URL = "http://192.168.1.9:3000/send_location";
	String senderPhone, recipientPhone;
	SendLocationListener listener;
	double longitude;
	double latitude;
	private Context context;
		
	public SendLocation(Context context, String senderPhone, String recipientPhone, double longitude, double latitude, SendLocationListener listener){
		this.recipientPhone = recipientPhone;
		this.senderPhone = senderPhone;
		this.longitude = longitude;
		this.latitude = latitude;
		this.listener = listener;
		this.context = context;
	}
	
	@Override
	protected String doInBackground(String... params) {
		HttpURLConnection urlConnection = null;
		String result = "";

		try {
			URL url = new URL(context.getString(R.string.send_location_url));//"http://192.168.1.9:3000/send_location");
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
		boolean validPush = false;
		try {
			JSONObject jsonObj = new JSONObject(result);
			validPush = jsonObj.getBoolean("has_account");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		listener.onSendComplete(validPush);

	}

}

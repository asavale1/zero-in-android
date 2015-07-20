package com.scott.martin.zero_in;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.scott.martin.zero_in.adapter.ContactsAdapter;
import com.scott.martin.zero_in.helper.ContactsHelper;
import com.scott.martin.zero_in.helper.DirectionsHelper;
import com.scott.martin.zero_in.helper.MapHelper;
import com.scott.martin.zero_in.helper.SendHelper;
import com.scott.martin.zero_in.listener.CheckAccountListener;
import com.scott.martin.zero_in.model.Contact;
import com.scott.martin.zero_in.server.CheckAccount;
import com.scott.martin.zero_in.server.GCMRegister;
import com.scott.martin.zero_in.server.RegisterSender;
import com.scott.martin.zero_in.listener.GCMRegisterListener;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;

public class BaseActivity extends ActionBarActivity implements GCMRegisterListener {
    public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_PHONE_NO_CC = "phone_no_cc";
	public static final String PROPERTY_PHONE_WITH_CC = "phone_with_cc";
    private static final String PROPERTY_APP_VERSION = "1.0";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


	private String senderPhoneNoCC, senderPhoneWithCC;
	private double userLong, userLat;
	private double receivedLong, receivedLat;
	private Context context;
	private String recipientPhone = "", recipientName = "";
	private ArrayList<Contact> contacts;
	private GoogleApiClient googleApiClient;
	private MapHelper mapHelper;
	private ContactsHelper contactsHelper;
	private Contact selectedContact;

	private boolean showDirections = false;

	private Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		context = this;

		dialog = new Dialog(this);
		//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		contactsHelper = new ContactsHelper(this);

		if(checkPlayServices()) {

			mapHelper = new MapHelper(this);
			Intent intent = getIntent();
			if (intent.hasExtra("sender")) {
				receivedLong = Double.parseDouble(intent.getStringExtra("longitude"));
				receivedLat = Double.parseDouble(intent.getStringExtra("latitude"));
				showDirections = true;
			}else{
				showDirections = false;
			}

			buildGoogleApiClient();

			//LatLng origin = new LatLng(37.305310, -122.041747);
			//LatLng dest = new LatLng(37.315292, -122.056603);
			//mapHelper.directions(origin, dest);



			String regid = getSharedPrefernces(PROPERTY_REG_ID);

			contacts = contactsHelper.getContacts();

			senderPhoneWithCC = getSharedPrefernces(PROPERTY_PHONE_WITH_CC);
			if (senderPhoneWithCC.isEmpty()) {
				getSenderPhoneNumber();
			} else {
				senderPhoneNoCC = getSharedPrefernces(PROPERTY_PHONE_NO_CC);
			}

			Button addRecipient = (Button) findViewById(R.id.add_recipient);
			addRecipient.setOnClickListener(addRecipientListener);

			if (regid.isEmpty()) {
				System.out.println("GCM Register");
				new GCMRegister(this, this).execute();
			} else {
				new RegisterSender(context, senderPhoneNoCC, senderPhoneWithCC, regid).execute();
			}
		}
	}

	/**
	 * Sets sender phone number with and without country code
	 */
	private void getSenderPhoneNumber(){
		TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String tempPhone = tMgr.getLine1Number();

		if(tempPhone == null){
			dialog.setContentView(R.layout.dialog_phonenumber);
			dialog.show();
		}else{
			String countryID = tMgr.getSimCountryIso().toUpperCase();
			String senderCC = "";

			String[] rl=this.getResources().getStringArray(R.array.country_codes);
			for(int i=0;i<rl.length;i++){
				String[] g=rl[i].split(",");
				if(g[1].trim().equals(countryID.trim())){
					senderCC=g[0];
					break;
				}
			}

			senderPhoneNoCC = tempPhone.replaceFirst(senderCC, "");
			senderPhoneWithCC = tempPhone;

			setSharedPreferences(PROPERTY_PHONE_WITH_CC, senderPhoneWithCC);
			setSharedPreferences(PROPERTY_PHONE_NO_CC, senderPhoneNoCC);

		}

	}

	View.OnClickListener addRecipientListener = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			dialog.setContentView(R.layout.dialog_contacts);
			dialog.setTitle("Contacts");

			ListView contactList = (ListView) dialog.findViewById(R.id.contacts_list);

			contactList.setAdapter(new ContactsAdapter(context, contacts));
			contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					recipientName = contacts.get(position).getName();
					recipientPhone = contacts.get(position).getPhone();
					selectedContact = contacts.get(position);
					new CheckAccount(context, recipientPhone, checkAccountListener).execute();
				}
			});
			dialog.show();
		}
	};

	CheckAccountListener checkAccountListener = new CheckAccountListener() {
		@Override
		public void onCheckComplete(boolean hasAccount) {

			if(hasAccount){
				System.out.println("HAS ACCOUNT");
				SendHelper sendHelper = new SendHelper(context, recipientName, recipientPhone, senderPhoneWithCC, userLat, userLong);
				sendHelper.showCountDown(true);
			}else{
				System.out.println("NO ACCOUNT");
				SendHelper sendHelper = new SendHelper(context, recipientName, recipientPhone, senderPhoneWithCC, userLat, userLong);
				sendHelper.displaySendMethod(selectedContact);
			}
		}
	};

	public String getSharedPrefernces(String key){
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getString(key, "");
	}

	public void setSharedPreferences(String key, String value){
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	protected synchronized void buildGoogleApiClient() {
		System.out.println("Building Google Api Client");
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(connectionCB)
				.addOnConnectionFailedListener(onConnectionFailedListener)
				.addApi(LocationServices.API)
				.build();
	}

	GoogleApiClient.ConnectionCallbacks connectionCB = new GoogleApiClient.ConnectionCallbacks() {
		@Override
		public void onConnected(Bundle bundle) {
			Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
			if (lastLocation != null) {
				userLat = lastLocation.getLatitude();
				userLong = lastLocation.getLongitude();

			}else{
				userLong = 86.922623;
				userLat = 27.986065;
			}

			System.out.println("Sender longitude: " + userLong);
			System.out.println("Sender Latitude: " + userLat);

			if(showDirections){
				LatLng origin = new LatLng(userLat, userLong);
				LatLng dest = new LatLng(receivedLat, receivedLong);
				//mapHelper.setMapMarker(senderLong, senderLat);
				mapHelper.directions(origin, dest);
			}else{
				LatLng origin = new LatLng(userLat, userLong);
				mapHelper.setMapMarker(origin);
			}
		}

		@Override
		public void onConnectionSuspended(int i) {
			System.out.println("Connection suspended");
		}
	};

	GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			System.out.println("Connection Failed");
		}
	};

	@Override
	public void onGCMRegisterComplete(String result) {
		new RegisterSender(context, senderPhoneNoCC, senderPhoneWithCC, result).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.base, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				finish();
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onStart(){
		super.onStart();
		if(checkPlayServices()){ googleApiClient.connect(); }
		contacts = contactsHelper.getContacts();
	}

	@Override
	protected  void onStop(){
		if(checkPlayServices()){ googleApiClient.disconnect(); }
		super.onStop();
	}


}

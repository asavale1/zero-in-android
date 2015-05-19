package com.scott.martin.zero_in;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.scott.martin.zero_in.adapter.ContactsAdapter;
import com.scott.martin.zero_in.helper.MapHelper;
import com.scott.martin.zero_in.model.Contact;
import com.scott.martin.zero_in.server.GCMRegister;
import com.scott.martin.zero_in.server.RegisterSender;
import com.scott.martin.zero_in.server.SendLocation;
import com.scott.martin.zero_in.listener.GCMRegisterListener;
import com.scott.martin.zero_in.listener.SendLocationListener;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BaseActivity extends ActionBarActivity implements GCMRegisterListener {
    public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_PHONE_NO_CC = "phone_no_cc";
	public static final String PROPERTY_PHONE_WITH_CC = "phone_with_cc";
    private static final String PROPERTY_APP_VERSION = "1.0";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


	private String senderPhoneNoCC, senderPhoneWithCC;
	private double senderLong, senderLat;
	private Context context;
	private String recipientPhone = "", recipientName = "";
	private ArrayList<Contact> contacts;
	private GoogleApiClient googleApiClient;
	private MapHelper mapHelper;

	private boolean receiverMarkerSet = false;
	private TextView recipientView;
	private Handler mHandler = new Handler();

	private Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		context = this;

		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		recipientView = (TextView) findViewById(R.id.recipient);

		mapHelper = new MapHelper(this);
		Intent intent = getIntent();
		if(intent.hasExtra("sender")){
			Double receivedLong = Double.parseDouble(intent.getStringExtra("longitude"));
			Double receivedLat = Double.parseDouble(intent.getStringExtra("latitude"));
			mapHelper.setMapMarker(receivedLong, receivedLat);
			receiverMarkerSet = true;
		}

		buildGoogleApiClient();

		String regid = getSharedPrefernces(PROPERTY_REG_ID);


		contacts = queryContacts();

		senderPhoneWithCC = getSharedPrefernces(PROPERTY_PHONE_WITH_CC);
		if(senderPhoneWithCC.isEmpty()){
			getSenderPhoneNumber();
		}else{
			senderPhoneNoCC = getSharedPrefernces(PROPERTY_PHONE_NO_CC);
		}

		Button sendLocation = (Button) findViewById(R.id.send_location);
		sendLocation.setOnClickListener(sendLocationListener);

		Button addRecipient = (Button) findViewById(R.id.add_recipient);
		addRecipient.setOnClickListener(addRecipientListener);

		if(regid.isEmpty()){
			System.out.println("GCM Register");
			new GCMRegister(this, this).execute();
		}else{
			new RegisterSender(context, senderPhoneNoCC, senderPhoneWithCC, regid).execute();
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
			final Dialog dialog = new Dialog(context);
			dialog.setContentView(R.layout.dialog_contacts);
			dialog.setTitle("Contacts");

			ListView contactList = (ListView) dialog.findViewById(R.id.contacts_list);

			contactList.setAdapter(new ContactsAdapter(context, contacts));
			contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					recipientName = contacts.get(position).getName();
					recipientPhone = contacts.get(position).getPhone();

					recipientView.setText("Send to...\n" + recipientName);

					dialog.dismiss();
				}
			});
			dialog.show();
		}
	};

	private ArrayList<Contact> queryContacts(){
		ArrayList<Contact> contacts = new ArrayList<Contact>();

		ContentResolver cr = this.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		if(cursor.getCount() > 0){
			while (cursor.moveToNext()) {
				Contact contact = new Contact();

				String id = cursor.getString(
						cursor.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cursor.getString(
						cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				contact.setName(name);

				if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
							new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phonenumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						contact.setPhone(phonenumber);
					}
					pCur.close();

					contacts.add(contact);
				}
			}
		}
		cursor.close();

		return contacts;
	}

	View.OnClickListener sendLocationListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(recipientPhone.isEmpty()){
				Toast.makeText(getApplicationContext(), getString(R.string.recipient_error),
						Toast.LENGTH_LONG).show();
			}else{

				recipientPhone = recipientPhone.replaceAll("[^?0-9]+", "");

				dialog.setContentView(R.layout.dialog_progress);
				TextView status = (TextView) dialog.findViewById(R.id.status);
				status.setText("Sending location...");
				ProgressBar spinner = (ProgressBar) dialog.findViewById(R.id.progress_bar);
				spinner.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
				dialog.show();

				new SendLocation(context, senderPhoneWithCC, recipientPhone, senderLong, senderLat, sendCompleteListener).execute();

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

	SendLocationListener sendCompleteListener = new SendLocationListener() {
		@Override
		public void onSendComplete(boolean validPush) {
			if(!validPush){
				SmsManager smsManager = SmsManager.getDefault();
				String message = "Has shared their location\nhttps://maps.google.com/maps?q="+ senderLat + "," + senderLong;
				smsManager.sendTextMessage(recipientPhone, null, message, null, null);
			}

			TextView status = (TextView) dialog.findViewById(R.id.status);
			ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.progress_bar);
			progress.setVisibility(View.INVISIBLE);


			status.setText("Location sent :)");

			mHandler.postDelayed(new Runnable() {
				public void run() {
					dialog.dismiss();
				}
			}, 2000);

			recipientPhone = "";
			recipientName = "";
			recipientView.setText("Send to...");
		}
	};

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
				senderLat = lastLocation.getLatitude();
				senderLong = lastLocation.getLongitude();


			}else{
				senderLong = 86.922623;
				senderLat = 27.986065;
			}

			if(!receiverMarkerSet){
				mapHelper.setMapMarker(senderLong, senderLat);
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
		if(checkPlayServices()){
			googleApiClient.connect();
		}
		contacts = queryContacts();
	}

	@Override
	protected  void onStop(){
		if(checkPlayServices()){
			googleApiClient.disconnect();
		}
		super.onStop();
	}


}

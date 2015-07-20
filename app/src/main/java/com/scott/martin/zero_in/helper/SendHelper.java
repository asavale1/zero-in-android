package com.scott.martin.zero_in.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.scott.martin.zero_in.BaseActivity;
import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.listener.LogLocationListener;
import com.scott.martin.zero_in.listener.SendLocationListener;
import com.scott.martin.zero_in.model.Contact;
import com.scott.martin.zero_in.server.LogLocation;
import com.scott.martin.zero_in.server.SendLocation;

import java.util.ArrayList;

/**
 * Created by ameya on 6/26/15.
 */
public class SendHelper {
    Dialog dialog;
    private String recipientName, recipientPhone, senderPhoneWithCC;
    private double senderLat, senderLong;
    public Context context;

    public SendHelper( Context context,
                      String recipientName, String recipientPhone, String senderPhoneWithCC,
                      double senderLat, double senderLong){

        this.context = context;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone.replaceAll("[^?0-9]+", "");
        this.senderPhoneWithCC = senderPhoneWithCC;

        this.senderLat = senderLat;
        this.senderLong = senderLong;

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        System.out.println(recipientName);
        System.out.println(recipientPhone);
        System.out.println(senderPhoneWithCC);
        System.out.println(senderLat);
        System.out.println(senderLong);
    }

    public void showCountDown(final boolean hasAccount){
        dialog.setContentView(R.layout.dialog_cancel_send);

        TextView dialogTitle = (TextView) dialog.findViewById(R.id.title);
        dialogTitle.setText("Location will be sent to\n" + recipientName + " in...");

        final TextView countdown = (TextView) dialog.findViewById(R.id.countdown);
        Button cancelSend = (Button) dialog.findViewById(R.id.cancel_send);

        dialog.show();

        final CountDownTimer timer = new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdown.setText(Long.toString(millisUntilFinished / 1000));
            }

            public void onFinish() {
                System.out.println("COuntdown complete");
                dialog.dismiss();
                sendLocation(hasAccount);
            }
        };
        timer.start();

        cancelSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
                timer.cancel();
            }
        });
    }

    public void showLocationSent(){
        dialog.setContentView(R.layout.dialog_location_sent);
        dialog.show();
        final CountDownTimer timer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                dialog.dismiss();
            }
        };
        timer.start();
    }

    private void sendLocation(boolean hasAccount){

        dialog.setContentView(R.layout.dialog_progress);
        TextView status = (TextView) dialog.findViewById(R.id.status);
        status.setText("Sending location...");
        ProgressBar spinner = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        spinner.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

        dialog.show();

        if(hasAccount){

            new SendLocation(context, senderPhoneWithCC, recipientPhone, senderLong, senderLat, sendLocationListener).execute();
        }else{
            SmsManager smsManager = SmsManager.getDefault();
            //String message = "Has shared their location\nhttps://maps.google.com/maps?q="+ senderLat + "," + senderLong;
            String message = "Has shared their location\nhttps://maps.google.com/maps?directionsmode=driving&dirflg=d&daddr="+ senderLat + "+" + senderLong+"&hl=en";

            smsManager.sendTextMessage(recipientPhone, null, message, null, null);

            showLocationSent();
        }

    }

    SendLocationListener sendLocationListener = new SendLocationListener(){

        @Override
        public void onSendComplete(boolean validPush) {
            showLocationSent();
        }
    };

    public void displaySendMethod(Contact contact){
        dialog.setContentView(R.layout.dialog_send_method);
        Button messaging = (Button) dialog.findViewById(R.id.messaging);
        messaging.setOnClickListener(messageClickListener);

        Button whatsapp = (Button) dialog.findViewById(R.id.whatsapp);
        whatsapp.setOnClickListener(whatsappClickListener);

        dialog.show();
        ArrayList<String> types = contact.getTypes();
        for(int i = 0; i < types.size(); i++){
            if(types.get(i).equals(context.getString(R.string.send_via_message))){
                messaging.setVisibility(View.VISIBLE);
            }else if(types.get(i).equals(context.getString(R.string.send_via_whatsapp))){
                whatsapp.setVisibility(View.VISIBLE);
            }
        }
    }

    View.OnClickListener messageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("MESSAGE CLICK");
            dialog.dismiss();
            new LogLocation(logLocationListener, context, senderPhoneWithCC, recipientPhone, senderLong, senderLat, context.getString(R.string.send_via_message)).execute();

        }
    };

    View.OnClickListener whatsappClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            System.out.println("WHATS APP CLICK");
            dialog.dismiss();
            new LogLocation(logLocationListener, context, senderPhoneWithCC, recipientPhone, senderLong, senderLat, context.getString(R.string.send_via_whatsapp)).execute();
        }
    };

    LogLocationListener logLocationListener = new LogLocationListener() {
        @Override
        public void onLocationLogged(String type) {
            if(type.equals(context.getString(R.string.send_via_message))){
                sendViaMessaging();
            }else if(type.equals(context.getString(R.string.send_via_whatsapp))){
                sendViaWhatsApp();
            }
        }
    };

    private void sendViaMessaging(){ showCountDown(false); }

    private void sendViaWhatsApp(){
        PackageManager pm = context.getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String message = "Has shared their location\nhttps://maps.google.com/maps?directionsmode=driving&dirflg=d&daddr="+ senderLat + "+" + senderLong+"&hl=en";

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            context.startActivity(Intent.createChooser(waIntent, "Send location to"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

        //System.out.println("SENT VIA WHATSAPP");
    }

}

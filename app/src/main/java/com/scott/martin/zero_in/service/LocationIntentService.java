package com.scott.martin.zero_in.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.scott.martin.zero_in.BaseActivity;
import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.receiver.LocationBroadcastReceiver;

/**
 * Created by ameya on 4/26/15.
 */
public class LocationIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager nm;


    public LocationIntentService(){
        super("LocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if(!extras.isEmpty()){
            if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
                System.out.println("Send error: " + extras.toString());
            }else if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
                System.out.println("Deleted messages on server: " + extras.toString() );
            }else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
                System.out.println("Did some work: " + extras.toString());
                sendNotification(extras);
            }
        }

        LocationBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle extras){
        nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        System.out.println(extras.toString());

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
        nb.setSmallIcon(R.drawable.logox96);
        nb.setContentTitle("Find Me Notification");
        nb.setStyle(new NotificationCompat.BigTextStyle().bigText("Location received"));

        nb.setContentText("Location received " + intent.getStringExtra("sender"));
        nb.setAutoCancel(true);



        nb.setContentIntent(pendingIntent);
        nm.notify(NOTIFICATION_ID, nb.build());
    }
}

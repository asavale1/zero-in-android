package com.scott.martin.zero_in.server;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.scott.martin.zero_in.BaseActivity;
import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.listener.GCMRegisterListener;

import java.io.IOException;

/**
 * Created by ameya on 4/26/15.
 */
public class GCMRegister extends AsyncTask<String, Void, String> {
    private GCMRegisterListener listener;
    private BaseActivity base;

    public GCMRegister(Context context, GCMRegisterListener listener){
        this.listener = listener;
        this.base = (BaseActivity) context;
    }

    @Override
    protected String doInBackground(String... params) {
        String regid = "";
        if(base.checkPlayServices()){
            regid = base.getSharedPrefernces(base.getString(R.string.property_reg_id));
            if(regid.isEmpty()) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(base);

                try {
                    regid = gcm.register(base.getString(R.string.gcm_sender_id));
                    base.setSharedPreferences(base.getString(R.string.property_reg_id), regid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return regid;
    }

    @Override
    protected void onPostExecute(String result) {
        listener.onGCMRegisterComplete(result);
    }


}

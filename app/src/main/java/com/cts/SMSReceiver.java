package com.cts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

    SharedPreferences sp;

    public SMSReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (Object aPdusObj : pdusObj) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i(AppConfig.TAG, "senderNum: " + phoneNumber + "; message: " + message);

                    // Save New Co-ordinates
                    if (phoneNumber.equals(AppConfig.RX_MOBILE_NUMBER)) {
                        sp = context.getSharedPreferences(AppConfig.TAG, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        if (message.contains(",")) {
                            String[] strs = message.split(",");
                            Float lat = Float.parseFloat(strs[0]);
                            Float lng = Float.parseFloat(strs[1]);
                            editor.putFloat("chLat", lat);
                            editor.putFloat("chLng", lng);
                            editor.commit();
                            Log.d(AppConfig.TAG, "New Child Location (" + lat + ", " + lng + ") saved.");
                            MapsActivity.updateLocation(lat, lng);
                        } else {
                            Log.e(AppConfig.TAG, "Wrong Message format.");
                        }
                    } else {
                        Log.e(AppConfig.TAG, "Message from another number.");
                    }
                    // Show Alert
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "senderNum: " + phoneNumber + ", message: " + message, duration);
                    toast.show();

                }
            } else {
                Log.e(AppConfig.TAG, "Null Bundle");
            }
        } catch (Exception e) {
            Log.e(AppConfig.TAG, "Exception smsReceiver" + e);

        }
    }
}

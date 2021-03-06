package com.rahul.otpfetch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsListener {

    static final String SMS_INTENT_ACTION = "SMS_INTENT_ACTION";
    static final String TAG_MESSAGE = "TAG_MESSAGE";
    static final String TAG_SUCCESS = "TAG_SUCCESS";

    private final Context context;
    private final SmsResponseHandler handler;
    private final int digits;
    private BroadcastReceiver receiver;

    public SmsListener(Context context, SmsResponseHandler handler, int digits) {
        this.context = context;
        this.handler = handler;
        this.digits = digits;
    }

    public void startService() {

        SmsRetrieverClient client = SmsRetriever.getClient(context);

        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //Registering the Broadcast receiver once the Task has started.
                registerBroadcastReceiver();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //If the Task could not start, will throw error.
                handler.failureToStartService(e);
            }
        });
    }

    private void registerBroadcastReceiver() {

        IntentFilter intentFilter = new IntentFilter(SMS_INTENT_ACTION);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getBooleanExtra(TAG_SUCCESS, false)) {
                    String retrievedText = intent.getStringExtra(TAG_MESSAGE);

                    handleResponse(retrievedText);
                    return;
                }

                handler.requestTimedOut();
            }
        };

        context.registerReceiver(receiver, intentFilter);
    }

    private void handleResponse(String retrievedText) {

        if (digits > 0) {
            String otp = getOtpFromSms(retrievedText);
            if (!TextUtils.isEmpty(otp)) {
                handler.otpResponse(otp);
            }
        }

        handler.smsResponse(retrievedText);
    }

    private String getOtpFromSms(String retrievedText) {

        String regex = "(\\d{" + digits + "})";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(retrievedText);

        String val = "";
        if (matcher.find()) {
            val = matcher.group(0);
        }

        return val;
    }

    /**
     * Call this method on onDestroy so that Broadcast Receiver can be unregistered
     */
    public void stopService() {
        context.unregisterReceiver(receiver);
    }
}

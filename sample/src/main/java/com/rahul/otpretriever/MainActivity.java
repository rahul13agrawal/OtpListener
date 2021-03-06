package com.rahul.otpretriever;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rahul.otpfetch.SmsListener;
import com.rahul.otpfetch.SmsResponseHandler;

public class MainActivity extends AppCompatActivity implements SmsResponseHandler {

    SmsListener smsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsListener = new SmsListener(this, this, 4);
        smsListener.startService();
    }

    @Override
    public void failureToStartService(Exception e) {
        Log.d("MainActivity", "failureToStartService: " + e.getMessage());
    }

    @Override
    public void smsResponse(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (smsListener != null) {
            smsListener.stopService();
        }
    }

    @Override
    public void requestTimedOut() {
        Toast.makeText(this, "TimedOut", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void otpResponse(String otp) {
        //Fetch otp from the Sms
        Log.d("MainActivity", "otpResponse: " + otp);
    }
}

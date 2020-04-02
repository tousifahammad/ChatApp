package com.bibhas.handsapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bibhas.handsapp.Util.SharedData;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.calling.CallClientListener;

import java.util.List;

public class SinchActivity extends AppCompatActivity {

    private final static String TAG = "=== SinchActivity ===";

    private Button voice_call, video_call, stop_call;
    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private Button button;
    private String callerId;
    private String recipientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sinch);

        // ============ init ==============
        voice_call = findViewById(R.id.voice_call);
        button = findViewById(R.id.video_call);
        stop_call = findViewById(R.id.stop_calling);
        callState = findViewById(R.id.callState);

        Intent intent = getIntent();
        callerId = intent.getStringExtra("callerId");
        recipientId = intent.getStringExtra("recipientId");

        if (ContextCompat.checkSelfPermission(SinchActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(SinchActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SinchActivity.this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.READ_PHONE_STATE},
                    1);
        }

        // Instantiate a SinchClient using the SinchClientBuilder.
        android.content.Context context = getApplicationContext();
        sinchClient = Sinch.getSinchClientBuilder().context(context)
                .applicationKey(SharedData.APP_KEY)
                .applicationSecret(SharedData.APP_SECRET)
                .environmentHost(SharedData.HOST_NAME)
                .userId(callerId)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

//        sinchClient.addSinchClientListener(new SinchClientListener() {
//            public void onClientStarted(SinchClient client) { }
//            public void onClientStopped(SinchClient client) { }
//            public void onClientFailed(SinchClient client, SinchError error) { }
//            public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) { }
//            public void onLogMessage(int level, String area, String message) { }
//        });
//        sinchClient.start();


        voice_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (call == null) {
                        call = sinchClient.getCallClient().callUser(recipientId);
                        call.addCallListener(new SinchCallListener());
                        button.setText("Hang Up");
                    } else {
                        call.hangup();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (call == null) {
                        call = sinchClient.getCallClient().callUserVideo(recipientId);
                        call.addCallListener(new SinchCallListener());
                        button.setText("Hang Up");
                    } else {
                        call.hangup();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }

            }
        });

        stop_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sinchClient.isStarted()) {
                    sinchClient.stopListeningOnActiveConnection();
                    sinchClient.terminate();
                }
            }
        });
    }


    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            SinchError a = endedCall.getDetails().getError();
            button.setText("Call");
            callState.setText("on Call Ended");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            callState.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            callState.setText("ringing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            Toast.makeText(SinchActivity.this, "incoming call", Toast.LENGTH_SHORT).show();
            call.answer();
            call.addCallListener(new SinchCallListener());
            button.setText("Hang Up");
        }
    }
}

package com.bibhas.handsapp.Activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bibhas.handsapp.MainActivity;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Services.BaseActivity;
import com.bibhas.handsapp.Services.SinchService;
import com.google.firebase.auth.FirebaseAuth;
import com.sinch.android.rtc.SinchError;

public class SplashActivity extends BaseActivity implements SinchService.StartFailedListener {
    private final static String TAG = "==SplashActivity==";
    private static int SPLASH_TIME = 2000; //This is 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Code to start timer and take action after the timer ends
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSinchService();
            }
        }, SPLASH_TIME);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            if (getSinchServiceInterface() != null) {
//                if (!getSinchServiceInterface().isStarted()) {
//                    getSinchServiceInterface().startClient(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                    showSpinner();
//                } else {
//                    openPlaceCallActivity();
//                }
//            }else {
//                Log.d("Exception", "onStart: getSinchServiceInterface() return null ");
//                Toast.makeText(this, "getSinchServiceInterface() return null", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        checkSinchService();
//    }

    private void checkSinchService() {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                if (getSinchServiceInterface() != null) {
                    if (!getSinchServiceInterface().isStarted()) {
                        getSinchServiceInterface().startClient(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                    openActivity(MainActivity.class);

                } else {
                    Toast.makeText(this, "Video call is not ready yet. It may not work properly", Toast.LENGTH_SHORT).show();
                    //Log.d("1111", "checkSinchService: getSinchServiceInterface() return null"  );
                    openActivity(MainActivity.class);
                }
            } else {
                // go to loging
                openActivity(LoginActivity.class);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: checkSinchService : " + e.getMessage());
            openActivity(LoginActivity.class);
        }

    }


//    // ============sinch================

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStartFailed(SinchError error) {
        //Toast.makeText(this, "Error " + error.getMessage(), Toast.LENGTH_LONG).show();
        openActivity(MainActivity.class);
    }

    @Override
    public void onStarted() {
        openActivity(MainActivity.class);
    }

    //this method is invoked when the connection is established with the SinchService
    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

    private void openActivity(Class destination) {
        Intent intent = new Intent(this, destination);
        startActivity(intent);
        finish();
    }
}

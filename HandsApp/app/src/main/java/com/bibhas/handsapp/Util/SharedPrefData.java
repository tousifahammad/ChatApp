package com.bibhas.handsapp.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

public class SharedPrefData extends AppCompatActivity {

    public void setIsImageDownloaded(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("IS_IMAGE_DOWNLOADED", Context.MODE_PRIVATE).edit();
        editor.putBoolean("isImageDownloaded", true);
        editor.apply();
    }

    public boolean getIsImageDownloaded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("IS_IMAGE_DOWNLOADED", Context.MODE_PRIVATE);
        if (prefs.contains("isImageDownloaded"))
            return prefs.getBoolean("isImageDownloaded", false);
        else
            return false;
    }
}

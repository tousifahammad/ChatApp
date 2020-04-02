package com.bibhas.handsapp.Activities;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewMediaActivity extends AppCompatActivity {

    private final String TAG = "==ViewMediaActivity==";

    private ImageView imageView;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_media);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);

        String file_type = getIntent().getStringExtra("file_type");
        String file_dir = getIntent().getStringExtra("file_dir");


        showMedia(file_type, file_dir);
    }

    private void showMedia(String file_type, String file_dir) {
        switch (file_type) {
            case "image":
                imageView.setVisibility(View.VISIBLE);
                showImage(file_dir);
                break;

            case "video":
                videoView.setVisibility(View.VISIBLE);
                file_dir = file_dir.replace("thumb","");
                file_dir = file_dir.replace(SharedData.image_format,SharedData.video_format);
                showVideo(file_dir);
                break;

            default:
                break;
        }
    }


    private void showImage(String file_dir) {
        try {
            File file = new File(file_dir);
            Picasso.with(this).load(file).into(imageView);

        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }

    private void showVideo(String file_dir) {
        try {
            Log.d(TAG, "file_dir: " + file_dir);
            MediaController mediaController = new MediaController(videoView.getContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            Uri uri = Uri.parse(file_dir);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.start();
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }
}

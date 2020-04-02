package com.bibhas.handsapp.Util;

import android.os.Environment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class SharedData {
    public static final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public static String text_file_type = "text";

    public static String image_file_type = "image";
    public static String image_format = ".jpg";
    public static String sent_image_dir = "/HandsApp/Images/Sent";
    public static String received_image_dir = "/HandsApp/Images/Received";

    public static String thumb_image_file_type = "thumbimage";
    public static String sent_thumb_image_dir = "/HandsApp/Images/Sent/Thumbnails";
    public static String received_thumb_image_dir = "/HandsApp/Images/Received/Thumbnails";

    public static String audio_file_type = "audio";
    public static String audio_format = ".mp3";
    public static String sent_audio_dir = "/HandsApp/Audio/Sent";
    public static String received_audio_dir = "/HandsApp/Audio/Received";

    public static String video_file_type = "video";
    public static String video_format = ".mp4";
    public static String sent_video_dir = "/HandsApp/Video/Sent";
    public static String received_video_dir = "/HandsApp/Video/Received";

    public static String thumb_video_file_type = "thumbvideo";
    public static String sent_thumb_video_dir = "/HandsApp/Video/Sent/Thumbnails";
    public static String received_thumb_video_dir = "/HandsApp/Video/Received/Thumbnails";

    public static String file_one_file_type = "file_one";
    public static String file_one_format = ".pdf";
    public static String sent_file_one_dir = "/HandsApp/File/Sent";
    public static String received_file_one_dir = "/HandsApp/File/Received";

    public static String app_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();


    // notification
    public static String notification_id = "Hands_App_channel_id_000111";
    public static final String CHANNEL_1_ID = "Hands_App_channel1";
    public static final String CHANNEL_2_ID = "Hands_App_channel2";
    public static final String NOTIFICATION_GROUP = "HandsApp";


    //sinch voice and video calling
    public static final String APP_KEY = "2e22beb4-f0e9-48a9-ab57-9824fff20f4a";
    public static final String APP_SECRET = "PeJNpNMseE+0CDzZeVxpaQ==";
    public static final String ENVIRONMENT = "clientapi.sinch.com";
    public static final String HOST_NAME = "clientapi.sinch.com";
    public static final String USER_ID = "112721";


    //single or group chat
    public static final String group_id_prefix = "HandsAppGroup";
    public static final String single_chat = "single_chat";
    public static final String group_chat = "group_chat";

}

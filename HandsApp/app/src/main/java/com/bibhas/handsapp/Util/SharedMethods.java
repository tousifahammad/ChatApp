package com.bibhas.handsapp.Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bibhas.handsapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.zelory.compressor.Compressor;
import io.reactivex.annotations.NonNull;

public class SharedMethods extends AppCompatActivity {

    private final static String TAG = "==SharedMethods==";

    public static String getFileName(String str, String prefix, String file_format) {
        //Pattern pattern = Pattern.compile("message_" + file_type + "%2F(.*?)" + file_format);
        Pattern pattern = Pattern.compile(prefix + "%2F(.*?)" + file_format);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    public static boolean checkFileExist(String file_dir, String file_name) {
        try {
            File file = new File(file_dir + "/" + file_name);
            Log.d(TAG, "getPath: " + file.getPath().toString());
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "checkFileExist: " + e.getMessage());
        }

        return false;
    }


    public static ProgressDialog showProgressDialog(Context context, String title, String message) {

        ProgressDialog mProgressDialog = new ProgressDialog(context, R.style.AlertDialog);

        if (mProgressDialog != null) {
            mProgressDialog.setTitle(title);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        return mProgressDialog;
    }


    public static void changelayoutPosition(RelativeLayout container) {
        //=========== sent message =================
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
        layoutParams.setMargins(200, 0, 20, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        container.setLayoutParams(layoutParams);
        container.setBackgroundResource(R.drawable.gradient_message_sent);
    }

    public static void changelayoutPositionRight(RelativeLayout container) {
        //=========== sent message =================
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
        layoutParams.setMargins(20, 0, 200, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        container.setLayoutParams(layoutParams);
    }


    // ============= send push Notification to receiver ===============
    public static void sendPushNotification(String receiver, String message, String sender) {
        try {
            DatabaseReference notificationref = FirebaseDatabase.getInstance().getReference().child("notifications");

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", receiver);
            notificationData.put("message", message);

            notificationref.child(sender).push().setValue(notificationData);

        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }


    // ============= get Thumbnail from image uri ===============
    public static Bitmap getThumbnail(Context inContext, Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = inContext.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MICRO_KIND);
        return bitmap;
    }


    // ============= get Image Uri from bitmap===============
    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
            return Uri.parse(path);
        } catch (Exception e) {
            Log.d(TAG, "Exception: getImageUri " + e.getMessage());
        }
        return null;
    }


    // ============= validate Name from string ===============
    public static boolean validateName(String txt) {
        String regx = "^[\\p{L} .'-]+$";
        Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(txt);
        return matcher.find();
    }


    // ============= get a random AlphaNumeric string ===============
    public static String randomAlphaNumeric() {
        try {
            String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int count = 20;
            StringBuilder builder = new StringBuilder();
            while (count-- != 0) {
                int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
                builder.append(ALPHA_NUMERIC_STRING.charAt(character));
            }
            return builder.toString();
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
        return null;
    }


    // ============= string To HashMap converter===============
    public static HashMap<String, String> stringToHashMap(String value) {
        //String value = "{first_name = naresh,last_name = kumar,gender = male}";
        value = value.substring(1, value.length() - 1);           //remove curly brackets
        String[] keyValuePairs = value.split(",");        //split the string to creat key-value pairs
        HashMap<String, String> map = new HashMap<>();

        for (String pair : keyValuePairs)                        //iterate over the pairs
        {
            String[] entry = pair.split("=");                   //split the pairs to get key and value
            map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
        }
        return map;
    }


    // ============= get compressed image from image uri===============
    public Bitmap compressImage(Uri file_uri, int quality) {
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(file_uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        boolean isCompressed = bmp.compress(Bitmap.CompressFormat.PNG, quality, stream);
        byte[] byteArray = stream.toByteArray();
        try {
            stream.close();
            stream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isCompressed) {
            return bmp;
        }
        return null;
    }


    // ============= get resized image from image uri===============
    public static Bitmap resizeImage(Context c, Uri uri, final int requiredSize) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            while (true) {
                if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
        return null;
    }


    //download Thumb file
    public static void saveThumbFileInStorage(StorageReference fileRef, String file_dir, final String file_name) {
        if (fileRef != null) {
            try {
                File myDir = new File(file_dir);
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }

                final File localFile = new File(myDir, file_name);
                if (!localFile.exists()) {
                    localFile.createNewFile();
                    fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
                }

            } catch (IOException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    //download file
    public static void saveFileInStorage(StorageReference fileRef,
                                         String file_dir,
                                         final String file_name,
                                         final ImageView imageView,
                                         final ProgressBar progressBar,
                                         final TextView progress_percentage) {
        if (fileRef != null) {
            try {
                progressBar.setVisibility(View.VISIBLE);
                progress_percentage.setVisibility(View.VISIBLE);

                File myDir = new File(file_dir);
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }

                final File localFile = new File(myDir, file_name);
                if (!localFile.exists()) {
                    localFile.createNewFile();
                    fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            if (imageView != null) {   //for image
//                                Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                                imageView.setImageBitmap(bmp);
//                            }

                            // download full image

                            progressBar.setVisibility(View.GONE);
                            progress_percentage.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressBar.setVisibility(View.GONE);
                            progress_percentage.setText("Retry");
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progress_percentage.setText(((int) progress) + "%");
                        }
                    });
                }

            } catch (IOException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }


    // save audio
    public static void saveFileInStorage(StorageReference fileRef,
                                         final String file_dir,
                                         final String file_name,
                                         final MediaPlayer player,
                                         final ProgressBar progressBar,
                                         final TextView progress_percentage,
                                         final ImageView play_icon) {

        if (fileRef != null) {
            try {
                progressBar.setVisibility(View.VISIBLE);
                progress_percentage.setVisibility(View.VISIBLE);

                File myDir = new File(file_dir);
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }

                final File localFile = new File(myDir, file_name);
                if (!localFile.exists()) {
                    localFile.createNewFile();
                    fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            play_icon.setVisibility(View.VISIBLE);

                            try {
                                player.reset();
                                player.setDataSource(file_dir + "/" + file_name);
                                player.prepare();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.getMessage());
                            }


                            progressBar.setVisibility(View.GONE);
                            progress_percentage.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressBar.setVisibility(View.GONE);
                            progress_percentage.setText("download failed");
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progress_percentage.setText(((int) progress) + "%");
                        }
                    });
                    ;
                }

            } catch (IOException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }


    public static String getTime(Long milisec) {
        long seconds = 0;
        long minutes = 0;
        long hours = 0;
        String time = "";

        hours = milisec / 3600000;
        milisec = milisec % 3600000;

        minutes = milisec / 60000;
        milisec = milisec % 60000;

        seconds = milisec / 1000;

        time = hours + ": " + minutes + ":" + seconds;

        return time;

    }

    public static String getDateTime(Long milisec) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(milisec);
        return sdf.format(resultdate);
    }
}




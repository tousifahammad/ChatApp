package com.bibhas.handsapp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.google.firebase.messaging.RemoteMessage;

import static com.bibhas.handsapp.Activities.ChatActivity.chattingWithTheUserName;
import static com.bibhas.handsapp.Util.SharedData.CHANNEL_2_ID;
import static com.bibhas.handsapp.Util.SharedData.NOTIFICATION_GROUP;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
//    private NotificationManagerCompat notificationManager;
//    private Notification notification2;
//    private Notification summaryNotification;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        sendOnChannel1(notification_title, notification_message, from_user_id, click_action);
//        if ( Build.VERSION.SDK_INT >= 24){
//            sendOnChannel2(notification_title, notification_message, from_user_id, click_action);
//        }else {
//            sendOnChannel1(notification_title, notification_message, from_user_id, click_action);
//        }

        //Log.d("1111", "isChattingWithThisUser: " + chattingWithTheUserName + "    notification_title -" + notification_title);
    }

    private void sendOnChannel1(String notification_title, String notification_message, String from_user_id, String click_action) {
        if (chattingWithTheUserName != null) {
            if (chattingWithTheUserName.equals(notification_title)) {
                return;
            }
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, SharedData.CHANNEL_1_ID);
        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setOnlyAlertOnce(true);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", from_user_id);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();
        notificationManager.notify(mNotificationId, notificationBuilder.build());
    }

//    private void sendOnChannel2(String notification_title, String notification_message, String from_user_id, String click_action) {
//        // if user chat with the same person, who sent notification, then avoid notification
//        if (chattingWithTheUserName != null) {
//            if (chattingWithTheUserName.equals(notification_title)) {
//                return;
//            }
//        }
//
//        Intent resultIntent = new Intent(click_action);
//        resultIntent.putExtra("user_id", from_user_id);
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        Notification notification2 = new NotificationCompat.Builder(this, CHANNEL_2_ID)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle(notification_title)
//                .setContentText(notification_message)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setGroup(NOTIFICATION_GROUP)
//                .setContentIntent(resultPendingIntent)
//                .build();
//
//        Notification summaryNotification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .setBigContentTitle(NOTIFICATION_GROUP))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
//                .setGroup(NOTIFICATION_GROUP)
//                .setGroupSummary(true)
//                .setContentIntent(resultPendingIntent)
//                .build();
//
//        int mNotificationId = (int) System.currentTimeMillis();
//        notificationManager.notify(mNotificationId, notification2);
//        notificationManager.notify(11111, summaryNotification);
//    }
}

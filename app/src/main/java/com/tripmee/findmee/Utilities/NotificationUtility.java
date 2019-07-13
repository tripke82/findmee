package com.tripmee.findmee.Utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tripmee.findmee.EmergencyRequests.LocationActivity;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.R;

public class NotificationUtility {

    public static void notificationCallForOreo(String message, String RequestID, Context context){
        String title = context.getString(R.string.title_emergency_request);
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/warningsound");


        // Sets an ID for the notification, so it can be updated.
        int notifyID = 1;
        String CHANNEL_ID = GlobalConstants.EMERGENCY_NOTIFICATION_CHANNEL_ID;// The id of the channel.
        CharSequence name = GlobalConstants.EMERGENCY_NOTIFICATION_CHANNEL;// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Notification notification = new Notification.Builder(context)
                //.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setSmallIcon(R.drawable.ic_emergency)
                .setColorized(true)
                //.setColor(Color.BLUE)
                .setBadgeIconType(Notification.BADGE_ICON_LARGE)
                .setContentTitle(title)
                .setContentText(message)
                //.setSound(soundUri)
                .setChannelId(CHANNEL_ID).build();

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder() // new code for oreo
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            //delete old channels
            for (NotificationChannel channel: mNotificationManager.getNotificationChannels()) {
                String channelid = channel.getId();
                Log.d("channel", channelid);
                if(!channelid.equals(CHANNEL_ID)){
                    mNotificationManager.deleteNotificationChannel(channelid);
                }
            }

            // Configure the notification channel.
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (mChannel == null) {
                mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                //mChannel.setCol
                mChannel.setSound(soundUri, attributes); // This is IMPORTANT // ends new code
                mChannel.setBypassDnd(true);
                mChannel.setShowBadge(true);
                mChannel.setImportance(importance);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        Intent i = new Intent(context, LocationActivity.class);
        i.putExtra("RequestID", RequestID);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;

        // Issue the notification.
        //Log.e("Noti77", "notificationCall about to sent");
        mNotificationManager.notify(notifyID , notification);

    }

    public static void notificationCall(String message, String RequestID, Context context){

        String title = context.getString(R.string.title_emergency_request);
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/warningsound");

        Notification notification = new NotificationCompat.Builder(context, "Channel 1")
                //.setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                //.setLargeIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(soundUri)
                .build();


        Intent targetIntent = new Intent(context.getApplicationContext(), LocationActivity.class);

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //targetIntent.putExtra("Notification", "Notification");
        targetIntent.putExtra(GlobalConstants.SELECTED_FRAGMENT, GlobalConstants.NOTIFICATION_FRAGMENT);
        Intent i = new Intent(context, LocationActivity.class);
        //i.putExtra("UserID", ContactID);
        i.putExtra("RequestID", RequestID);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Log.e("Noti77", "Notificationcall about to sent");
        notificationManager.notify(1, notification);
    }

}

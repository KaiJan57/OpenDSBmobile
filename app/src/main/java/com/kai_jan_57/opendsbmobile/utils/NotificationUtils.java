package com.kai_jan_57.opendsbmobile.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.kai_jan_57.opendsbmobile.R;

public class NotificationUtils {

    private static int sNotificationId = 1;

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(context.getPackageName() + ".ContentUpdates", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancelAll();
        sNotificationId = 1;
    }

    public static void postNotification(Context context, Notification.Builder notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(context.getPackageName() + ".ContentUpdates");
        }
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(sNotificationId ++, notification.build());
    }
}

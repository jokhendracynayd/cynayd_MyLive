package com.livestreaming.mylive.notification_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.livestreaming.mylive.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationUtils {

    private static final String CHANNEL_ID = "mylive_new_channel";

    static void sendNotification(Context context, String title, String messageBody, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        CharSequence name = "mylive_new_channel";
        NotificationCompat.Builder mBuilder;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = createNotificationForHigherApis(context, title, messageBody, name, mNotifyMgr);
        } else {
            mBuilder = createNotificationForLowerApis(context, title, messageBody, icon, soundUri);
        }
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);
        mNotifyMgr.notify(NotificationID.getID(), mBuilder.build());
    }


    public static void sendNotification( Context context,  String title,  String messageBody,
                                         String avatarUrl,  Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        CharSequence name = "mylive_new_channel";
        NotificationCompat.Builder mBuilder;
        final NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Default icon for the notification
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        // Set the sound for the notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Download the avatar image using Glide (or Picasso)
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Glide.with(context)
                        .asBitmap()
                        .load(avatarUrl)
                        .into(new com.bumptech.glide.request.target.SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                NotificationCompat.Builder builder;
                                builder = createNotificationForHigherApis(context, title, messageBody, name, mNotifyMgr);
                                builder.setLargeIcon(resource);
                                builder.setAutoCancel(true);
                                builder.setContentIntent(pendingIntent);
                                mNotifyMgr.notify(NotificationID.getID(), builder.build());
                            }
                        });
            }
        });
    }

    private static NotificationCompat.Builder createNotificationForHigherApis(Context context, String title, String messageBody, CharSequence name, NotificationManager mNotifyMgr) {
        NotificationCompat.Builder mBuilder;
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription("mylive channel");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(true);
            mNotifyMgr.createNotificationChannel(mChannel);
        }
        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(messageBody);
        return mBuilder;
    }

    private static NotificationCompat.Builder createNotificationForLowerApis(Context context, String title, String messageBody, Bitmap icon, Uri soundUri) {
        NotificationCompat.Builder mBuilder;
        mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setLargeIcon(icon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH);
        return mBuilder;
    }

    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "mylive_new_channel";
            String description = "mylive channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;//makes sound and appear in notification bar
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static class NotificationID {
        private final static AtomicInteger c = new AtomicInteger();

        static int getID() {
            return c.incrementAndGet();
        }

        public static void init(int id) {
            c.set(id);
        }
    }


}

package com.technoexponent.clienttest3.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.technoexponent.clienttest3.R;

public class NotificationService extends Service {

    // notification channel id for devices running Android-Oreo and above
    private static final String NOTIFICATION_CHANNEL_ID = "100";

    // total lifespan(in mills) of this service, after which it will restart itself
    private static final long TOTAL_TIME = 5 * 60 * 1000; // 5 mins

    // countdown timer interval
    private static final long INTERVAL_TIME = 1000; // 1 sec

    // The delay, after which the service restarts itself
    private static final long SERVICE_RESTART_DELAY = 1500; // 1.5 sec


    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get notification manager instance
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // start this service as foreground service
        startForeground(1, getNotification(getString(R.string.you_are_on_break), getString(R.string.time_left)));

        // start countdown timer and update notification
        CountDownTimer countDownTimer = new CountDownTimer(TOTAL_TIME, INTERVAL_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                // update notification with remaining time
                Notification notification = getNotification(getString(R.string.you_are_on_break), getString(R.string.time_left) + " " + getMinuteAndSeconds(millisUntilFinished));
                notificationManager.notify(1, notification);
            }

            @Override
            public void onFinish() {
                // remove foreground notification
                stopForeground(true);

                // stop this service
                stopSelf();

                // schedule to restart itself
                new Handler()
                        .postDelayed(
                                () -> startService(new Intent(NotificationService.this, NotificationService.class)),
                                SERVICE_RESTART_DELAY
                        );
            }
        }.start();

        return super.onStartCommand(intent, flags, startId);
    }

    // returns mm:ss formatted time from provided milliseconds
    private String getMinuteAndSeconds(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return formatTo2Digits("" + minutes) + ":" + formatTo2Digits("" + seconds);
    }

    // formats to 2 digit String (adds '0' before any single digit char)
    private String formatTo2Digits(String s) {
        return s.length() == 1 ? "0" + s : s;
    }

    // create and returns a notification Object with provided Title and Body
    private Notification getNotification(String notification_title, String notification_body) {
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // create notification channel for Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, notification_title, NotificationManager.IMPORTANCE_HIGH);

            // configure Notification Channel
            notificationChannel.setDescription(notification_body);
            notificationChannel.enableLights(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        // configure notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)
                .setSound(defaultSound)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification_title)
                .setContentText(notification_body);

        // create notification object and returns it
        return notificationBuilder.build();
    }
}

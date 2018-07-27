package com.pockeyt.cloverpay.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.handlers.NotificationHandler;
import com.pockeyt.cloverpay.ui.activities.MainActivity;

public class PusherService extends Service {
    public static final String POCKEYT_SERVICES_CHANNEL = "pockeyt_services";
    private static final String TAG = PusherService.class.getSimpleName();
    private IBinder pusherBinder = new PusherBinder();
    private NotificationHandler mNotificationHandler;
    private boolean mRunning;

    public class PusherBinder extends Binder {
        public PusherService getService() {
            return PusherService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!mRunning && intent != null) {
            Log.d(TAG, "is not running");
            mRunning = true;
            mNotificationHandler = new NotificationHandler(intent.getStringExtra(MainActivity.KEY_BUSINESS_SLUG), intent.getStringExtra(MainActivity.KEY_BUSINESS_TOKEN));
            mNotificationHandler.init();
            createNotificationChannel();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, POCKEYT_SERVICES_CHANNEL);
            builder.setSmallIcon(R.drawable.ic_done_white_48dp);
            builder.setContentTitle("Pockeyt Services");
            builder.setContentText("Pockeyt Services are running");
            Notification notification = builder.build();

            startForeground(-1, notification);
            return Service.START_STICKY;
        } else {
            Log.d(TAG, "is runnning");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return pusherBinder;
    }

    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(POCKEYT_SERVICES_CHANNEL, "Pockeyt Services",  NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Pockeyt Services currently active");
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }


    public void stopErrorNotification(int notificationId) {
        mNotificationHandler.stopRepeatErrorNotification(notificationId);
    }

}

package com.pockeyt.cloverpay.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pockeyt.cloverpay.handlers.NotificationHandler;

public class NotificationAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "On received notification Alarm receiver");
        Log.d(TAG, NotificationHandler.DEFAULT_NOTIFICATION_ID +"");
        notificationManager.cancel(NotificationHandler.DEFAULT_NOTIFICATION_ID);
    }
}

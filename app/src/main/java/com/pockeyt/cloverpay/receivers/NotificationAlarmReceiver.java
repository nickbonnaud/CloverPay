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
        Log.d(TAG, intent.getIntExtra(NotificationHandler.NOTIFICATION_ID_KEY, NotificationHandler.DEFAULT_NOTIFICATION_ID) + "");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(intent.getIntExtra(NotificationHandler.NOTIFICATION_ID_KEY, NotificationHandler.DEFAULT_NOTIFICATION_ID));
    }
}

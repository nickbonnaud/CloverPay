package com.pockeyt.cloverpay.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.handlers.NotificationHandler;
import com.pockeyt.cloverpay.utils.PusherService;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationDismissedReceiver.class.getSimpleName();
    private int mNotificationId;
    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationId = intent.getIntExtra(NotificationHandler.NOTIFICATION_ID_KEY, NotificationHandler.DEFAULT_NOTIFICATION_ID);
        connectToService();
    }

    private void connectToService() {
        Context context = PockeytPay.getAppContext();
        Intent intent = new Intent(context, PusherService.class);
        context.bindService(intent, serviceConnection, 0);
    }

    private void disconnectFromService() {
        PockeytPay.getAppContext().unbindService(serviceConnection);
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PusherService pusherService = ((PusherService.PusherBinder) service).getService();
            pusherService.stopErrorNotification(mNotificationId);
            disconnectFromService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}

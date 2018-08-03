package com.pockeyt.cloverpay.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.clover.sdk.v1.Intents;
import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.handlers.NotificationHandler;
import com.pockeyt.cloverpay.utils.PusherService;

public class NotificationErrorClickedReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationErrorClickedReceiver.class.getSimpleName();
    private int mNotificationId;
    private String mOrderId;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received dismiss in receiver");
        mNotificationId = intent.getIntExtra(NotificationHandler.NOTIFICATION_ID_KEY, NotificationHandler.DEFAULT_NOTIFICATION_ID);
        mOrderId = intent.getStringExtra(NotificationHandler.NOTIFICATION_ORDER_ID);
        boolean isError = intent.getBooleanExtra(NotificationHandler.KEY_IS_ERROR, false);
        if (isError) {
            connectToService();
        }
        dismissNotification();
    }

    private void connectToService() {
        Log.d(TAG, "Connect to service");
        Context context = PockeytPay.getAppContext();
        Intent intent = new Intent(context, PusherService.class);
        context.bindService(intent, serviceConnection, 0);
    }

    private void dismissNotification() {
        Log.d(TAG, "Dismiss notification function");
        Intent intent = new Intent(Intents.ACTION_START_ORDER_MANAGE);
        intent.putExtra(Intents.EXTRA_ORDER_ID, mOrderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PockeytPay.getAppContext().startActivity(intent);
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

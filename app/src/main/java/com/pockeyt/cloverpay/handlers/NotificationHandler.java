package com.pockeyt.cloverpay.handlers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.CustomerPubSubModel;
import com.pockeyt.cloverpay.receivers.NotificationAlarmReceiver;
import com.pockeyt.cloverpay.receivers.NotificationDismissedReceiver;
import com.pockeyt.cloverpay.receivers.NotificationErrorClickedReceiver;
import com.pockeyt.cloverpay.utils.ImageLoader;
import com.pockeyt.cloverpay.utils.PusherConnector;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.subjects.PublishSubject;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class NotificationHandler  {
    private static final String TAG = NotificationHandler.class.getSimpleName();
    public static final String NOTIFICATION_ID_KEY = "notification_id_key";
    public static final String NOTIFICATION_ORDER_ID = "notification_order_id";
    public static final int DEFAULT_NOTIFICATION_ID = 21;
    public static final String NOTIFICATION_BROADCAST_CUSTOMER_ACTION = "notification_broadcast_customer_action";
    public static final String KEY_CUSTOMER_BROADCAST = "customer_broadcast_key";
    public static final String KEY_TYPE_BROADCAST = "type_broadcast_key";
    public static final String KEY_IS_ERROR = "is_error_key";

    private static final String POCKEYT_NOTIFICATION_CHANNEL = "pockeyt_notification";
    private static int mNotificationIdToDismiss;
    private static ArrayList<Integer> mRunningNotifications = new ArrayList<Integer>();
    private String mBusinessSlug;
    private String mBusinessToken;

    private PublishSubject<CustomerPubSubModel> mCustomerPusherSubject;


    public NotificationHandler(String businessSlug, String businessToken) {
        mBusinessSlug = businessSlug;
        mBusinessToken = businessToken;
    }

    public void init() {
        startPusherThread();
    }

    private void startPusherThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                connectToPusher();
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName("PusherThread");
        thread.start();
    }

    private void connectToPusher() {
        PusherConnector pusherConnector = new PusherConnector(mBusinessSlug, mBusinessToken);
        if (!pusherConnector.getIsPusherConnected()) {
            pusherConnector.connectToPusher();
            pusherConnector.getPusherChannel().bind(pusherConnector.getPusherEvent(), privateChannelEventListener);
        }
    }

    private PrivateChannelEventListener privateChannelEventListener = new PrivateChannelEventListener() {
        @Override
        public void onAuthenticationFailure(String s, Exception e) {
            Log.e(TAG, e.getMessage());
        }

        @Override
        public void onSubscriptionSucceeded(String s) {
            Log.d(TAG, s);
        }

        @Override
        public void onEvent(String channel, String event, String data) {
            Log.d(TAG, "on event");
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject dataBody = jsonObject.getJSONObject("data");
                handlePusherData(dataBody);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void handlePusherData(JSONObject dataBody) throws JSONException, IOException {
        String type = dataBody.getString("type");
        CustomerModel customer = CustomerHandler.setCustomer(dataBody.getJSONObject("data"));
        JSONObject openTransaction = (dataBody.getJSONObject("data")).getJSONObject("open_transaction");

        String title;
        String message;
        int icon = R.drawable.ic_stat_logo;
        boolean isError = false;
        boolean isDealOrRewardError = false;
        boolean isCustomerExitEnter = false;

        switch (type) {
            case "deal_redeemed":
                title = "Deal Redeemed!";
                message = customer.getFirstName() + " has accepted your request to redeem their deal!";
                break;
            case "loyalty_redeemed":
                title = "Loyalty Reward Redeemed!";
                message = customer.getFirstName() + " has accepted your request to redeem their loyalty reward!";
                break;
            case "redeem_later_deal":
                title = "Redeem Deal Later";
                message = customer.getFirstName() + " wishes to redeem their deal at a later time.";
                icon = R.drawable.ic_stat_deal;
                isDealOrRewardError = true;
                break;
            case "wrong_deal":
                title = "Wrong Deal!";
                message = customer.getFirstName() + " claims they did not purchase this deal.";
                icon = R.drawable.ic_stat_deal;
                isDealOrRewardError = true;
                break;
            case "redeem_later_reward":
                title = "Redeem Loyalty Reward Later";
                message = customer.getFirstName() + " wishes to redeem their loyalty reward at a later time.";
                icon = R.drawable.ic_stat_loyalty;
                isDealOrRewardError = true;
                break;
            case "not_earned_reward":
                title = "Reward Not Earned!";
                message = customer.getFirstName() + " claims they have not earned this loyalty reward.";
                icon = R.drawable.ic_stat_loyalty;
                isDealOrRewardError = true;
                break;
            case "wrong_bill":
                title = "Wrong Bill Sent!";
                message = customer.getFirstName() + " claims the bill they were sent was the wrong bill.";
                isError = true;
                break;
            case "error_bill":
                title = "Error in Customer Bill";
                message = customer.getFirstName() + " claims their is an error with their bill.";
                isError = true;
                break;
            case "customer_enter":
                title = "New Pockeyt Customer";
                message = customer.getFirstName() + " " + customer.getLastName() + " has entered your location.";
                isCustomerExitEnter = true;
                break;
            case "customer_exit_unpaid":
                title = "Pockeyt Customer Exited";
                message = customer.getFirstName() + " " + customer.getLastName() + " has left with an open bill.";
                isCustomerExitEnter = true;
                break;
            case "customer_exit_paid":
                title = null;
                message = null;
                break;
            default:
                title = "Something Went Wrong!";
                message = "Oops! An error occurred";
        }

        Bitmap customerImage = getCustomerPhoto(customer);

        if (isError) {
            repeatErrorNotification(title, message, openTransaction, customerImage);
        } else if (isDealOrRewardError) {
            showDealOrRewardNotification(title, message, icon, customerImage);
        } else if (isCustomerExitEnter) {
            showCustomerExitEnterEvent(title, message, openTransaction, customerImage);
        }
        broadcastUpdatedCustomer(customer, type);
    }

    private Bitmap getCustomerPhoto(CustomerModel customer) throws IOException {
        Context context = PockeytPay.getAppContext();
        RequestCreator creator = ImageLoader.load(context,customer.getPhotoUrl());
        creator.resize(context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height));
        if (android.os.Build.VERSION.SDK_INT > 19) {
            return creator.transform(new CropCircleTransformation()).get();
        }
        return creator.get();
    }

    private void broadcastUpdatedCustomer(CustomerModel customer, String type) {
        Intent intent = new Intent(NOTIFICATION_BROADCAST_CUSTOMER_ACTION);
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_CUSTOMER_BROADCAST, customer);
        bundle.putString(KEY_TYPE_BROADCAST, type);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(PockeytPay.getAppContext()).sendBroadcast(intent);
    }


    private void repeatErrorNotification(String title, String message, JSONObject openTransaction, Bitmap customerImage) throws JSONException {
        int notificationId = openTransaction.getBoolean("has_open") ? openTransaction.getInt("transaction_id") : DEFAULT_NOTIFICATION_ID;
        String orderId = openTransaction.getBoolean("has_open") ? openTransaction.getString("pos_transaction_id") : null;
        Context context = PockeytPay.getAppContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showHeadsUpNotification(context, title, message, orderId, customerImage, notificationId);
        } else {
            showActionBarNotification(context, title, message, orderId, customerImage, notificationId);
        }
    }

    private void showActionBarNotification(Context context, String title, String message, String orderId, Bitmap customerImage, int notificationId) {
        if (findRunningNotificationIndex(notificationId) == -1) {
            mRunningNotifications.add(notificationId);
        } else {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            private int count = 0;

            @Override
            public void run() {
                if (getShouldRepeatNotification(notificationId)) {
                    if (isOldestNotification(notificationId) || count == 0) {
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        createNotificationChannel(notificationManager);
                        boolean isColorized = count % 2 == 0;
                        Uri sound = count <= 60 ? Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm) : Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.monotone);
                        int icon = isColorized ? R.drawable.ic_stat_bill_error_red : R.drawable.ic_stat_bill_error;

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, POCKEYT_NOTIFICATION_CHANNEL);
                        builder.setSmallIcon(icon)
                                .setColor(Color.RED)
                                .setColorized(isColorized)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setSound(sound)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setContentIntent(createContentIntent(orderId, notificationId, true))
                                .setDeleteIntent(createOnDismissedIntent(notificationId))
                                .setLargeIcon(customerImage)
                                .setAutoCancel(true);
                        Notification notification = builder.build();
                        notificationManager.notify(notificationId, notification);
                    }
                    count++;
                    handler.postDelayed(this, 2000);
                } else {
                    mNotificationIdToDismiss = -1;
                }
            }
        }, 2000);
    }

    private void showHeadsUpNotification(Context context, String title, String message, String orderId, Bitmap customerImage, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, POCKEYT_NOTIFICATION_CHANNEL);
        builder.setSmallIcon(R.drawable.ic_stat_bill_error)
                .setColor(Color.RED)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.monotone))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(createContentIntent(orderId, notificationId, true))
                .setDeleteIntent(createOnDismissedIntent(notificationId))
                .setLargeIcon(customerImage)
                .setAutoCancel(true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_CALL)
                .setFullScreenIntent(PendingIntent.getActivity(context, 0, new Intent(), 0), true);
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    private boolean getShouldRepeatNotification(int notificationId) {
        return notificationId != mNotificationIdToDismiss;
    }

    private boolean isOldestNotification(int notificationId) {
        return notificationId == mRunningNotifications.get(0);
    }

    private int findRunningNotificationIndex(int notificationId) {
        return mRunningNotifications.indexOf(notificationId);
    }

    public void stopRepeatErrorNotification(int notificationId) {
        mNotificationIdToDismiss = notificationId;
        int index = findRunningNotificationIndex(notificationId);
        if (index != -1) {
            mRunningNotifications.remove(index);
        }
    }

    private void showDealOrRewardNotification(String title, String message, int icon, Bitmap customerImage) {
        Context context = PockeytPay.getAppContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, POCKEYT_NOTIFICATION_CHANNEL);
        builder.setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.monotone))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(new long[] {1000, 500, 1000})
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                .setTimeoutAfter(10 * 60 * 1000)
                .setContentText(message)
                .setLargeIcon(customerImage);
        Notification notification = builder.build();
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notification);
        setAlarmToRemoveSuccessNotification(10 * 60 * 1000, DEFAULT_NOTIFICATION_ID);
    }

    private void showCustomerExitEnterEvent(String title, String message, JSONObject openTransaction, Bitmap customerImage) throws JSONException, IOException {
        Context context = PockeytPay.getAppContext();
        boolean hasOpenTransaction =  openTransaction.getBoolean("has_open");

        int notificationId = hasOpenTransaction ? openTransaction.getInt("transaction_id") : DEFAULT_NOTIFICATION_ID;
        String orderId = hasOpenTransaction ? openTransaction.getString("pos_transaction_id") : null;
        int icon = hasOpenTransaction ? R.drawable.ic_stat_customer_leave : R.drawable.ic_stat_customer_enter;
        int color = hasOpenTransaction ? Color.rgb(243,156,18) : Color.rgb(39,174,96);
        Uri sound = hasOpenTransaction ? Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.monotone) : Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.success);
        PendingIntent pendingIntent = hasOpenTransaction ? createContentIntent(orderId, notificationId, false) : PendingIntent.getActivity(context, 0, new Intent(), 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, POCKEYT_NOTIFICATION_CHANNEL);
        builder.setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(sound)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(new long[] {1000, 500, 1000})
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(3 * 60 * 1000)
                .setColorized(true)
                .setColor(color)
                .setLargeIcon(customerImage)
                .setContentText(message);
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
        setAlarmToRemoveSuccessNotification(3 * 60 * 1000, notificationId);
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(POCKEYT_NOTIFICATION_CHANNEL, "Pockeyt Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Pockeyt Notification");
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    private PendingIntent createContentIntent(String orderId, int notificationId, boolean isError) {
        Context context = PockeytPay.getAppContext();
        Intent intent = new Intent(context, NotificationErrorClickedReceiver.class);
        intent.putExtra(NOTIFICATION_ORDER_ID, orderId);
        intent.putExtra(NOTIFICATION_ID_KEY, notificationId);
        intent.putExtra(KEY_IS_ERROR, isError);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, 0);
        return pendingIntent;
    }

    private PendingIntent createOnDismissedIntent(int notificationId) {
        Context context = PockeytPay.getAppContext();
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra(NOTIFICATION_ID_KEY, notificationId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notificationId, intent, 0);
        return pendingIntent;
    }

    private void setAlarmToRemoveSuccessNotification(long dismissAfterTime, int notificationId) {
        Context context = PockeytPay.getAppContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        intent.putExtra(NOTIFICATION_ID_KEY, notificationId);
        int requestCode = (int) System.currentTimeMillis();
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + dismissAfterTime, alarmIntent);
    }


}

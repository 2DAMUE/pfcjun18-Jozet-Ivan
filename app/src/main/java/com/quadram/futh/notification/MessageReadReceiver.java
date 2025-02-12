package com.quadram.futh.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.quadram.futh.helper.Constantes;

public class MessageReadReceiver extends BroadcastReceiver {
    private static final String TAG = MessageReadReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ONRECEIVE");
        int conversationId = intent.getIntExtra(Constantes.NOTIFICATION_ID_STRING, -1);
        if (conversationId != -1) {
            Log.d(TAG, "Notification " + conversationId + " was read");
            NotificationManagerCompat.from(context).cancel(conversationId);
        }
    }
}

package com.quadram.futh.notification;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import com.quadram.futh.helper.Constantes;

public class NotificationHelper extends ContextWrapper {
    private Intent notification;

    public NotificationHelper(Context c) {
        super(c);

        notification = new Intent(getApplicationContext(), NotificationService.class);
    }

    public void showNotification(String title, String text, String channel, int icon, boolean hasReply) {
        notification.setAction(Constantes.SHOW_NOTIFICATION);
        notification.putExtra("title", title);
        notification.putExtra("text", text);
        notification.putExtra("channel", channel);
        notification.putExtra("icon", icon);
        notification.putExtra("hasReply", hasReply);
        startService(notification);
    }
}

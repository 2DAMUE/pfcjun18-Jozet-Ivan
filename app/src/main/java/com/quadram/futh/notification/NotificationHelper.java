package com.quadram.futh.notification;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.quadram.futh.helper.Constantes;

public class NotificationHelper extends ContextWrapper {
    private Intent notification;

    public NotificationHelper(Context c) {
        super(c);

        notification = new Intent(getApplicationContext(), NotificationService.class);
    }

    public void showNotification(String title, String text, int icon) {
        notification.setAction(Constantes.SEND_MESSAGE_ACTION);
        notification.putExtra("title", title);
        notification.putExtra("text", text);
        notification.putExtra("icon", icon);
        startService(notification);
    }
}
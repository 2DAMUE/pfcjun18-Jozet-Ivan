package com.quadram.futh.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStartup extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SERVICE_STARTUP", "ONBOOTCOMPLETED");
        Intent startServiceIntent = new Intent(context, ServiceListener.class);
        context.startService(startServiceIntent);
    }
}

package com.quadram.futh.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.quadram.futh.helper.Constantes;

import java.util.Arrays;

public class MessageReplyReceiver extends BroadcastReceiver {
    private static String KEY_NOTIFICATION_ID = "key_noticiation_id";
    private static String KEY_MESSAGE_ID = "key_message_id";

    public static Intent getReplyMessageIntent(Context context, int notificationId, int messageId) {
        Intent intent = new Intent(context, MessageReplyReceiver.class);
        intent.setAction(Constantes.REPLY_ACTION);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(KEY_MESSAGE_ID, messageId);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constantes.REPLY_ACTION.equals(intent.getAction())) {
            // do whatever you want with the message. Send to the server or add to the db.
            // for this tutorial, we'll just show it in a toast;
            CharSequence message = getMessageText(intent);
            String channel = intent.getStringExtra("channel");

            processReply(context, channel, message);

            NotificationManagerCompat.from(context).cancel(Constantes.NOTIFICATION_ID_INT);  // Se cancela la notificacion una vez ha sido respondida
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(Constantes.VOICE_REPLY);
        }
        return "";
    }

    private void processReply(Context context, String channel, CharSequence message) {
        if (channel.equalsIgnoreCase(Constantes.CHANNEL_LIGHT)) {
            if (Arrays.asList(Constantes.COMANDOS_ENCENDER_LUZ).contains(message)) {
                Toast.makeText(context, "Encendiendo la luz ahora mismo", Toast.LENGTH_LONG).show();
                FirebaseDatabase.getInstance().getReference("devices/0x00000001/rele1/state").setValue("on");
            }
            else if (Arrays.asList(Constantes.COMANDOS_APAGAR_LUZ).contains(message)) {
                Toast.makeText(context, "Apagando la luz ahora mismo", Toast.LENGTH_LONG).show();
                FirebaseDatabase.getInstance().getReference("devices/0x00000001/rele1/state").setValue("off");
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_PLUG)) {
            if (Arrays.asList(Constantes.COMANDOS_ACTIVAR_ENCHUFE).contains(message)) {
                Toast.makeText(context, "Activando el enchufe ahora mismo", Toast.LENGTH_LONG).show();
                FirebaseDatabase.getInstance().getReference("devices/0x00000001/rele2/state").setValue("on");
            }
            else if (Arrays.asList(Constantes.COMANDOS_DESACTIVAR_ENCHUFE).contains(message)) {
                Toast.makeText(context, "Desactivando el enchufe ahora mismo", Toast.LENGTH_LONG).show();
                FirebaseDatabase.getInstance().getReference("devices/0x00000001/rele2/state").setValue("off");
            }
        }
        Toast.makeText(context, "Reply: " + message, Toast.LENGTH_LONG).show();
        Log.d("REPLY", message.toString());
    }
}

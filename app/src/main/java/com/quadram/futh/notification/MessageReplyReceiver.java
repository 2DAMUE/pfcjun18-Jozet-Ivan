package com.quadram.futh.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quadram.futh.R;
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

    private void processReply(final Context context, String channel, CharSequence message) {
        final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("devices/0x00000001/");

        if (channel.equalsIgnoreCase(Constantes.CHANNEL_LIGHT)) {
            if (Arrays.asList(Constantes.COMANDOS_ENCENDER_LUZ).contains(message)) {
                Toast.makeText(context, "Encendiendo luz...", Toast.LENGTH_LONG).show();
                mReference.child("rele1/state").setValue("on");
            }
            else if (Arrays.asList(Constantes.COMANDOS_APAGAR_LUZ).contains(message)) {
                Toast.makeText(context, "Apagando luz...", Toast.LENGTH_LONG).show();
                mReference.child("rele1/state").setValue("off");
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_PLUG)) {
            if (Arrays.asList(Constantes.COMANDOS_ACTIVAR_ENCHUFE).contains(message)) {
                Toast.makeText(context, "Activando enchufe...", Toast.LENGTH_LONG).show();
                mReference.child("rele2/state").setValue("on");
            }
            else if (Arrays.asList(Constantes.COMANDOS_DESACTIVAR_ENCHUFE).contains(message)) {
                Toast.makeText(context, "Desactivando enchufe...", Toast.LENGTH_LONG).show();
                mReference.child("rele2/state").setValue("off");
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_GAS)) {
            if (Arrays.asList(Constantes.COMANDOS_ESTADO_GAS).contains(message)) {
                Toast.makeText(context, "Leyendo estado del gas...", Toast.LENGTH_LONG).show();
                mReference.child("gas1/risk").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int risk = Integer.parseInt(snapshot.getValue().toString());

                        new NotificationHelper(context).showNotification(Constantes.CHANNEL_GAS, "El riesgo de gas es de nivel "+risk, Constantes.CHANNEL_GAS, R.mipmap.gas_risk_one_icon, false);

                        mReference.removeEventListener(this);  // Se elimina el listener una vez se recupero el dato correspondiente
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_HUMIDITY)) {
            if (Arrays.asList(Constantes.COMANDOS_VALOR_HUMEDAD).contains(message)) {
                Toast.makeText(context, "Leyendo valor de humedad...", Toast.LENGTH_LONG).show();
                mReference.child("humidity1/value").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        float value = Float.parseFloat(snapshot.getValue().toString());

                        new NotificationHelper(context).showNotification(Constantes.CHANNEL_HUMIDITY, "La humedad es del "+value+" por ciento", Constantes.CHANNEL_HUMIDITY, R.mipmap.humidity_notification_icon, false);

                        mReference.removeEventListener(this);  // Se elimina el listener una vez se recupero el dato correspondiente
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_TEMPERATURE)) {
            if (Arrays.asList(Constantes.COMANDOS_VALOR_TEMPERATURA).contains(message)) {
                Toast.makeText(context, "Leyendo valor de temperatura...", Toast.LENGTH_LONG).show();
                mReference.child("temperature1/value").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        float value = Float.parseFloat(snapshot.getValue().toString());

                        new NotificationHelper(context).showNotification(Constantes.CHANNEL_TEMPERATURE, "La temperatura es de "+value+" grados", Constantes.CHANNEL_TEMPERATURE, R.mipmap.notification_temperature_icon, false);

                        mReference.removeEventListener(this);  // Se elimina el listener una vez se recupero el dato correspondiente
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
        Toast.makeText(context, "Reply: " + message, Toast.LENGTH_LONG).show();
        Log.d("REPLY", message.toString());
    }
}

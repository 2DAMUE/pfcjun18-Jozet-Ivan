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
            CharSequence message = getMessageText(intent);
            String channel = intent.getStringExtra("channel"); // Se obtiene el canal de la notificacion en forma de string
            int channelId = intent.getIntExtra("channelId", 0);  // Se obtiene el canal de la notificacion en forma de int

            processReply(context, channelId, channel, message);  // Se procesa el tipo de notificacion y la respuesta obtenida
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(Constantes.VOICE_REPLY);  // Se obtiene la respuesta de voz
        }
        return "";
    }

    private void processReply(final Context context, int channelId, String channel, CharSequence message) {
        final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("devices/0x00000001/");

        if (channel.equalsIgnoreCase(Constantes.CHANNEL_LIGHT)) {  // Si la notificacion es de luz
            if (Arrays.asList(Constantes.COMANDOS_ENCENDER_LUZ).contains(message)) {  // Si la respuesta obtenida coincide con algun comando de encender luz
                //Toast.makeText(context, "Encendiendo luz...", Toast.LENGTH_LONG).show();
                mReference.child("rele1/state").setValue("on");  // Encendemos la luz
            }
            else if (Arrays.asList(Constantes.COMANDOS_APAGAR_LUZ).contains(message)) {  // Si la respuesta obtenida coincide con algun comando de apagar luz
                //Toast.makeText(context, "Apagando luz...", Toast.LENGTH_LONG).show();
                mReference.child("rele1/state").setValue("off");  // Apagamos la luz
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_PLUG)) {  // Si la notificacion es de enchufe
            if (Arrays.asList(Constantes.COMANDOS_ACTIVAR_ENCHUFE).contains(message)) {  // Si la respuesta obtenida coincide con algun comando de activar enchufe
                //Toast.makeText(context, "Activando enchufe...", Toast.LENGTH_LONG).show();
                mReference.child("rele2/state").setValue("on");  // Activamos el enchufe
            }
            else if (Arrays.asList(Constantes.COMANDOS_DESACTIVAR_ENCHUFE).contains(message)) {  // Si la respuesta obtenida coincide con algun comando de desactivar enchufe
                //Toast.makeText(context, "Desactivando enchufe...", Toast.LENGTH_LONG).show();
                mReference.child("rele2/state").setValue("off");  // Desactivamos el enchufe
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_GAS)) {  // Si la notificacion es de gas
            if (Arrays.asList(Constantes.COMANDOS_ESTADO_GAS).contains(message)) {  // Si la respuesta obtenida coincide con algun comando para conocer el nivel de riesgo de gas
                //Toast.makeText(context, "Leyendo estado del gas...", Toast.LENGTH_LONG).show();
                mReference.child("gas1/risk").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int risk = Integer.parseInt(snapshot.getValue().toString());  // Se obtiene el valor solicitado

                        // Mostramos una nueva notificacion con los datos solicitados
                        new NotificationHelper(context).showNotification(Constantes.CHANNEL_GAS_ID, Constantes.CHANNEL_GAS, "El riesgo de gas es de nivel "+risk, Constantes.CHANNEL_GAS, R.mipmap.gas_risk_one_icon, false);

                        mReference.removeEventListener(this);  // Se elimina el listener una vez se recupero el dato correspondiente
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_HUMIDITY)) {  // Si la notificacion es de humedad
            if (Arrays.asList(Constantes.COMANDOS_VALOR_HUMEDAD).contains(message)) {  // Si la respuesta obtenida coincide con algun comando para conocer el porcentaje de humedad
                //Toast.makeText(context, "Leyendo valor de humedad...", Toast.LENGTH_LONG).show();
                mReference.child("humidity1/value").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        float value = Float.parseFloat(snapshot.getValue().toString());  // Se obtiene el valor solicitado

                        // Mostramos una nueva notificacion con los datos solicitados
                        new NotificationHelper(context).showNotification(Constantes.CHANNEL_HUMIDITY_ID, Constantes.CHANNEL_HUMIDITY, "La humedad es del "+value+" por ciento", Constantes.CHANNEL_HUMIDITY, R.mipmap.humidity_notification_icon, false);

                        mReference.removeEventListener(this);  // Se elimina el listener una vez se recupero el dato correspondiente
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
        else if (channel.equalsIgnoreCase(Constantes.CHANNEL_TEMPERATURE)) {  // Si la notificacion es de temperatura
            if (Arrays.asList(Constantes.COMANDOS_VALOR_TEMPERATURA).contains(message)) {  // Si la respuesta obtenida coincide con algun comando para conocer la temperatura
                //Toast.makeText(context, "Leyendo valor de temperatura...", Toast.LENGTH_LONG).show();
                mReference.child("temperature1/value").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        float value = Float.parseFloat(snapshot.getValue().toString());  // Se obtiene el valor solicitado

                        // Mostramos una nueva notificacion con los datos solicitados
                        new NotificationHelper(context).showNotification(Constantes.CHANNEL_TEMPERATURE_ID, Constantes.CHANNEL_TEMPERATURE, "La temperatura es de "+value+" grados", Constantes.CHANNEL_TEMPERATURE, R.mipmap.notification_temperature_icon, false);

                        mReference.removeEventListener(this);  // Se elimina el listener una vez se recupero el dato correspondiente
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
        //Toast.makeText(context, "Reply: " + message, Toast.LENGTH_LONG).show();
        Log.d("REPLY", message.toString());
        NotificationManagerCompat.from(context).cancel(channelId);  // Se cancela la notificacion una vez ha sido respondida
    }
}

package com.quadram.futh.notification;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.quadram.futh.R;
import com.quadram.futh.helper.Constantes;

public class NotificationService extends IntentService {
    private static final String TAG = NotificationService.class.getSimpleName();

    public NotificationService() {
        super(NotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Handle intent to send a new notification.
        if (intent != null && Constantes.SHOW_NOTIFICATION.equals(intent.getAction())) {

            sendNotification(
                    Constantes.NOTIFICATION_ID_INT,
                    intent.getStringExtra("title"),
                    intent.getStringExtra("text"),
                    intent.getStringExtra("channel"),
                    intent.getIntExtra("icon", android.R.drawable.sym_def_app_icon),
                    intent.getBooleanExtra("hasReply", false),
                    System.currentTimeMillis());
        }
    }

    // Creates an intent that will be triggered when a message is read.
    private Intent getMessageReadIntent(int id) {
        return new Intent().setAction(Constantes.READ_ACTION).putExtra(Constantes.NOTIFICATION_ID_STRING, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId, String channel) {
        return new Intent()
                .setAction(Constantes.REPLY_ACTION)
                .putExtra("channel", channel)
                .putExtra(Constantes.NOTIFICATION_ID_STRING, conversationId);
    }

    private void sendNotification(
            int conversationId,
            String sender,
            String message,
            String channel,
            int icon,
            boolean hasReply,
            long timestamp) {

        createNotificationChannel();

        // A pending Intent for reads.
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                conversationId,
                getMessageReadIntent(conversationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Building a Pending Intent for the reply action to trigger.
        PendingIntent replyIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                conversationId,
                getMessageReplyIntent(conversationId, channel),
                PendingIntent.FLAG_UPDATE_CURRENT);

        /// TODO: Add the code to create the UnreadConversation.
        // Build a RemoteInput for receiving voice input from devices.
        RemoteInput remoteInput = new RemoteInput.Builder(Constantes.VOICE_REPLY).build();

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(sender)
                        .setLatestTimestamp(timestamp)
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        // Note: Add messages from oldest to newest to the UnreadConversation.Builder.
        // Since we are sending a single message here we simply add the message.
        // In a real world application there could be multiple unread messages which
        // should be ordered and added from oldest to newest.
        unreadConversationBuilder.addMessage(message);
        /// End create UnreadConversation

        // TODO: Add the code to allow inline reply on Wear 2.0.
        // Wear 2.0 allows for inline actions, which will be used for "reply"
        NotificationCompat.Action.WearableExtender inlineActionForWear2 =
                new NotificationCompat.Action.WearableExtender()
                        .setHintDisplayActionInline(true)
                        .setHintLaunchesActivity(false);
        /// End inline action for Wear 2.0.

        // 2. Build action
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, "Reply", getReplyPendingIntent(channel))
                .addRemoteInput(remoteInput)
                .extend(inlineActionForWear2) // TODO: Add better Wear support.
                .setAllowGeneratedReplies(true)
                .build();

        // Creamos la notificacion
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channel)  // Obtenemos el contexto y le asignamos a que grupo de notificaciones pertenece
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setSmallIcon(R.mipmap.notification_small_icon_white)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), icon))
                        .setWhen(timestamp)
                        .setColor(Color.BLACK)  // Se establece el color de fondo de la notificacion cuando se use el smallIcon
                        .setAutoCancel(true)
                        .setContentTitle(sender)
                        .setContentText(message)
                        .setContentIntent(readPendingIntent)
                        /// TODO: Extend the notification with CarExtender.
                        .extend(new NotificationCompat.CarExtender()
                                .setUnreadConversation(unreadConversationBuilder.build()));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        if (hasReply) {  // Si se ha indicado que la notificacion debe tener respuesta se le añade
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) builder.addAction(replyAction);  // Aunque se quiera respuesta, solo se permite para versiones superiores a Android N
        }

        Log.d(TAG, "Sending notification " + conversationId + " conversation: " + message);
        NotificationManagerCompat.from(this).notify(conversationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelGas = new NotificationChannel(Constantes.CHANNEL_GAS, Constantes.CHANNEL_GAS, NotificationManager.IMPORTANCE_HIGH);
            channelGas.setDescription("Recibir notificaciones de los sensores de gas");

            NotificationChannel channelHumidity = new NotificationChannel(Constantes.CHANNEL_HUMIDITY, Constantes.CHANNEL_HUMIDITY, NotificationManager.IMPORTANCE_HIGH);
            channelHumidity.setDescription("Recibir notificaciones de los sensores de humedad");

            NotificationChannel channelLight = new NotificationChannel(Constantes.CHANNEL_LIGHT, Constantes.CHANNEL_LIGHT, NotificationManager.IMPORTANCE_HIGH);
            channelLight.setDescription("Recibir notificaciones si se enciende o se apaga la luz");

            NotificationChannel channelPlug = new NotificationChannel(Constantes.CHANNEL_PLUG, Constantes.CHANNEL_PLUG, NotificationManager.IMPORTANCE_HIGH);
            channelPlug.setDescription("Recibir notificaciones si se enciende o se apaga el enchufe");

            NotificationChannel channelTemperature = new NotificationChannel(Constantes.CHANNEL_TEMPERATURE, Constantes.CHANNEL_TEMPERATURE, NotificationManager.IMPORTANCE_HIGH);
            channelTemperature.setDescription("Recibir notificaciones de los sensores de temperatura");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelGas);
            notificationManager.createNotificationChannel(channelHumidity);
            notificationManager.createNotificationChannel(channelLight);
            notificationManager.createNotificationChannel(channelPlug);
            notificationManager.createNotificationChannel(channelTemperature);
        }
    }

    private PendingIntent getReplyPendingIntent(String channel) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // start a
            // (i)  broadcast receiver which runs on the UI thread or
            // (ii) service for a background task to b executed , but for the purpose of this codelab, will be doing a broadcast receiver
            intent = MessageReplyReceiver.getReplyMessageIntent(getApplicationContext(), Constantes.NOTIFICATION_ID_INT, 1);
            intent.putExtra("channel", channel);
            return PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else {
            // start your activity
            intent = MessageReplyReceiver.getReplyMessageIntent(getApplicationContext(), Constantes.NOTIFICATION_ID_INT, 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("channel", channel);
            return PendingIntent.getActivity(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}

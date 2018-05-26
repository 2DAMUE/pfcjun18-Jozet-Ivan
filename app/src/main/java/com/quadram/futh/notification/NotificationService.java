package com.quadram.futh.notification;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.quadram.futh.helper.Constantes;

public class NotificationService extends IntentService {
    private static final String TAG = NotificationService.class.getSimpleName();

    public NotificationService() {
        super(NotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Handle intent to send a new notification.
        if (intent != null && Constantes.SEND_MESSAGE_ACTION.equals(intent.getAction())) {

            sendNotification(
                    Constantes.CONVERSATION_ID_INT,
                    intent.getStringExtra("title"),
                    intent.getStringExtra("text"),
                    intent.getIntExtra("icon", android.R.drawable.sym_def_app_icon),
                    System.currentTimeMillis());
        }
    }

    // Creates an intent that will be triggered when a message is read.
    private Intent getMessageReadIntent(int id) {
        return new Intent().setAction(Constantes.READ_ACTION).putExtra(Constantes.CONVERSATION_ID_STRING, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent().setAction(Constantes.REPLY_ACTION).putExtra(Constantes.CONVERSATION_ID_STRING, conversationId);
    }

    private void sendNotification(
            int conversationId,
            String sender,
            String message,
            int icon,
            long timestamp) {

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
                getMessageReplyIntent(conversationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        /// TODO: Add the code to create the UnreadConversation.
        // Build a RemoteInput for receiving voice input from devices.
        RemoteInput remoteInput = new RemoteInput.Builder(Constantes.EXTRA_VOICE_REPLY).build();

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

        // Add an action to allow replies.
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        android.R.drawable.stat_notify_chat,
                        "Reply",
                        replyIntent)
                        /// TODO: Add better wear support.
                        .addRemoteInput(remoteInput)
                        .extend(inlineActionForWear2)
                        .build();


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "CHANNEL")
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), icon))
                        .setWhen(timestamp)
                        .setColor(Color.YELLOW)
                        .addAction(replyAction)
                        .setContentTitle(sender)
                        .setContentIntent(readPendingIntent)
                        /// TODO: Extend the notification with CarExtender.
                        .extend(new NotificationCompat.CarExtender()
                                .setUnreadConversation(unreadConversationBuilder.build()));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        /// End

        Log.d(TAG, "Sending notification " + conversationId + " conversation: " + message);
        NotificationManagerCompat.from(this).notify(conversationId, builder.build());
    }
}

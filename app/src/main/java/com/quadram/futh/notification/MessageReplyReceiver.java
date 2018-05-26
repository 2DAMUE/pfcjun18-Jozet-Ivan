package com.quadram.futh.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.quadram.futh.helper.Constantes;

public class MessageReplyReceiver extends BroadcastReceiver {
    private static final String TAG = MessageReplyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constantes.REPLY_ACTION.equals(intent.getAction())) {
            int conversationId = intent.getIntExtra(Constantes.CONVERSATION_ID_STRING, -1);
            CharSequence reply = getMessageText(intent);
            if (conversationId != -1) {
                Log.d(TAG, "Got reply (" + reply + ") for ConversationId " + conversationId);
            }
            // Tell the Service to send another message.
            Intent serviceIntent = new Intent(context, MessageReadReceiver.class);
            serviceIntent.setAction(Constantes.REPLY_ACTION);
            context.startService(serviceIntent);
        }
    }

    /**
     * Get the message text from the intent.
     * Note that you should call {@code RemoteInput#getResultsFromIntent(intent)} to process
     * the RemoteInput.
     */
    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(Constantes.EXTRA_VOICE_REPLY);
        }
        return null;
    }
}

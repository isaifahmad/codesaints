package com.pathways.conversation.ui;

import android.content.Context;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class ChatMessageFactory {

    public static final int INCOMING_TEXT_MESSAGE_TIPPED = 1;
    public static final int OUTGOING_TEXT_MESSAGE_TIPPED = 2;

    public static BaseMessageViewSetter<?, ?> getRowViewSetter(Context context,
                                                               UIChatMessage message) {

        switch (message.getType()) {
            case INCOMING_TEXT_MESSAGE_TIPPED:
                return new IncomingMessageViewSetter(context, (UIIncomingChatMessage) message);
            case OUTGOING_TEXT_MESSAGE_TIPPED:
                return new OutgoingMessageViewSetter(context, (UIOutgoingChatMessage) message);
        }
        return null;
    }

}

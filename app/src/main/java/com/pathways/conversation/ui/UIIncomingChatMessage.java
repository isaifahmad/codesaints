package com.pathways.conversation.ui;

import static com.pathways.conversation.ui.ChatMessageFactory.INCOMING_TEXT_MESSAGE_TIPPED;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class UIIncomingChatMessage extends UIChatMessage {

    public UIIncomingChatMessage(String message) {
        super(message);
    }

    @Override
    public int getType() {
        return INCOMING_TEXT_MESSAGE_TIPPED;
    }
}

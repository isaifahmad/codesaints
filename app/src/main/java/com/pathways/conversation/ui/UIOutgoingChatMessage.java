package com.pathways.conversation.ui;

import static com.pathways.conversation.ui.ChatMessageFactory.OUTGOING_TEXT_MESSAGE_TIPPED;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class UIOutgoingChatMessage extends UIChatMessage {

    public UIOutgoingChatMessage(String message) {
        super(message);
    }

    @Override
    public int getType() {
        return OUTGOING_TEXT_MESSAGE_TIPPED;
    }
}

package com.pathways.conversation.ui;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public abstract class UIChatMessage {

    private String message;

    public UIChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public abstract int getType();
}

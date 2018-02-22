package com.pathways.conversation.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class ChatAdapter extends BaseAdapter {

    private Context context;
    private List<UIChatMessage> messagesList = new ArrayList<>();

    public ChatAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (null != messagesList) {
            return messagesList.size();
        }
        return 0;
    }

    @Override
    public UIChatMessage getItem(int position) {
        return messagesList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        UIChatMessage message = getItem(position);
        BaseMessageViewSetter<?, ?> viewSetter = ChatMessageFactory
                .getRowViewSetter(context, message);
        return viewSetter.getView(position, convertView, viewGroup);
    }

    public void appendMessages(UIChatMessage message) {
        // IrisChatScreenUIModelFactory.appendUIChatMessages(messages, this.messagesList);
        messagesList.add(message);
        notifyDataSetChanged();
    }
}

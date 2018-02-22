package com.pathways.conversation.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.pathways.R;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class IncomingMessageViewSetter extends BaseMessageViewSetter<UIIncomingChatMessage, IncomingRowViewHolder> {


    public IncomingMessageViewSetter(Context context, UIIncomingChatMessage data) {
        super(context, data);
    }

    protected View buildView() {
        return getInflater().
                inflate(R.layout.iris_chat_tipped_incoming_message, null);
    }


    protected IncomingRowViewHolder buildHolder(View convertView) {
        return new IncomingRowViewHolder(convertView);
    }

    @Override
    protected void setViews(IncomingRowViewHolder holder, int position) {
        holder.textView.setText(getData().getMessage());
    }
}

class IncomingRowViewHolder extends BaseMessageViewSetter.BaseViewHolder {

    public TextView textView;

    public IncomingRowViewHolder(View rowView) {
        super(rowView);
        textView = (TextView) rowView.findViewById(R.id.message_textview);
    }


}

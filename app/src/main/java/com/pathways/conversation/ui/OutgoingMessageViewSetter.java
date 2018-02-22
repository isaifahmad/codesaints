package com.pathways.conversation.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.pathways.R;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class OutgoingMessageViewSetter extends BaseMessageViewSetter<UIOutgoingChatMessage, OutgoingRowViewHolder> {

    public OutgoingMessageViewSetter(Context context, UIOutgoingChatMessage data) {
        super(context, data);
    }

    protected View buildView() {
        return getInflater().
                inflate(R.layout.iris_chat_tipped_outgoing_message, null);
    }


    protected OutgoingRowViewHolder buildHolder(View convertView) {
        return new OutgoingRowViewHolder(convertView);

    }

    @Override
    protected void setViews(OutgoingRowViewHolder holder, int position) {
        holder.textView.setText(getData().getMessage());
    }

}

class OutgoingRowViewHolder extends BaseMessageViewSetter.BaseViewHolder {

    public TextView textView;

    public OutgoingRowViewHolder(View rowView) {
        super(rowView);
        textView = (TextView) rowView.findViewById(R.id.message_textview);
    }
}

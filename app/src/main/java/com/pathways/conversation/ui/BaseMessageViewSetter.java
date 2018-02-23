package com.pathways.conversation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public abstract class BaseMessageViewSetter<BCM extends UIChatMessage, BVH extends BaseMessageViewSetter.BaseViewHolder> {

    private Context context;
    private BCM data;
    private LayoutInflater inflater;

    public BaseMessageViewSetter(Context context, BCM data) {
        this.context = context;
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    public BCM getData() {
        return data;
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        BVH holder;
        convertView = buildView();
        holder = buildHolder(convertView);
        convertView.setTag(holder);
        setViews(holder, position);
        return convertView;
    }


    protected abstract View buildView();

    protected abstract BVH buildHolder(View convertView);

    protected abstract void setViews(BVH holder, int position);

    public static abstract class BaseViewHolder {

        public BaseViewHolder(View convertView) {

        }
    }
}

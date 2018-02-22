package com.pathways.conversation.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class RobotoRegularTextView extends CustomTextView {

    public static final String FONT_NAME = "fonts/Roboto-Regular.ttf";

    public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public RobotoRegularTextView(Context context) {
        super(context);
    }



    @Override
    protected String getFontName() {
        return FONT_NAME;
    }
}


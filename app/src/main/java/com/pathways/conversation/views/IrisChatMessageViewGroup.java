package com.pathways.conversation.views;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pathways.R;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class IrisChatMessageViewGroup extends ViewGroup {

    private static final int UNKNOWN = -1;
    private static final float FRACTION_BELOW_TEXT = 0.2f;
    private static final int PADDING_BETWEEN_TEXT_TIME_DP = 8;
    private static final float STATUS_IMAGE_VIEW_BOTTOM_CORRECTION_SP = 1.5f;

    private final int DISPLAY_TEXT_MAX_WIDTH;
    private final int PADDING_BETWEEN_TEXT_TIME_PIXEL;
    private final int TIME_STATUS_TOP_MARGIN;

    private TextView displayTextView;


    private int displayTextPaddingLeft;
    private int displayTextPaddingRight;
    private int displayTextPaddingBottom;

    private int statusImageViewWidth = UNKNOWN;
    private int statusImageViewHeight = UNKNOWN;

    public IrisChatMessageViewGroup(Context context) {
        this(context, null);
    }

    public IrisChatMessageViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IrisChatMessageViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DISPLAY_TEXT_MAX_WIDTH = context.getResources()
                .getDimensionPixelSize(R.dimen.chat_display_text_max_width);
        PADDING_BETWEEN_TEXT_TIME_PIXEL = dpToPx(context, PADDING_BETWEEN_TEXT_TIME_DP);

        TIME_STATUS_TOP_MARGIN = dpToPx(getContext(), 3);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        displayTextView = (TextView) findViewById(R.id.message_textview);
        displayTextPaddingLeft = displayTextView.getPaddingLeft();
        displayTextPaddingRight = displayTextView.getPaddingRight();
        displayTextPaddingBottom = displayTextView.getPaddingBottom();
    }

    public TextView getDisplayTextView() {
        return displayTextView;
    }

    public void setValues(String displayString, String displayTime) {
        displayTextView.setText(displayString);
        requestLayout();
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        mesureViewAsUnspecified(displayTextView);
        int displayTextWidth = displayTextView.getMeasuredWidth();
        int displayTextHeight = displayTextView.getMeasuredHeight();


        int layoutHeight = (int) (displayTextHeight + FRACTION_BELOW_TEXT );
        int layoutWidth;

        final int TOTAL_HORIZONTAL_PADDING = getDisplayTextTotalHorizontalPadding();
        final int TOTAL_TIME_STATUS_WIDTH =  statusImageViewWidth;

        final int LAST_CHAR_POSITION = getDisplayTextLastCharPosition();

        if (LAST_CHAR_POSITION + TOTAL_HORIZONTAL_PADDING +
                PADDING_BETWEEN_TEXT_TIME_PIXEL + TOTAL_TIME_STATUS_WIDTH > DISPLAY_TEXT_MAX_WIDTH) {
            // There was no enough space in this line, so moving to next line.
            // Dimens calculations
            layoutWidth = DISPLAY_TEXT_MAX_WIDTH;
            layoutHeight += Math.max(0, statusImageViewHeight) +
                    TIME_STATUS_TOP_MARGIN;
            // Y calculations
        } else {
            // There is some space left at end of current line, so fitting elements in same line
            // Dimens calculations
            if (displayTextView.getLineCount() > 1) {
                layoutWidth = DISPLAY_TEXT_MAX_WIDTH;
            } else {
                layoutWidth = LAST_CHAR_POSITION + TOTAL_HORIZONTAL_PADDING +
                        PADDING_BETWEEN_TEXT_TIME_PIXEL +
                        TOTAL_TIME_STATUS_WIDTH;
            }
            // Y calculations
        }
        // X calculations
        // Y calculations
        // To align status icon at baseline of time textview.
        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        displayTextView.layout(0, 0, displayTextView.getMeasuredWidth(),
                displayTextView.getMeasuredHeight());
    }

    private static float calculateLastWordXPostion(String showString, Paint paint, int maxWidth) {
        float position = 0.0f;
        String[] words = showString.split(" ");
        float currentSize;
        final int SIZE = words.length;
        for (int index = 0; index < words.length; index++) {
            String word = words[index];
            if (index != SIZE - 1) {
                word += " ";
            }
            currentSize = paint.measureText(word);
            final float newSize = position + currentSize;
            if (newSize > maxWidth) {
                // When adding the new word into current position, if it exceeds total max width
                if (currentSize > maxWidth) {
                    // If current word is greater than max width, the word will be placed
                    // automatically in the new line & will be wrapped in coming lines so Mod is
                    // used.
                    position = currentSize % maxWidth;
                } else {
                    // If current word is less than maxwidth but excedding total maxwidth in that
                    // case, it will be placed in new line & will complete its last position in
                    // same line so position = current size of that line.
                    position = currentSize;
                }
            } else if (newSize == maxWidth) {
                // If adding a new word fits exactly in max width that means current is
                // completely exausted & position should point to end of line.
                position = maxWidth;
            } else {
                // If new word size has not reached the max width yet, that case is a normal
                // incremental case.
                position = position + currentSize;
            }
        }
        return position;
    }

    private boolean isStatusImageDimenCalculationNeeded() {
        return statusImageViewWidth == UNKNOWN && statusImageViewHeight == UNKNOWN;
    }


    private void mesureViewAsUnspecified(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    private void measureViewAsExact(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(view.getLayoutParams().width, MeasureSpec.EXACTLY),
                MeasureSpec
                        .makeMeasureSpec(view.getLayoutParams().height, MeasureSpec.EXACTLY));
    }

    private int getDisplayTextLastCharPosition() {
        return (int) Math.ceil(calculateLastWordXPostion((String) displayTextView.getText(),
                displayTextView.getPaint(),
                DISPLAY_TEXT_MAX_WIDTH -
                        getDisplayTextTotalHorizontalPadding()));
    }

    private int getDisplayTextTotalHorizontalPadding() {
        return displayTextPaddingLeft + displayTextPaddingRight;
    }

    private static int dpToPx(Context context, float value) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) Math.ceil(value * scale);
    }

    private static int spToPx(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) Math.ceil(value * scale);
    }
}


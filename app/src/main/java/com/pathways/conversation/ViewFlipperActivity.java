package com.pathways.conversation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.pathways.R;

/**
 * Created by amandeepsingh on 23/02/18.
 */

public class ViewFlipperActivity extends Activity {
    private ViewFlipper viewFlipper;
    public static final String CHECK = "check";
    int[] images_4bhk = {R.drawable.bhk_42_1, R.drawable.bhk_42_2, R.drawable.bhk_42_3};
    int[] images_5bhk = {R.drawable.bhk_42_1, R.drawable.bhk_42_2, R.drawable.bhk_42_3};
    int[] images_6bhk = {R.drawable.bhk_42_1, R.drawable.bhk_42_2, R.drawable.bhk_42_3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flipper);
        viewFlipper = findViewById(R.id.viewFlipper);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String check = getIntent().getExtras().getString(CHECK);

        switch (check) {
            case "4 BHK":
                for (int i = 0; i < images_4bhk.length; i++) {
                    View view = inflater.inflate(R.layout.flipper_layout, null, false);
                    ImageView imageView = view.findViewById(R.id.imageView);
                    imageView.setImageDrawable(getResources().getDrawable(images_4bhk[i]));
                    viewFlipper.addView(view);
                }
                break;

            case "5 BHK":
                for (int i = 0; i < images_5bhk.length; i++) {
                    View view = inflater.inflate(R.layout.flipper_layout, null, false);
                    ImageView imageView = view.findViewById(R.id.imageView);
                    imageView.setImageDrawable(getResources().getDrawable(images_5bhk[i]));
                    viewFlipper.addView(view);
                }
                break;

            case "6 BHK":
                for (int i = 0; i < images_6bhk.length; i++) {
                    View view = inflater.inflate(R.layout.flipper_layout, null, false);
                    ImageView imageView = view.findViewById(R.id.imageView);
                    imageView.setImageDrawable(getResources().getDrawable(images_6bhk[i]));
                    viewFlipper.addView(view);
                }
                break;

        }

        viewFlipper.startFlipping();
        viewFlipper.setFlipInterval(2000);
    }

}
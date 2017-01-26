package com.point.eslee.health_free;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by eslee on 2017-01-26.
 */

public class CustomTextView extends TextView implements View.OnTouchListener {
    public CustomTextView(Context context) {
        super(context);
        setOnTouchListener(this);
    }
    public CustomTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            Log.d("tag", "DOWN");
            view.setPressed(true);
        }
        else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            Log.d("tag", "UP");
            view.setPressed(false);
        }
        return true;
    }
}

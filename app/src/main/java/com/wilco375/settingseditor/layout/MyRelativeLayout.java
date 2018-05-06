package com.wilco375.settingseditor.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * RelativeLayout with a listener implementation for {@link #onInterceptTouchEvent(MotionEvent)}
 */
public class MyRelativeLayout extends RelativeLayout{
    public OnInterceptTouchListener onInterceptTouchListener;

    public MyRelativeLayout(Context context) {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(onInterceptTouchListener != null) this.onInterceptTouchListener.onInterceptTouch(this, ev);

        return super.onInterceptTouchEvent(ev);
    }

    public interface OnInterceptTouchListener {
        void onInterceptTouch(View v, MotionEvent ev);
    }

    public void setOnInterceptTouchListener(@Nullable OnInterceptTouchListener l) {
        onInterceptTouchListener = l;
    }
}

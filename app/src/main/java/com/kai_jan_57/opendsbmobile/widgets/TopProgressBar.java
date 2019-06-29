package com.kai_jan_57.opendsbmobile.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

public class TopProgressBar extends ProgressBar {

    public TopProgressBar(Context pContext) {
        this(pContext, null);
    }

    public TopProgressBar(Context pContext, AttributeSet attrs) {
        super(pContext, attrs);
        init();
    }

    public TopProgressBar(Context pContext, AttributeSet attrs, int defStyleAttr) {
        super(pContext, attrs, defStyleAttr);
        init();
    }

    public TopProgressBar(Context pContext, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(pContext, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {

            private boolean mEventFired = false;

            @Override
            public void onDraw() {
                if (!mEventFired) {
                    mEventFired = true;
                    setTranslationY(-getHeight());
                }
            }
        });
    }

    public void hide() {
        animate().translationY(-getHeight()).alpha(0).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    public void show() {
        animate().translationY(0).alpha(1).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

}

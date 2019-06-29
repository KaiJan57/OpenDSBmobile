package com.kai_jan_57.opendsbmobile.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.ortiz.touchview.TouchImageView;

import androidx.viewpager.widget.ViewPager;

public class ZoomableViewPager extends ViewPager {

    private int mCurrentPosition;

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public ZoomableViewPager(Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public ZoomableViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private final GestureDetector singleTapDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(final MotionEvent pMotionEvent) {
            callOnClick();
            return true;
        }
    });

    @Override
    public boolean onInterceptTouchEvent(MotionEvent pMotionEvent) {
        singleTapDetector.onTouchEvent(pMotionEvent);
        return super.onInterceptTouchEvent(pMotionEvent);
    }

    @Override
    protected boolean canScroll(View view, boolean checkV, int dx, int x, int y) {
        if (view instanceof TouchImageView) {
            return ((TouchImageView) view).canScrollHorizontallyFroyo(-dx);
        } else {
            return super.canScroll(view, checkV, dx, x, y);
        }
    }

}

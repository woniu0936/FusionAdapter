package com.fusion.example.feature.lab.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class BannerContainer extends FrameLayout {

    private float startX, startY;
    private final int touchSlop;
    private ViewPager2 viewPager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAutoScrollEnabled = true;
    private long autoScrollInterval = 3000;

    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && isAutoScrollEnabled && viewPager.getAdapter() != null) {
                int count = viewPager.getAdapter().getItemCount();
                if (count > 1) {
                    int next = (viewPager.getCurrentItem() + 1) % count;
                    viewPager.setCurrentItem(next, true);
                }
                handler.postDelayed(this, autoScrollInterval);
            }
        }
    };

    public BannerContainer(@NonNull Context context) {
        this(context, null);
    }

    public BannerContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void attachViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopAutoScroll();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    startAutoScroll();
                }
            }
        });
        startAutoScroll();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleNestedScroll(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleNestedScroll(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                // Do NOT disallow intercept here
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - startX);
                float dy = Math.abs(ev.getY() - startY);
                if (dx > touchSlop && dx > dy) {
                    // Horizontal scroll - claim it
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false); // Reset
                break;
        }
    }
    
    // Also handle dispatchTouchEvent to catch events before children if needed? 
    // Usually onIntercept is enough for ViewGroup, but since ViewPager2 consumes events, 
    // we strictly need to ensure parent doesn't steal it *before* ViewPager2 gets it.
    // The requestDisallowInterceptTouchEvent(true) on DOWN is key.

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoScroll();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScroll();
    }

    private void startAutoScroll() {
        stopAutoScroll();
        if (isAutoScrollEnabled) {
            handler.postDelayed(autoScrollRunnable, autoScrollInterval);
        }
    }

    private void stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable);
    }
}

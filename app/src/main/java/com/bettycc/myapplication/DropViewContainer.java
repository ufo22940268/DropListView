package com.bettycc.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by ccheng on 12/25/14.
 */
public class DropViewContainer extends ListView {

    private static final float SCROLL_FACTOR = 0.9f;
    private final DropView mDropView;
    private OnRefreshListener mOnRefreshListener;

    private final android.view.GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (getFirstVisiblePosition() == 0) {
                updateHeaderHeight((int) (mDropView.getBottom() + (-distanceY)));
            } else {
                updateHeaderHeight(0);
            }
            return true;
        }
    };

    private final GestureDetector mGestureDetector;
    private final View mHeaderView;
    private final float mDropViewHeight;
    private final float mPullOffset;
    private ValueAnimator mRestoreAnimator;

    public DropViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDropViewHeight = getResources().getDimension(R.dimen.drop_view_height);
        mPullOffset = getResources().getDimension(R.dimen.pull_offset);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mHeaderView = LayoutInflater.from(context).inflate(
                R.layout.item_drop_view, this, false);
        mDropView = (DropView) mHeaderView.findViewById(R.id.drop);
        addHeaderView(mHeaderView);
        updateHeaderHeight(0);
    }

    private void updateHeaderHeight(int i) {
        mDropView.getLayoutParams().height = i;
        mDropView.setScroll((int) ((i - mPullOffset) * SCROLL_FACTOR));
        mDropView.requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRestoring()) {
            return true;
        }
        mGestureDetector.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            onPullRelease();
        }

        return super.onTouchEvent(ev);
    }

    private boolean isRestoring() {
        return mRestoreAnimator != null && mRestoreAnimator.isRunning();
    }

    private void onPullRelease() {
        int bottom = mHeaderView.getBottom();
        if (getFirstVisiblePosition() == 0 && bottom > 0) {
            int to;
            if (bottom < mPullOffset || mDropView.getMode() == DropView.Mode.PULL) {
                to = 0;
            } else {
                to = (int) mPullOffset;
            }
            mRestoreAnimator = getHeaderScrollAnimator(to);
            mRestoreAnimator.start();
            mRestoreAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onPullDownToRefresh();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onPullDownToRefresh();
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    private ValueAnimator getHeaderScrollAnimator(int to) {
        ValueAnimator restoreAnimator = ValueAnimator.ofFloat(mHeaderView.getBottom(), to);
        restoreAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        restoreAnimator.setInterpolator(new AccelerateInterpolator());
        restoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = ((Float) (animation.getAnimatedValue())).floatValue();
                updateHeaderHeight((int) v);
            }
        });
        return restoreAnimator;
    }

    public OnRefreshListener getOnRefreshListener() {
        return mOnRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public void onRefreshCompleted() {
        mRestoreAnimator = getHeaderScrollAnimator(0);
        mRestoreAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mDropView.reset();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mDropView.reset();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mRestoreAnimator.start();
    }

    public static interface OnRefreshListener {
        public void onPullDownToRefresh();
    }
}

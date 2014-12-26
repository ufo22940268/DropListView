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
                int headerHeight = (int) (mDropView.getBottom() + (-distanceY));
                updateHeaderHeight(headerHeight);
                updateDropviewScroll(headerHeight);
            } else {
                updateHeaderHeight(0);
                updateDropviewScroll(0);
            }
            onDropviewScrollUpdated();
            return true;
        }
    };

    private final GestureDetector mGestureDetector;
    private final View mHeaderView;
    private final float mDropViewHeight;
    private final float mLoadingHeaderHeight;
    private ValueAnimator mRestoreAnimator;
    private ValueAnimator mScrollToLoadingAnimator;

    public DropViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDropViewHeight = getResources().getDimension(R.dimen.drop_view_height);
        mLoadingHeaderHeight = getResources().getDimension(R.dimen.drop_view_loading_header_height);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mHeaderView = LayoutInflater.from(context).inflate(
                R.layout.item_drop_view, this, false);
        mDropView = (DropView) mHeaderView.findViewById(R.id.drop);
        addHeaderView(mHeaderView);
        updateHeaderHeight(0);
        updateDropviewScroll(0);
    }

    private void updateHeaderHeight(int headerHeight) {
        mDropView.getLayoutParams().height = headerHeight;
        mDropView.requestLayout();
    }

    private void updateDropviewScroll(int headerHeight) {
        mDropView.setScroll(convertHeaderHeightToScroll(headerHeight));
    }

    private int convertHeaderHeightToScroll(int i) {
        return (int) ((i - mLoadingHeaderHeight) * SCROLL_FACTOR);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRestoreAnimatorRunning()) {
            return true;
        }

        if (mDropView.getMode() == DropView.Mode.PULL && !isScrollToLoadingAnimatorRunning()) {
            mGestureDetector.onTouchEvent(ev);
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            onPullRelease();
        }

        return super.onTouchEvent(ev);
    }

    private boolean isRestoreAnimatorRunning() {
        return mRestoreAnimator != null && mRestoreAnimator.isRunning();
    }

    private void onPullRelease() {
        int to;
        if (pullOverThreshold()) {
            to = (int) mLoadingHeaderHeight;
        } else {
            to = 0;
        }
        mRestoreAnimator = getHeaderScrollAnimator(to, new OnHeaderHeightUpdatedListener() {
            @Override
            public void onHeightUpdated(int height) {
                updateHeaderHeight(height);
            }
        });
        mRestoreAnimator.start();
    }

    private boolean pullOverThreshold() {
        return convertHeaderHeightToScroll(getHeaderBottom()) > mDropView.getPullThreshold();
    }

    private void onDropviewScrollUpdated() {
        int bottom = mHeaderView.getBottom();
        if (getFirstVisiblePosition() == 0
                && bottom > 0
                && mDropView.getMode() == DropView.Mode.PULL
                && !isScrollToLoadingAnimatorRunning()
                && convertHeaderHeightToScroll(bottom) > mDropView.getPullThreshold()) {
            mScrollToLoadingAnimator = getDropViewScrollAnimator((int) mLoadingHeaderHeight);
            mScrollToLoadingAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onPullDownToRefresh();
                        mDropView.onLoading();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mScrollToLoadingAnimator.start();
        }
    }

    private boolean isScrollToLoadingAnimatorRunning() {
        return mScrollToLoadingAnimator != null && mScrollToLoadingAnimator.isRunning();
    }

    private ValueAnimator getHeaderScrollAnimator(int to, final OnHeaderHeightUpdatedListener headerHeightUpdatedListener) {
        ValueAnimator restoreAnimator = ValueAnimator.ofFloat(mHeaderView.getBottom(), to);
        restoreAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        restoreAnimator.setInterpolator(new AccelerateInterpolator());
        restoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = ((Float) (animation.getAnimatedValue())).floatValue();
                headerHeightUpdatedListener.onHeightUpdated((int) v);
            }
        });
        return restoreAnimator;
    }

    private ValueAnimator getDropViewScrollAnimator(int to) {
        ValueAnimator restoreAnimator = ValueAnimator.ofFloat(mHeaderView.getBottom(), to);
        restoreAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        restoreAnimator.setInterpolator(new AccelerateInterpolator());
        restoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = ((Float) (animation.getAnimatedValue())).floatValue();
                updateDropviewScroll((int) v);
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
        if (getHeaderBottom() > 0) {
            mRestoreAnimator = getHeaderScrollAnimator(0, new OnHeaderHeightUpdatedListener() {
                @Override
                public void onHeightUpdated(int height) {
                    updateHeaderHeight(height);
                }
            });
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
    }

    private int getHeaderBottom() {
        return mHeaderView.getBottom();
    }

    public static interface OnRefreshListener {
        public void onPullDownToRefresh();
    }

    public static interface OnHeaderHeightUpdatedListener {
        public void onHeightUpdated(int height);
    }
}

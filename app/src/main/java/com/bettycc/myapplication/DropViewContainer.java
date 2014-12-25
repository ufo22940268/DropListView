package com.bettycc.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by ccheng on 12/25/14.
 */
public class DropViewContainer extends ListView {

    private final DropView mDropView;

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
        if (i < mPullOffset) {
            mDropView.setScroll(0);
        } else {
            mDropView.setScroll((int) (i - mPullOffset));
        }
        mDropView.requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
}

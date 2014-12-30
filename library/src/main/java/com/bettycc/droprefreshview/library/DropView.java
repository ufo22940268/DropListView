package com.bettycc.droprefreshview.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


public class DropView extends FrameLayout {

    private static final float SHRINK_FACTOR = 0.7f;
    public static final float MIN_RADIUS1_FACTOR = 0.8f;
    public static final float MIN_RADIUS2_FACTOR = 0.3f;
    public static final int MAX_OFFSET_DEGREE = 30;
    private GestureDetector mGestureDetector;

    private int mDistanceY = 0;

    private int mRadius1;
    private int mRadius2;
    private float mMinRadius2;
    private int mTopPadding;
    private int mPullThreshold;
    private int mBzrOffset;
    private ProgressBar mLoadingView;
    private boolean mShowLoadingIcon = true;

    public void showLoadingIcon(boolean showLoadingIcon) {
        mShowLoadingIcon = showLoadingIcon;
    }

    enum Mode {
        NONE, PULL, LOADING;
    }

    private Mode mMode = Mode.PULL;
    private int mColor;
    private float mMinRadius1;
    private Bitmap mIconBitmap;
    private int mMaxRadius1;

    public ProgressBar getLoadingView() {
        return mLoadingView;
    }

    public void setLoadingView(ProgressBar loadingView) {
        mLoadingView = loadingView;
    }

    public void reset() {
        setStatus(Mode.PULL);
        enableLoading(false);
    }

    private void setStatus(Mode mode) {
        mMode = mode;
        invalidate();
    }

    public Mode getMode() {
        return mMode;
    }

    public int getPullThreshold() {
        return mPullThreshold;
    }

    public void setColor(int color) {
        mColor = color;
    }


    private android.view.GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mMode == Mode.PULL) {
                setDistanceY((int) (mDistanceY + distanceY));
                return true;
            } else {
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        }
    };

    public void onLoading() {
        mMode = Mode.LOADING;
        enableLoading(true);
    }

    public void setScroll(int i) {
        setDistanceY(-i);
    }

    public float getDistanceY() {
        return mDistanceY;
    }

    public void setDistanceY(int distanceY) {
        if (mMode == Mode.PULL) {
            if (distanceY < -mPullThreshold) {
                return;
            }

            mDistanceY = distanceY;
            if (mDistanceY > 0) {
                mDistanceY = 0;
            }

            onDistanceYChanged(mDistanceY);
            invalidate();
        }
    }



    private void onDistanceYChanged(int distanceY) {
        float v = (1 - getScrollPercent()) * mMaxRadius1;
        if (v < mMinRadius2) {
            v = mMinRadius2;
        }

        mRadius2 = (int) v;

        System.out.println("getScrollPercent() = " + getScrollPercent());
        v = mMinRadius1 + (mMaxRadius1 - mMinRadius1) * (1 - getScrollPercent());
        if (v < mMinRadius1) {
            v = mMinRadius1;
        }
        mRadius1 = (int) v;
    }
    enum Side {
        LEFT, RIGHT;
    }


    public DropView(Context context) {
        super(context);
        init(null, 0);
    }

    public DropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        mColor = getResources().getColor(R.color.drop_view_color);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mMaxRadius1 = dpToPx(25);
        mRadius1 = mMaxRadius1;
        mMinRadius1 = mRadius1 * MIN_RADIUS1_FACTOR;
        mRadius2 = mRadius1;
        mMinRadius2 = (float) (mRadius2 * MIN_RADIUS2_FACTOR);
        mTopPadding = dpToPx(15);
        mPullThreshold = dpToPx(100);
        mBzrOffset = dpToPx(10);

        mLoadingView = new ProgressBar(getContext());
        mLoadingView.setIndeterminate(true);
        int size = mRadius1 * 2 + dpToPx(5);
        LayoutParams params = new LayoutParams(size, size);
        params.topMargin = mTopPadding - dpToPx(5);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mLoadingView.setLayoutParams(params);

        mIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_refresh_white_48dp);

        /**
         * Adjust loading view position to fit drop view.
         */
        mLoadingView.setPadding(0, 0, dpToPx(15), 0);

        addView(mLoadingView);
        enableLoading(false);
    }

    private void enableLoading(boolean b) {
        mLoadingView.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mMode == Mode.PULL) {
            drawPull(canvas);
        }

        super.onDraw(canvas);
    }

    private void drawPull(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mColor);

        int cp1Y = mTopPadding + mRadius1;
        Point cp1 = new Point(getWidth() / 2, cp1Y);
        int cp2Y = cp1Y + (-mDistanceY);
        Point cp2 = new Point(getWidth() / 2, cp2Y);

        Path path = new Path();
        path.setFillType(Path.FillType.WINDING);
        RectF rectF = new RectF(cp1.x - mRadius1, cp1.y - mRadius1, cp1.x + mRadius1, cp1.y + mRadius1);
        int offsetDegree = (int) (MAX_OFFSET_DEGREE*getScrollPercent());
        double offsetRadians = Math.toRadians(offsetDegree);
        path.addArc(rectF, -180 - offsetDegree, 180 + 2*offsetDegree);

        /**
         * Draw loading icon.
         */
        RectF circleRect = new RectF(rectF);

        addBzr(path, cp1.x + mRadius1, cp1.y, cp2.x + mRadius2, cp2.y, Side.RIGHT);
        rectF = new RectF(cp2.x - mRadius2, cp2.y - mRadius2, cp2.x + mRadius2, cp2.y + mRadius2);
        path.addArc(rectF, 0, 180);
        addBzr(path, cp2.x - mRadius2, cp2.y,
                (float) (cp1.x - mRadius1*Math.cos(offsetRadians)), (float) (cp1.y + mRadius1*Math.sin(offsetRadians)), Side.LEFT);

        canvas.drawPath(path, paint);

        if (mShowLoadingIcon) {
            drawLoadingIcon(canvas, circleRect);
        }
    }

    private void drawLoadingIcon(Canvas canvas, RectF rectf) {
        canvas.save();
        canvas.translate(0, -dpToPx(2));
        int l = dpToPx(8), r = dpToPx(8), b = dpToPx(8);
        int t = dpToPx(8);
        Rect src = new Rect(0, 0, mIconBitmap.getWidth(), mIconBitmap.getHeight());
        Rect dst = new Rect((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.bottom);
        dst.left = dst.left + l;
        dst.top = dst.top + t;
        dst.right = dst.right - r;
        dst.bottom = dst.bottom - b;
        canvas.drawBitmap(mIconBitmap, src, dst, null);
        canvas.restore();
    }

    private void addBzr(Path path, float x, float y, float x1, float y1, Side side) {
        float i = (int) (getScrollPercent() * mBzrOffset);
        float mx;
        if (side == Side.RIGHT) {
            mx = (x + x1) / 2 - i;
        } else {
            mx = (x + x1) / 2 + i;
        }
        float my = (y + y1) / 2 - i;
        path.quadTo(mx, my, x1, y1);
    }

    private float getScrollPercent() {
        return (-getDistanceY()) / mPullThreshold;
    }

    public int dpToPx(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp * SHRINK_FACTOR);
    }
}

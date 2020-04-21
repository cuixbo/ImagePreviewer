package com.cuixbo.widget.image.previewer;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.OverScroller;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * 居中缩放至边缘
 * 滑动
 * 滚动
 * 双击缩放
 * 双指捏合缩放
 * （中心点缩放）
 * 滑动边界
 * 长图
 * 大图
 * 显示、消失动画
 * 需要考虑多点触控的情况
 *
 * @author xiaobocui
 * @date 2020/4/3
 */
public class ImagePreviewer extends AppCompatImageView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    Bitmap mBitmap;
    Paint mPaint;
    int mBitmapWidth, mBitmapHeight;
    int mWidth, mHeight;
    Rect srcRect, dstRect;
    GestureDetector mGestureDetector;

    // 缩放动画
    ObjectAnimator mScaleAnimator;
    OverScroller mScroller;

    int originOffsetX, originOffsetY;
    float originScale;
    float mCurrentScale;
    float mMaxScale = 8;

    public ImagePreviewer(Context context) {
        super(context);
        init();
    }

    public ImagePreviewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImagePreviewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_jy_1);
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        Log.e("xbc", "mBitmapWidth,mBitmapHeight:" + mBitmapWidth + "," + mBitmapHeight);
        srcRect = new Rect(0, 0, mBitmapWidth, mBitmapHeight);

        mGestureDetector = new GestureDetector(getContext(), this);
        mGestureDetector.setOnDoubleTapListener(this);
        mScroller = new OverScroller(getContext());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mWidth = w;
        mHeight = h;
        Log.e("xbc", "mWidth,mHeight:" + mWidth + "," + mHeight);

        originOffsetX = (getWidth() - mBitmap.getWidth()) / 2;
        originOffsetY = (getHeight() - mBitmap.getHeight()) / 2;


        if (1.0F * mBitmap.getHeight() / mBitmap.getWidth() > 1.0F * getHeight() / getWidth()) {
            // 长图
            originScale = 1.0F * getHeight() / mBitmap.getHeight();
        } else {
            // 宽图
            originScale = 1.0F * getWidth() / mBitmap.getWidth();
        }

        mCurrentScale = originScale;

        scaleCenterX = getWidth() / 2F;
        scaleCenterY = getHeight() / 2F;

        dstRect = new Rect(0, 0, mWidth, mHeight * mBitmapHeight / mBitmapWidth);
    }

    /**
     *
     */
    private float fraction;
    private float scaleBefore;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
         * 偏移系数（解决在缩放过程中，伴随的位移，使得动画效果更贴切）
         * 系数=已缩放了的scale/整体要缩放的scale 变化范围：[0,1]
         */
        float fraction = 1F * (scaleBefore - mCurrentScale) / (mMaxScale - originScale);
        Log.e("xbc", "mCurrentScale:" + mCurrentScale + ",fraction:" + fraction + ",scaleScrollX:" + scaleScrollX);
        // 画布偏移 = ScrolledX*fraction,ScrolledY*fraction  变化范围：[0,ScrolledX],[0,ScrolledY]
//        canvas.translate(scaleScrollX * fraction, scaleScrollY * fraction);
        canvas.scale(mCurrentScale, mCurrentScale, scaleCenterX, scaleCenterY);
        canvas.drawBitmap(mBitmap, originOffsetX, originOffsetY, mPaint);
        Log.e("xbc", "onDraw:" + getScrollX() + "," + getScrollY());
    }

    int downX, downY;
    int lastX, lastY;
    int dx, dy;
    int offsetX, offsetY;
    int lastOffsetX, lastOffsetY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void setCurrentScale(float currentScale) {
        mCurrentScale = currentScale;
        invalidate();
    }

    public float getCurrentScale() {
        return mCurrentScale;
    }

    private ObjectAnimator getScaleAnimator() {
        if (mScaleAnimator == null) {
            mScaleAnimator = ObjectAnimator.ofFloat(this, "currentScale", 0f);
        }
        mScaleAnimator.setFloatValues(originScale, mMaxScale);
        return mScaleAnimator;
    }

    /************* OnGestureListener Start *************/

    @Override
    public boolean onDown(MotionEvent e) {
        downX = (int) e.getX();
        downY = (int) e.getY();
//        if (!mScroller.isFinished()) {
//            mScroller.abortAnimation();
//        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.e("xbc", "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.e("xbc", "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.e("xbc", "onScroll:distanceX=" + distanceX + ",distanceY=" + distanceY);
//        offsetX += -distanceX;
//        offsetY += -distanceY;

        int maxDistanceX = (int) ((mBitmap.getWidth() * mCurrentScale - getWidth()) / 2);
        int maxDistanceY = (int) ((mBitmap.getHeight() * mCurrentScale - getHeight()) / 2);

        Log.e("xbc", "onScroll:maxDistanceX=" + maxDistanceX + ",maxDistanceY=" + maxDistanceY + ",scrollX=" + getScrollX() + ",scrollY=" + getScrollY());
        if (Math.abs(getScrollX()) >= maxDistanceX) {
            distanceX = 0;
        }

        if (Math.abs(getScrollY()) >= maxDistanceY) {
            distanceY = 0;
        }
        scrollBy((int) distanceX, (int) distanceY);
        Log.e("xbc", "onScroll:offsetX=" + offsetX + ",offsetY=" + offsetY + ",scrollX=" + getScrollX());
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.e("xbc", "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e("xbc", "onFling:velocityX=" + velocityX + ",velocityY=" + velocityY);
        if (mCurrentScale == originScale) {
            // 原始缩放比例，则不可以fling
            return false;
        }
        int maxDistanceX = (int) ((mBitmap.getWidth() * mCurrentScale - getWidth()) / 2);
        int maxDistanceY = (int) ((mBitmap.getHeight() * mCurrentScale - getHeight()) / 2);
        int minX = -maxDistanceX;
        int minY = -maxDistanceY;
        int maxX = maxDistanceX;
        int maxY = maxDistanceY;
        mScroller.fling(
                getScrollX(), getScrollY(),
                (int) -velocityX, (int) -velocityY,
                minX, maxX,
                minY, maxY,
                100, 100
        );
        invalidate();
        return false;
    }

    /************* OnGestureListener End *************/

    /************* OnDoubleTapListener Start *************/

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.e("xbc", "onSingleTapConfirmed");
        return false;
    }

    int scaleScrollX, scaleScrollY;
    float scaleCenterX, scaleCenterY;

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.e("xbc", "onDoubleTap");
        if (mCurrentScale == mMaxScale) {
            // todo 需要处理缩放过程中的，位置偏移(双击指定位置，以此为中心放大)
//            scrollTo(0, 0);
            Log.e("xbc", "onDoubleTap:" + getScrollX() + "," + getScrollY());
            scaleScrollX = getScrollX();
            scaleScrollY = getScrollY();
            scaleBefore = mCurrentScale;
            getScaleAnimator().reverse();
            mScroller.startScroll(getScrollX(), getScrollY(), -getScrollX(), -getScrollY());
            invalidate();
        } else {
            // todo 需要处理缩放过程中的，位置偏移(双击指定位置，以此为中心放大)
            scaleScrollX = 0;
            scaleScrollY = 0;
//            scaleScrollX = (int) ((e.getX() - getWidth() / 2)*mCurrentScale);
//            scaleScrollY = (int) ((e.getY() - getHeight() / 2)*mCurrentScale);
//
//            int startX = (int) ((e.getX() - getWidth() / 2)) * (int) (mMaxScale - originScale);
//            int startY = (int) ((e.getY() - getHeight() / 2)) * (int) (mMaxScale - originScale);
//            mScroller.startScroll(getScrollX(), getScrollY(), -startX, -startY);
//            invalidate();
//            scaleCenterX = e.getX();
//            scaleCenterY = e.getY();
            scaleBefore = mCurrentScale;
            getScaleAnimator().start();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.e("xbc", "onDoubleTapEvent");
        return false;
    }

    /************* OnDoubleTapListener End *************/

    @Override
    public void computeScroll() {
        super.computeScroll();
        Log.e("xbc", "computeScroll:" + mScroller.computeScrollOffset() + "," + mScroller.getCurrX() + "," + mScroller.getCurrY());
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}

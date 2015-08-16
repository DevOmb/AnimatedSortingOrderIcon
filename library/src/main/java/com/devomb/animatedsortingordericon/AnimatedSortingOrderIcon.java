package com.devomb.animatedsortingordericon;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Created by Ombrax on 9/08/2015.
 */
public class AnimatedSortingOrderIcon extends View {

    //region declaration
    //region constant
    private final int FPS = 20;
    //endregion

    //region variable
    private int width;
    private int height;

    private boolean ascendingOrder;
    private long animationLength;

    private int bars;
    private float barIncrement;
    private float barSpacing;
    private float barOffset;
    private float barCornerRadius;
    private int barColor;

    private OnAnimationChangeListener onAnimationChangeListener;
    //endregion

    //region inner field
    private float mBarWidth;
    private float mBarHeight;
    private float mBarSpacing;
    private float mBarIncrement;

    private boolean rounded;

    private Paint paint;

    private boolean isAnimating;
    private float currentInvalidationCount;
    private float totalInvalidationCount;
    private float[] barTops;
    private int animationBarThreshold;
    //endregion
    //endregion

    //region constructor
    public AnimatedSortingOrderIcon(Context context) {
        super(context);
        init(null, 0);
    }

    public AnimatedSortingOrderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AnimatedSortingOrderIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }
    //endregion

    //region setup
    private void init(AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);
        setAttributes(attrs, defStyleAttr);
        newPaint();
        calcAll();
    }
    //endregion


    //region getter
    public boolean isAscendingOrder() {
        return ascendingOrder;
    }
    //endregion

    //region setter
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        if (width > 0 && height > 0) {
            calcAll();
            measure(width, height);
        }
    }

    public void setAscendingOrder(boolean ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }

    public void setBars(int bars) {
        this.bars = bars;
        calcBarWidth();
        barTops = new float[bars];
        animationBarThreshold = bars / 2;
    }

    public void setBarIncrement(float barIncrement) {
        this.barIncrement = barIncrement;
        calcHeightIncrement();
    }

    public void setBarSpacing(float barSpacing) {
        this.barSpacing = barSpacing;
        calcBarWidth();
    }

    public void setBarOffset(float barOffset) {
        this.barOffset = barOffset;
        calcBarHeight();
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
        if (paint != null) {
            paint.setColor(barColor);
        } else {
            newPaint();
        }
    }

    public void setBarCornerRadius(float barCornerRadius) {
        this.barCornerRadius = barCornerRadius;
        rounded = barCornerRadius != 0;
    }

    public void setAnimationLength(long animationLength) {
        this.animationLength = animationLength;
        totalInvalidationCount = animationLength / FPS;
    }

    public void setOnAnimationChangeListener(OnAnimationChangeListener onAnimationChangeListener) {
        this.onAnimationChangeListener = onAnimationChangeListener;
    }
    //endregion

    //region method
    public void transform() {
        startAnimation();
    }
    //endregion

    //region helper
    private void waitForLayout() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                calcNewDimensions();
            }
        });
    }

    private void newPaint() {
        paint = new Paint();
        paint.setColor(barColor);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void setAttributes(AttributeSet set, int defStyleAttr) {
        //System attributes
        int[] systemSizeAttrs = new int[]{android.R.attr.layout_width, android.R.attr.layout_height};
        TypedArray systemAttributes = getContext().obtainStyledAttributes(set, systemSizeAttrs, defStyleAttr, 0);
        setDimensions(systemAttributes.getLayoutDimension(0, -1), systemAttributes.getLayoutDimension(1, -1));
        systemAttributes.recycle();

        //Custom attributes
        TypedArray customAttributes = getContext().obtainStyledAttributes(set, R.styleable.AnimatedSortingOrderIcon);
        setBars(customAttributes.getInt(R.styleable.AnimatedSortingOrderIcon_bars, 4));
        setBarIncrement(customAttributes.getFloat(R.styleable.AnimatedSortingOrderIcon_barIncrement, 0.20f));
        setBarSpacing(customAttributes.getFloat(R.styleable.AnimatedSortingOrderIcon_barSpacing, 0.66f));
        setBarOffset(customAttributes.getFloat(R.styleable.AnimatedSortingOrderIcon_barOffset, 0f));
        setBarColor(customAttributes.getColor(R.styleable.AnimatedSortingOrderIcon_barColor, Color.WHITE));
        setBarCornerRadius(customAttributes.getFloat(R.styleable.AnimatedSortingOrderIcon_barCornerRadius, 0f));
        setAnimationLength(customAttributes.getInt(R.styleable.AnimatedSortingOrderIcon_animationLength, 500));
        setAscendingOrder(customAttributes.getInt(R.styleable.AnimatedSortingOrderIcon_order, 0) == 0);
        customAttributes.recycle();
    }
    //endregion

    //region animation
    long animEndTime;

    Handler handler = new Handler();
    Runnable looper = new Runnable() {
        public void run() {
            if (currentInvalidationCount == totalInvalidationCount) {
                stopAnimation();
            } else {
                currentInvalidationCount++;
                invalidate();
                handler.postDelayed(this, FPS);
                if (onAnimationChangeListener != null) {
                    onAnimationChangeListener.onAnimationChange(currentInvalidationCount / totalInvalidationCount);
                }
            }
        }
    };

    void startAnimation() {
        animEndTime = SystemClock.uptimeMillis() + animationLength;
        if (onAnimationChangeListener != null) {
            onAnimationChangeListener.onAnimationStart();
        }
        currentInvalidationCount = 0;
        isAnimating = true;
        handler.removeCallbacks(looper);
        handler.post(looper);
    }

    void stopAnimation() {
        handler.removeCallbacks(looper);
        ascendingOrder = !ascendingOrder;
        isAnimating = false;
        if (onAnimationChangeListener != null) {
            onAnimationChangeListener.onAnimationStop(ascendingOrder);
        }
    }
    //endregion

    //region calc
    private void calcNewDimensions() {
        boolean invalidHeight = height < 0;
        boolean invalidWidth = width < 0;
        int mWidth = getWidth();
        int mHeight = getHeight();
        if (invalidWidth || invalidHeight) {
            if (mHeight > mWidth) {
                //PORTRAIT
                if (invalidWidth) {
                    //WRAP or MATCH
                    width = mWidth;
                }
                if (invalidHeight) {
                    if (height == -2) {
                        //WRAP
                        height = invalidWidth ? mWidth : width;
                    } else {
                        //MATCH or Default
                        height = mHeight;
                    }
                }
            } else {
                //LANDSCAPE
                if (invalidHeight) {
                    //WRAP or MATCH
                    height = getHeight();
                }
                if (invalidWidth) {
                    if (width == -2) {
                        //WRAP
                        width = invalidHeight ? mHeight : height;
                    } else {
                        //MATCH or Default
                        width = mWidth;
                    }
                }
            }
        }
        setDimensions(width, height);
    }

    private float calcNewTopForBar(int barIndex) {
        float top = barTops[barIndex];
        float hInc = Math.abs(barTops[bars - (barIndex + 1)] - top) / totalInvalidationCount * currentInvalidationCount;
        float mTop = top + (barIndex < animationBarThreshold ? hInc : -hInc);
        return mTop;
    }

    private void calcBarWidth() {
        mBarWidth = width / (bars + (bars * barSpacing) - barSpacing);
        mBarSpacing = mBarWidth * barSpacing;
    }

    private void calcBarHeight() {
        mBarHeight = height - (height * barOffset);
    }

    private void calcHeightIncrement() {
        mBarIncrement = mBarHeight * barIncrement;
    }

    private void calcAll() {
        calcBarWidth();
        calcBarHeight();
        calcHeightIncrement();
    }
    //endregion

    //region measure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (width < 0 || height < 0) {
            waitForLayout();
        } else {
            setMeasuredDimension(width, height);
        }
    }
    //endregion

    //region draw
    @Override
    protected void onDraw(Canvas canvas) {
        if (ascendingOrder) {
            canvas.scale(-1f, 1f, width / 2, height / 2);
        }
        super.onDraw(canvas);
        RectF bar = new RectF(0, height - mBarHeight, mBarWidth, height);
        for (int i = 0; i < bars; i++) {
            drawBar(canvas, i, bar, rounded);
        }
    }

    private void drawBar(Canvas canvas, int barIndex, RectF bar, boolean rounded) {
        if (isAnimating) {
            if (barIndex != 0) {
                bar.left = bar.right + mBarSpacing;
                bar.right = bar.left + mBarWidth;
            }
            bar.top = calcNewTopForBar(barIndex);
        } else {
            if (barIndex != 0) {
                bar.left = bar.right + mBarSpacing;
                bar.right = bar.left + mBarWidth;
                bar.top = bar.top + mBarIncrement;
            }
            barTops[barIndex] = bar.top;
        }

        if (rounded) {
            canvas.drawRoundRect(bar, barCornerRadius, barCornerRadius, paint);
        } else {
            canvas.drawRect(bar, paint);
        }
    }
//endregion

    //region listener
    public interface OnAnimationChangeListener {
        void onAnimationStart();

        void onAnimationStop(boolean isAscendingOrder);

        void onAnimationChange(float progress);
    }

    public static class DefaultOnAnimationChangeListener implements OnAnimationChangeListener {
        @Override
        public void onAnimationStart() {
            //Do Nothing
        }

        @Override
        public void onAnimationStop(boolean isAscendingOrder) {
            //Do Nothing
        }

        @Override
        public void onAnimationChange(float progress) {
            //Do Nothing
        }
    }
//endregion
}

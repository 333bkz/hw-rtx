package com.bkz.control;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//悬浮布局
public class FloatDragLayout extends FrameLayout {
    private static final int OFFSET_ALLOW_DISTANCE = 10;
    private boolean isNearScreenEdge = false;// 是否自动贴边
    private boolean isDrag = true;// 是否可拖拽
    private boolean isMoving;// 正在移动
    private RectF paddingRect = new RectF(5, 5, 5, 5);// 距离四周的边距
    private final PointF startPosition = new PointF();
    private final PointF lastTouchPoint = new PointF();

    public FloatDragLayout(@NonNull Context context) {
        super(context);
    }

    public FloatDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                startPosition.x = getX() - event.getRawX();
                startPosition.y = getY() - event.getRawY();
                // save last touch point
                lastTouchPoint.x = event.getRawX();
                lastTouchPoint.y = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDrag) {
                    float distanceX = event.getRawX() - lastTouchPoint.x;
                    float distanceY = event.getRawY() - lastTouchPoint.y;
                    if (Math.sqrt(distanceX * distanceX + distanceY * distanceY) > OFFSET_ALLOW_DISTANCE) {
                        isMoving = true;
                        setX(event.getRawX() + startPosition.x);
                        setY(event.getRawY() + startPosition.y);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMoving) {
                    setPressed(false);
                    // save last touch point
                    lastTouchPoint.x = event.getRawX();
                    lastTouchPoint.y = event.getRawY();

                    if (isNearScreenEdge) {
                        animatorMove(getNearPoint(), 300);
                    } else {
                        animatorMove(fixedValue(new PointF(getX(), getY())), 100);
                    }
                    isMoving = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isMoving) {
                    isMoving = false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    // 计算贴边位置
    private PointF getNearPoint() {
        View parent = (View) getParent();
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();

        float rightDistance = parentWidth - getX();// 距离右侧距离
        float bottomDistance = parentHeight - getY();// 距离底部距离

        float xMinDistance = Math.min(getX(), rightDistance);
        float yMinDistance = Math.min(getY(), bottomDistance);

        float xValue = 0;
        float yValue = 0;
        if (xMinDistance <= yMinDistance) {// 向X边靠拢
            yValue = getY();
            if (getX() > parentWidth / 2f) {// 向X右边靠拢
                xValue = parentWidth - getWidth();
            }
        } else {// 向Y边靠拢
            xValue = getX();
            if (getY() > parentHeight / 2f) {// 向Y底边靠拢
                yValue = parentHeight - getHeight();
            }
        }
        // 修正值
        return fixedValue(new PointF(xValue, yValue));
    }


    // 修正值
    private PointF fixedValue(PointF point) {
        View parent = (View) getParent();
        return fixedValue(point,
                0 + paddingRect.left, parent.getWidth() - getWidth() - paddingRect.right,
                0 + paddingRect.top, parent.getHeight() - getHeight() - paddingRect.bottom
        );
    }

    // 修正值
    private PointF fixedValue(PointF point, float minX, float maxX, float minY, float maxY) {
        // xValue -> [ minX , maxX ]
        point.x = Math.max(point.x, minX);
        point.x = Math.min(point.x, maxX);
        // yValue -> [ minY , maxY ]
        point.y = Math.max(point.y, minY);
        point.y = Math.min(point.y, maxY);
        return point;
    }

    /**
     * 从当前位置A 动画移动到 某个位置B
     *
     * @param targetPoint 目标位置
     * @param duration    动画时间
     */
    private void animatorMove(PointF targetPoint, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(this, "x", getX(), targetPoint.x),
                ObjectAnimator.ofFloat(this, "y", getY(), targetPoint.y)
        );
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                updateLayoutParams();
            }
        });
        animatorSet.setDuration(duration);
        animatorSet.start();
    }

    private void updateLayoutParams() {
        final int type = getResources().getConfiguration().orientation;
        int topPadding = 0;
        if (type == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || type == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            topPadding = ExtsKt.getStatusBarHeight(getContext());
        }
        LayoutParams layoutParams = new LayoutParams(getWidth(), getHeight());
        layoutParams.setMargins(getLeft(), getTop() - topPadding, getRight(), getBottom());
        setLayoutParams(layoutParams);
    }

    //------------------------------ public method ------------------------------

    // 是否自动贴边
    public FloatDragLayout setNearScreenEdge(boolean nearScreenEdge) {
        this.isNearScreenEdge = nearScreenEdge;
        if (isNearScreenEdge) {
            animatorMove(getNearPoint(), 300);
        }
        return this;
    }

    // 是否可拖拽
    public FloatDragLayout setDrag(boolean isDrag) {
        this.isDrag = isDrag;
        return this;
    }

    // 设置四周边距
    public FloatDragLayout setPaddingRect(RectF rect) {
        this.paddingRect = rect;
        updateLocation(getX(), getY());
        return this;
    }

    // 更新位置
    public void updateLocation(float x, float y) {
        updateLocation(new PointF(x, y));
    }

    // 更新位置
    public void updateLocation(PointF point) {
        point = fixedValue(point);
        this.setX(point.x);
        this.setY(point.y);
    }

    // 更新位置
    public void updateLocationForMove(PointF point, long duration) {
        point = fixedValue(point);
        animatorMove(point,duration);
    }
}
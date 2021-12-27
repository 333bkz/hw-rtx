package com.bkz.control;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface VideoGestureListener {

    int GESTURE_DEFAULT = -1;
    int GESTURE_HORIZONTAL = 1;//水平
    int GESTURE_VERTICAL_LEFT = 2;//垂直 + 左边：
    int GESTURE_VERTICAL_RIGHT = 3;//垂直 +右边：

    @IntDef({GESTURE_DEFAULT, GESTURE_HORIZONTAL, GESTURE_VERTICAL_LEFT, GESTURE_VERTICAL_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    @interface GestureType {
    }

    /**
     * 双击
     *
     * @return 拦截
     */
    boolean onDoubleTap();

    /**
     * 手势滑动数据返回
     *
     * @param gestureType
     * @param progress
     * @param move
     */
    void videoGestureCall(@GestureType int gestureType, int progress, int move);

    /**
     * 是否已准备好
     */
    boolean isPrepare();

    /**
     * 当前播放进度 直播设置<0不回调
     */
    int getCurrentProgress();

}

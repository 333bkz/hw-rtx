package com.bkz.control;

import static com.bkz.control.VideoGestureListener.GESTURE_DEFAULT;
import static com.bkz.control.VideoGestureListener.GESTURE_HORIZONTAL;
import static com.bkz.control.VideoGestureListener.GESTURE_VERTICAL_LEFT;
import static com.bkz.control.VideoGestureListener.GESTURE_VERTICAL_RIGHT;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class VideoGesture {
    private final GestureDetector mGestureDetector;
    private final VideoGestureListener mListener;
    private final AudioManager mAudioManager;
    private final Window mWindow;
    private final WindowManager.LayoutParams mLayoutParams;
    private final int mMaxSound;//系统最大音量
    private final int mScreenWidth;
    private final int mScreenHeight;
    //手势方向
    @VideoGestureListener.GestureType
    private int mGestureType = GESTURE_DEFAULT;
    private int mSystemSound = -1;//记录修改前的音量 可在界面关闭时恢复原来的音量 resetSystemSound()
    private int mTotal = 0; //video总进度
    private int mProgress = 0; //进度
    private float mCurLight; //亮度
    private int mDistanceY = 0;//最大音量只有15，所以记录滑动距离，当该值超过mScreenHeight / 1.5f / mMaxSound 音量增加｜减少1 该值清零

    public VideoGesture(Activity activity, VideoGestureListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(activity, new GestureListener());
        mScreenWidth = ExtsKt.getScreenWidth(activity);
        mScreenHeight = ExtsKt.getScreenHeight(activity);
        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxSound = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mWindow = activity.getWindow();
        mLayoutParams = mWindow.getAttributes();
        try {
            int screenBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            mCurLight = screenBrightness / 255f;
        } catch (Settings.SettingNotFoundException e) {
            mCurLight = 0.3f;
        }
    }

    public GestureDetector getGestureDetector() {
        return mGestureDetector;
    }

    public void setTotal(int totalTime) {
        mTotal = totalTime;
    }

    public int getProgress() {
        return mProgress;
    }

    public void resetSystemSound() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSystemSound, 0);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private float downX;
        private boolean isFirst = false;//onScroll第一次回调的值有问题 不处理

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return mListener.onDoubleTap();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mGestureType = GESTURE_DEFAULT;
            downX = e.getX();
            isFirst = true;
            mDistanceY = 0;
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mListener.isPrepare() && !isFirst) {
                if (mGestureType == GESTURE_DEFAULT) {//第一滑动是状态后面不再改动
                    if (Math.abs(distanceX) >= Math.abs(distanceY)) {// 横向的距离变化大则调整进度，纵向的变化大则调整音量
                        mGestureType = GESTURE_HORIZONTAL;
                    } else {
                        if (downX > mScreenWidth / 2.0f) {//右边处理音量
                            mGestureType = GESTURE_VERTICAL_RIGHT;
                        } else {
                            mGestureType = GESTURE_VERTICAL_LEFT;
                        }
                    }
                }

                switch (mGestureType) {
                    case GESTURE_HORIZONTAL:
                        final int currentProgress = mListener.getCurrentProgress();
                        if (currentProgress >= 0) {
                            horizontalScroll(currentProgress, e1.getX(), e2.getX(), distanceX);
                        }
                        break;
                    case GESTURE_VERTICAL_LEFT:
                        setLight(distanceY);
                        break;
                    case GESTURE_VERTICAL_RIGHT:
                        setVolume(distanceY);
                        break;
                    //noinspection ConstantConditions
                    case GESTURE_DEFAULT:
                        break;
                }
            }
            isFirst = false;
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 进度
     */
    private void horizontalScroll(int currentProgress, float lastX, float x, float distanceX) {
        if (x == lastX || Math.abs(distanceX) < 1.5) {
            return;
        }
        final float percent = (x - lastX) / mScreenWidth;
        int move = (int) (percent * mTotal) / 3;
        if (move + currentProgress >= mTotal) {
            mProgress = mTotal;
        } else {
            mProgress = Math.max(move + currentProgress, 0);
        }
        mListener.videoGestureCall(mGestureType, mProgress, move);
    }


    /**
     * 设置系统音量
     */
    private void setVolume(float distanceY) {
        if (mSystemSound < 0) {
            mSystemSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mDistanceY += distanceY;
        float percent = mScreenHeight / 1.5f / mMaxSound;
        if (Math.abs(mDistanceY) < percent) {
            return;
        }
        mDistanceY = 0;
        if (distanceY > 0) {
            current++;//最大音量只有15
        } else {
            current--;
        }
        if (current > mMaxSound) {
            current = mMaxSound;
        } else if (current < 0) {
            current = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, 0);
        float sound = current / 1.0f / mMaxSound * 100;
        mProgress = (int) sound;
        mListener.videoGestureCall(mGestureType, mProgress, 0);
    }

    /**
     * 设置界面亮度
     */
    private void setLight(float distanceY) {
        if (distanceY > 20) distanceY = 20;
        mLayoutParams.screenBrightness = mCurLight + distanceY / mScreenHeight * 1.5f;
        if (mLayoutParams.screenBrightness > 1.0f)
            mLayoutParams.screenBrightness = 1.0f;
        else if (mLayoutParams.screenBrightness < 0.01f)
            mLayoutParams.screenBrightness = 0.01f;
        mCurLight = mLayoutParams.screenBrightness;
        mProgress = (int) (mLayoutParams.screenBrightness * 100);
        mWindow.setAttributes(mLayoutParams);
        mListener.videoGestureCall(mGestureType, mProgress, 0);
    }
}
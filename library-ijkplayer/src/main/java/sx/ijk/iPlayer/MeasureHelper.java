package sx.ijk.iPlayer;

import static sx.ijk.iPlayer.IRenderView.AR_16_9_FIT_PARENT;
import static sx.ijk.iPlayer.IRenderView.AR_4_3_FIT_PARENT;
import static sx.ijk.iPlayer.IRenderView.AR_ASPECT_FILL_PARENT;
import static sx.ijk.iPlayer.IRenderView.AR_ASPECT_FIT_PARENT;
import static sx.ijk.iPlayer.IRenderView.AR_ASPECT_WRAP_CONTENT;
import static sx.ijk.iPlayer.IRenderView.AR_MATCH_PARENT;

import android.view.View;
import android.view.View.MeasureSpec;

import java.lang.ref.WeakReference;

public final class MeasureHelper {
    private final WeakReference<View> mWeakView;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private int mVideoRotationDegree;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int mCurrentAspectRatio = 0;

    public MeasureHelper(View view) {
        this.mWeakView = new WeakReference<>(view);
    }

    public View getView() {
        return this.mWeakView.get();
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        this.mVideoSarNum = videoSarNum;
        this.mVideoSarDen = videoSarDen;
    }

    public void setVideoRotation(int videoRotationDegree) {
        this.mVideoRotationDegree = videoRotationDegree;
    }

    public void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        if (this.mVideoRotationDegree == 90 || this.mVideoRotationDegree == 270) {
            width = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = width;
        }

        width = View.getDefaultSize(this.mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(this.mVideoHeight, heightMeasureSpec);
        if (this.mCurrentAspectRatio == 3) {
            width = widthMeasureSpec;
            height = heightMeasureSpec;
        } else if (this.mVideoWidth > 0 && this.mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthSpecMode == -2147483648 && heightSpecMode == -2147483648) {
                float specAspectRatio = (float) widthSpecSize / (float) heightSpecSize;
                float displayAspectRatio;
                switch (this.mCurrentAspectRatio) {
                    case AR_ASPECT_FIT_PARENT:
                    case AR_ASPECT_FILL_PARENT:
                    case AR_ASPECT_WRAP_CONTENT:
                    case AR_MATCH_PARENT:
                    default:
                        displayAspectRatio = (float) this.mVideoWidth / (float) this.mVideoHeight;
                        if (this.mVideoSarNum > 0 && this.mVideoSarDen > 0) {
                            displayAspectRatio = displayAspectRatio * (float) this.mVideoSarNum / (float) this.mVideoSarDen;
                        }
                        break;
                    case AR_16_9_FIT_PARENT:
                        displayAspectRatio = 1.7777778F;
                        if (this.mVideoRotationDegree == 90 || this.mVideoRotationDegree == 270) {
                            displayAspectRatio = 1.0F / displayAspectRatio;
                        }
                        break;
                    case AR_4_3_FIT_PARENT:
                        displayAspectRatio = 1.3333334F;
                        if (this.mVideoRotationDegree == 90 || this.mVideoRotationDegree == 270) {
                            displayAspectRatio = 1.0F / displayAspectRatio;
                        }
                }

                boolean shouldBeWider = displayAspectRatio > specAspectRatio;
                switch (this.mCurrentAspectRatio) {
                    case AR_ASPECT_FIT_PARENT:
                    case AR_16_9_FIT_PARENT:
                    case AR_4_3_FIT_PARENT:
                        if (shouldBeWider) {
                            width = widthSpecSize;
                            height = (int) ((float) widthSpecSize / displayAspectRatio);
                        } else {
                            height = heightSpecSize;
                            width = (int) ((float) heightSpecSize * displayAspectRatio);
                        }
                        break;
                    case AR_ASPECT_FILL_PARENT:
                        if (shouldBeWider) {
                            height = heightSpecSize;
                            width = (int) ((float) heightSpecSize * displayAspectRatio);
                        } else {
                            width = widthSpecSize;
                            height = (int) ((float) widthSpecSize / displayAspectRatio);
                        }
                        break;
                    case AR_ASPECT_WRAP_CONTENT:
                    case AR_MATCH_PARENT:
                    default:
                        if (shouldBeWider) {
                            width = Math.min(this.mVideoWidth, widthSpecSize);
                            height = (int) ((float) width / displayAspectRatio);
                        } else {
                            height = Math.min(this.mVideoHeight, heightSpecSize);
                            width = (int) ((float) height * displayAspectRatio);
                        }
                }
            } else if (widthSpecMode == 1073741824 && heightSpecMode == 1073741824) {
                width = widthSpecSize;
                height = heightSpecSize;
                if (this.mVideoWidth * heightSpecSize < widthSpecSize * this.mVideoHeight) {
                    width = heightSpecSize * this.mVideoWidth / this.mVideoHeight;
                } else if (this.mVideoWidth * heightSpecSize > widthSpecSize * this.mVideoHeight) {
                    height = widthSpecSize * this.mVideoHeight / this.mVideoWidth;
                }
            } else if (widthSpecMode == 1073741824) {
                width = widthSpecSize;
                height = widthSpecSize * this.mVideoHeight / this.mVideoWidth;
                if (heightSpecMode == -2147483648 && height > heightSpecSize) {
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == 1073741824) {
                height = heightSpecSize;
                width = heightSpecSize * this.mVideoWidth / this.mVideoHeight;
                if (widthSpecMode == -2147483648 && width > widthSpecSize) {
                    width = widthSpecSize;
                }
            } else {
                width = this.mVideoWidth;
                height = this.mVideoHeight;
                if (heightSpecMode == -2147483648 && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = heightSpecSize * this.mVideoWidth / this.mVideoHeight;
                }

                if (widthSpecMode == -2147483648 && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = widthSpecSize * this.mVideoHeight / this.mVideoWidth;
                }
            }
        }

        this.mMeasuredWidth = width;
        this.mMeasuredHeight = height;
    }

    public int getMeasuredWidth() {
        return this.mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return this.mMeasuredHeight;
    }

    public void setAspectRatio(int aspectRatio) {
        this.mCurrentAspectRatio = aspectRatio;
    }
}

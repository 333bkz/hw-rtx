package sx.ijk.iPlayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.ISurfaceTextureHolder;
import tv.danmaku.ijk.media.player.ISurfaceTextureHost;

public class TextureRenderView extends TextureView implements IRenderView {
    private MeasureHelper mMeasureHelper;
    private TextureRenderView.SurfaceCallback mSurfaceCallback;

    public TextureRenderView(Context context) {
        super(context);
        this.initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initView(context);
    }

    private void initView(Context context) {
        this.mMeasureHelper = new MeasureHelper(this);
        this.mSurfaceCallback = new TextureRenderView.SurfaceCallback(this);
        this.setSurfaceTextureListener(this.mSurfaceCallback);
    }

    public View getView() {
        return this;
    }

    public boolean shouldWaitForResize() {
        return false;
    }

    protected void onDetachedFromWindow() {
        this.mSurfaceCallback.willDetachFromWindow();
        super.onDetachedFromWindow();
        this.mSurfaceCallback.didDetachFromWindow();
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            this.mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            this.requestLayout();
        }

    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            this.mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            this.requestLayout();
        }

    }

    public void setVideoRotation(int degree) {
        this.mMeasureHelper.setVideoRotation(degree);
        this.setRotation((float) degree);
    }

    public void setAspectRatio(int aspectRatio) {
        this.mMeasureHelper.setAspectRatio(aspectRatio);
        this.requestLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        this.setMeasuredDimension(this.mMeasureHelper.getMeasuredWidth(), this.mMeasureHelper.getMeasuredHeight());
    }

    public ISurfaceHolder getSurfaceHolder() {
        return new TextureRenderView.InternalSurfaceHolder(this, this.mSurfaceCallback.mSurfaceTexture, this.mSurfaceCallback);
    }

    public void addRenderCallback(IRenderCallback callback) {
        this.mSurfaceCallback.addRenderCallback(callback);
    }

    public void removeRenderCallback(IRenderCallback callback) {
        this.mSurfaceCallback.removeRenderCallback(callback);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TextureRenderView.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextureRenderView.class.getName());
    }

    private static final class SurfaceCallback implements SurfaceTextureListener, ISurfaceTextureHost {
        private SurfaceTexture mSurfaceTexture;
        private boolean mIsFormatChanged;
        private int mWidth;
        private int mHeight;
        private boolean mOwnSurfaceTexture = true;
        private boolean mWillDetachFromWindow = false;
        private boolean mDidDetachFromWindow = false;
        private WeakReference<TextureRenderView> mWeakRenderView;
        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap();

        public SurfaceCallback(@NonNull TextureRenderView renderView) {
            this.mWeakRenderView = new WeakReference(renderView);
        }

        public void setOwnSurfaceTexture(boolean ownSurfaceTexture) {
            this.mOwnSurfaceTexture = ownSurfaceTexture;
        }

        public void addRenderCallback(@NonNull IRenderCallback callback) {
            this.mRenderCallbackMap.put(callback, callback);
            ISurfaceHolder surfaceHolder = null;
            if (this.mSurfaceTexture != null) {
                if (surfaceHolder == null) {
                    surfaceHolder = new TextureRenderView.InternalSurfaceHolder((TextureRenderView) this.mWeakRenderView.get(), this.mSurfaceTexture, this);
                }

                callback.onSurfaceCreated(surfaceHolder, this.mWidth, this.mHeight);
            }

            if (this.mIsFormatChanged) {
                if (surfaceHolder == null) {
                    surfaceHolder = new TextureRenderView.InternalSurfaceHolder((TextureRenderView) this.mWeakRenderView.get(), this.mSurfaceTexture, this);
                }

                callback.onSurfaceChanged(surfaceHolder, 0, this.mWidth, this.mHeight);
            }

        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            this.mRenderCallbackMap.remove(callback);
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            this.mSurfaceTexture = surface;
            this.mIsFormatChanged = false;
            this.mWidth = 0;
            this.mHeight = 0;
            ISurfaceHolder surfaceHolder = new TextureRenderView.InternalSurfaceHolder((TextureRenderView) this.mWeakRenderView.get(), surface, this);
            Iterator var5 = this.mRenderCallbackMap.keySet().iterator();

            while (var5.hasNext()) {
                IRenderCallback renderCallback = (IRenderCallback) var5.next();
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }

        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            this.mSurfaceTexture = surface;
            this.mIsFormatChanged = true;
            this.mWidth = width;
            this.mHeight = height;
            ISurfaceHolder surfaceHolder = new TextureRenderView.InternalSurfaceHolder((TextureRenderView) this.mWeakRenderView.get(), surface, this);
            Iterator var5 = this.mRenderCallbackMap.keySet().iterator();

            while (var5.hasNext()) {
                IRenderCallback renderCallback = (IRenderCallback) var5.next();
                renderCallback.onSurfaceChanged(surfaceHolder, 0, width, height);
            }

        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            this.mSurfaceTexture = surface;
            this.mIsFormatChanged = false;
            this.mWidth = 0;
            this.mHeight = 0;
            ISurfaceHolder surfaceHolder = new TextureRenderView.InternalSurfaceHolder((TextureRenderView) this.mWeakRenderView.get(), surface, this);
            Iterator var3 = this.mRenderCallbackMap.keySet().iterator();

            while (var3.hasNext()) {
                IRenderCallback renderCallback = (IRenderCallback) var3.next();
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }

            Log.d("TextureRenderView", "onSurfaceTextureDestroyed: destroy: " + this.mOwnSurfaceTexture);
            return this.mOwnSurfaceTexture;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void releaseSurfaceTexture(SurfaceTexture surfaceTexture) {
            if (surfaceTexture == null) {
                Log.d("TextureRenderView", "releaseSurfaceTexture: null");
            } else if (this.mDidDetachFromWindow) {
                if (surfaceTexture != this.mSurfaceTexture) {
                    Log.d("TextureRenderView", "releaseSurfaceTexture: didDetachFromWindow(): release different SurfaceTexture");
                    surfaceTexture.release();
                } else if (!this.mOwnSurfaceTexture) {
                    Log.d("TextureRenderView", "releaseSurfaceTexture: didDetachFromWindow(): release detached SurfaceTexture");
                    surfaceTexture.release();
                } else {
                    Log.d("TextureRenderView", "releaseSurfaceTexture: didDetachFromWindow(): already released by TextureView");
                }
            } else if (this.mWillDetachFromWindow) {
                if (surfaceTexture != this.mSurfaceTexture) {
                    Log.d("TextureRenderView", "releaseSurfaceTexture: willDetachFromWindow(): release different SurfaceTexture");
                    surfaceTexture.release();
                } else if (!this.mOwnSurfaceTexture) {
                    Log.d("TextureRenderView", "releaseSurfaceTexture: willDetachFromWindow(): re-attach SurfaceTexture to TextureView");
                    this.setOwnSurfaceTexture(true);
                } else {
                    Log.d("TextureRenderView", "releaseSurfaceTexture: willDetachFromWindow(): will released by TextureView");
                }
            } else if (surfaceTexture != this.mSurfaceTexture) {
                Log.d("TextureRenderView", "releaseSurfaceTexture: alive: release different SurfaceTexture");
                surfaceTexture.release();
            } else if (!this.mOwnSurfaceTexture) {
                Log.d("TextureRenderView", "releaseSurfaceTexture: alive: re-attach SurfaceTexture to TextureView");
                this.setOwnSurfaceTexture(true);
            } else {
                Log.d("TextureRenderView", "releaseSurfaceTexture: alive: will released by TextureView");
            }

        }

        public void willDetachFromWindow() {
            Log.d("TextureRenderView", "willDetachFromWindow()");
            this.mWillDetachFromWindow = true;
        }

        public void didDetachFromWindow() {
            Log.d("TextureRenderView", "didDetachFromWindow()");
            this.mDidDetachFromWindow = true;
        }
    }

    private static final class InternalSurfaceHolder implements ISurfaceHolder {
        private TextureRenderView mTextureView;
        private SurfaceTexture mSurfaceTexture;
        private ISurfaceTextureHost mSurfaceTextureHost;

        public InternalSurfaceHolder(@NonNull TextureRenderView textureView, @Nullable SurfaceTexture surfaceTexture, @NonNull ISurfaceTextureHost surfaceTextureHost) {
            this.mTextureView = textureView;
            this.mSurfaceTexture = surfaceTexture;
            this.mSurfaceTextureHost = surfaceTextureHost;
        }

        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp != null) {
                if (mp instanceof ISurfaceTextureHolder) {
                    ISurfaceTextureHolder textureHolder = (ISurfaceTextureHolder) mp;
                    this.mTextureView.mSurfaceCallback.setOwnSurfaceTexture(false);
                    SurfaceTexture surfaceTexture = textureHolder.getSurfaceTexture();
                    if (surfaceTexture != null) {
                        this.mTextureView.setSurfaceTexture(surfaceTexture);
                    } else {
                        textureHolder.setSurfaceTexture(this.mSurfaceTexture);
                        textureHolder.setSurfaceTextureHost(this.mTextureView.mSurfaceCallback);
                    }
                } else {
                    mp.setSurface(this.openSurface());
                }

            }
        }

        @NonNull
        public IRenderView getRenderView() {
            return this.mTextureView;
        }

        @Nullable
        public SurfaceHolder getSurfaceHolder() {
            return null;
        }

        @Nullable
        public SurfaceTexture getSurfaceTexture() {
            return this.mSurfaceTexture;
        }

        @Nullable
        public Surface openSurface() {
            return this.mSurfaceTexture == null ? null : new Surface(this.mSurfaceTexture);
        }
    }
}
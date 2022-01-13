package sx.ijk.iPlayer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

import androidx.annotation.NonNull;

import sx.ijk.iPlayer.IRenderView.IRenderCallback;
import sx.ijk.iPlayer.IRenderView.ISurfaceHolder;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MediaPlayer {
    private static final String TAG = "MediaPlayer";
    private IjkMediaPlayer mPlayer;
    private MediaPlayer.PLAYER_STATE mState;
    private MediaPlayerObserver mObserver;
    private boolean mEnableMediaCodec;
    private final MediaPlayer.AVParameters mParams;
    private long mPrepareStartTime = 0L;
    private long mPrepareEndTime = 0L;
    private IRenderView mRenderView;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private ISurfaceHolder mSurfaceHolder = null;
    private boolean isSeeking = false;

    IRenderCallback mSHCallback = new IRenderCallback() {
        public void onSurfaceChanged(@NonNull ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != MediaPlayer.this.mRenderView) {
                Log.e(TAG, "onSurfaceChanged: unmatched render callback\n");
            } else {
                Log.d(TAG, "surface changed, w=" + w + ",h=" + h);
                MediaPlayer.this.mSurfaceWidth = w;
                MediaPlayer.this.mSurfaceHeight = h;
                View renderUIView = MediaPlayer.this.mRenderView.getView();
                LayoutParams lp = new LayoutParams(-2, -2, 17);
                renderUIView.setLayoutParams(lp);
            }
        }

        public void onSurfaceCreated(@NonNull ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != MediaPlayer.this.mRenderView) {
                Log.e(TAG, "onSurfaceCreated: unmatched render callback\n");
            } else {
                MediaPlayer.this.mSurfaceHolder = holder;
                if (MediaPlayer.this.mPlayer != null) {
                    holder.bindToMediaPlayer(MediaPlayer.this.mPlayer);
                }
            }
        }

        public void onSurfaceDestroyed(@NonNull ISurfaceHolder holder) {
            if (holder.getRenderView() != MediaPlayer.this.mRenderView) {
                Log.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
            } else {
                MediaPlayer.this.mSurfaceHolder = null;
                if (MediaPlayer.this.mPlayer != null) {
                    MediaPlayer.this.mPlayer.setDisplay((SurfaceHolder) null);
                }

            }
        }
    };

    public MediaPlayer(MediaPlayer.AVParameters params) {
        this.mState = MediaPlayer.PLAYER_STATE.INIT;
        this.mParams = params;
    }

    public void setObserver(MediaPlayerObserver observer) {
        this.mObserver = observer;
    }

    public MediaPlayer.PLAYER_STATE getState() {
        return this.mState;
    }

    public boolean init(Context ctx) {
        if (this.mParams.url.isEmpty()) {
            Log.e(TAG, "av url not set, cannot init bjy media player");
            return false;
        } else {
            this.mPrepareStartTime = System.currentTimeMillis();
            try {
                this.mState = MediaPlayer.PLAYER_STATE.CREATING;
                this.mPlayer = this.createPlayer(this.mParams.buffer_tcp_default);
                this.mPlayer.setOnPreparedListener(mp -> {
                    MediaPlayer.this.mPrepareEndTime = System.currentTimeMillis();
                    Log.i(TAG, "onPrepared, elapse time:" + (MediaPlayer.this.mPrepareEndTime - MediaPlayer.this.mPrepareStartTime));
                    MediaPlayer.this.mPlayer.start();
                    if (MediaPlayer.this.mObserver != null) {
                        MediaPlayer.this.mObserver.onPrepared();
                    }
                    MediaPlayer.this.mState = PLAYER_STATE.PREPARED;
                });
                this.mPlayer.setOnVideoSizeChangedListener((mp, width, height, sarNum, sarDen) -> {
                    Log.d(TAG, "video size changed, w=" + width + " h=" + height + " n=" + sarNum + " d=" + sarDen);
                    if ((height > 0 && MediaPlayer.this.mVideoHeight != height || width > 0 && MediaPlayer.this.mVideoWidth != width) && MediaPlayer.this.mObserver != null) {
                        MediaPlayer.this.mObserver.onVideoSizeChanged(width, height);
                    }

                    MediaPlayer.this.mVideoWidth = width;
                    MediaPlayer.this.mVideoHeight = height;
                    MediaPlayer.this.mVideoSarNum = mp.getVideoSarNum();
                    MediaPlayer.this.mVideoSarDen = mp.getVideoSarDen();
                    if (MediaPlayer.this.mVideoWidth != 0 && MediaPlayer.this.mVideoHeight != 0 && MediaPlayer.this.mRenderView != null) {
                        MediaPlayer.this.mRenderView.setVideoSize(MediaPlayer.this.mVideoWidth, MediaPlayer.this.mVideoHeight);
                        MediaPlayer.this.mRenderView.setVideoSampleAspectRatio(MediaPlayer.this.mVideoSarNum, MediaPlayer.this.mVideoSarDen);
                    }

                });
                this.mPlayer.setOnCompletionListener(mp -> {
                    Log.i(TAG, "onCompletion");
                    MediaPlayer.this.mObserver.onCompletion();
                });
                this.mPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "bjy media player got a error: [" + what + ", " + extra + "]");
                    if (MediaPlayer.this.mObserver != null) {
                        MediaPlayer.this.mObserver.onError(what, extra);
                    }

                    return true;
                });
                this.mPlayer.setOnInfoListener((mp, what, extra) -> {
                    Log.i(TAG, "onInfo, " + MediaPlayer.this.infoToString(what, extra));
                    return true;
                });
                this.mPlayer.setOnSeekCompleteListener(iMediaPlayer -> {
                    if (MediaPlayer.this.mObserver != null) {
                        MediaPlayer.this.mObserver.onSeekComplete();
                    }

                    MediaPlayer.this.isSeeking = false;
                });
                this.mPlayer.setOnBufferingUpdateListener((iMediaPlayer, i) -> {
                    if (MediaPlayer.this.mObserver != null) {
                        MediaPlayer.this.mObserver.onBufferingUpdate(i);
                    }

                });
                this.mPlayer.setDataSource(ctx, Uri.parse(this.mParams.url));
                this.mPlayer.prepareAsync();
                return true;
            } catch (Exception var3) {
                var3.printStackTrace();
                return false;
            }
        }
    }

    public void setMixedStreamDisplayMode(int mode) {
        if (this.mRenderView != null) {
            this.mRenderView.setAspectRatio(mode);
        }
    }

    public void setRenderView(IRenderView renderView) {
        if (this.mRenderView != null) {
            if (this.mPlayer != null) {
                this.mPlayer.setDisplay(null);
            }

            this.mRenderView.removeRenderCallback(this.mSHCallback);
            this.mRenderView = null;
        }

        if (renderView != null) {
            this.mRenderView = renderView;
            if (this.mVideoWidth > 0 && this.mVideoHeight > 0) {
                renderView.setVideoSize(this.mVideoWidth, this.mVideoHeight);
            }

            if (this.mVideoSarNum > 0 && this.mVideoSarDen > 0) {
                renderView.setVideoSampleAspectRatio(this.mVideoSarNum, this.mVideoSarDen);
            }

            View renderUIView = this.mRenderView.getView();
            LayoutParams lp = new LayoutParams(-2, -2, 17);
            renderUIView.setLayoutParams(lp);
            Log.d(TAG, "setRenderView mVideoWidth=" + this.mVideoWidth + " mVideoHeight=" + this.mVideoHeight + " mVideoSarNum" + this.mVideoSarNum + " mVideoSarDen" + this.mVideoSarDen);
            this.mRenderView.addRenderCallback(this.mSHCallback);
            this.mRenderView.setVideoRotation(this.mVideoRotationDegree);
        }
    }

    private String infoToString(int what, int extra) {
        String strInfo = "Unknown info [" + what + "," + extra + "]";
        switch (what) {
            case 3:
                strInfo = "MEDIA_INFO_VIDEO_RENDERING_START:" + extra;
                break;
            case 700:
                strInfo = "MEDIA_INFO_VIDEO_TRACK_LAGGING:" + extra;
                break;
            case 701:
                strInfo = "MEDIA_INFO_BUFFERING_START:" + extra;
                break;
            case 702:
                strInfo = "MEDIA_INFO_BUFFERING_END:" + extra;
                break;
            case 703:
                strInfo = "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra;
                break;
            case 800:
                strInfo = "MEDIA_INFO_BAD_INTERLEAVING:" + extra;
                break;
            case 801:
                strInfo = "MEDIA_INFO_NOT_SEEKABLE:" + extra;
                break;
            case 802:
                strInfo = "MEDIA_INFO_METADATA_UPDATE:" + extra;
                break;
            case 901:
                strInfo = "MEDIA_INFO_UNSUPPORTED_SUBTITLE:" + extra;
                break;
            case 902:
                strInfo = "MEDIA_INFO_SUBTITLE_TIMED_OUT:" + extra;
                break;
            case 10001:
                strInfo = "MEDIA_INFO_VIDEO_ROTATION_CHANGED:" + extra;
                break;
            case 10002:
                strInfo = "MEDIA_INFO_AUDIO_RENDERING_START:" + extra;
        }

        return strInfo;
    }

    public void setVolume(int left, int right) {
        Log.i(TAG, "setVolume: " + left + ", " + right);
        this.mPlayer.setVolume((float) left, (float) right);
    }

    public void enableMediaCodec(boolean isEnable) {
        this.mEnableMediaCodec = isEnable;
    }

    private void setEnableMediaCodec(IjkMediaPlayer ijkMediaPlayer, boolean isEnable) {
        int value = isEnable ? 1 : 0;
        ijkMediaPlayer.setOption(4, "mediacodec", (long) value);
        ijkMediaPlayer.setOption(4, "mediacodec-auto-rotate", (long) value);
        ijkMediaPlayer.setOption(4, "mediacodec-handle-resolution-change", (long) value);
    }

    private IjkMediaPlayer createPlayer(float tcpDelay) {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(4, "start-on-prepared", 0L);
        ijkMediaPlayer.setOption(4, "framedrop", 4L);
        ijkMediaPlayer.setOption(4, "packet-buffering", 0L);
        ijkMediaPlayer.setOption(4, "reconnect", 1L);
        ijkMediaPlayer.setOption(4, "inbuf", 1L);
        ijkMediaPlayer.setOption(4, "enable-accurate-seek", 1L);
        ijkMediaPlayer.setOption(1, "analyzemaxduration", 100L);
        ijkMediaPlayer.setOption(1, "analyzeduration", 1L);
        ijkMediaPlayer.setOption(1, "flush_packets", 1L);
        ijkMediaPlayer.setOption(1, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,rtmp,rtsp");
        ijkMediaPlayer.setOption(1, "safe", 0);
        ijkMediaPlayer.setOption(4, "soundtouch", 1);
        ijkMediaPlayer.setOption(2, "skip_loop_filter", 48);
        ijkMediaPlayer.setOption(1, "dns_cache_clear", 1);
        ijkMediaPlayer.setOption(1, "http-detect-range-support", 0);
        ijkMediaPlayer.setOption(1, "probesize", 10240L);
        //ijkMediaPlayer.setOption(1, "fflags", 1);
        //ijkMediaPlayer.setMaxBufferTime((int) tcpDelay * 1000);
        ijkMediaPlayer.setVolume(1.0F, 1.0F);
        IjkMediaPlayer.native_setLogLevel(8);
        this.setEnableMediaCodec(ijkMediaPlayer, this.mEnableMediaCodec);
        if (this.mParams != null) {
            if (!this.mParams.enableVideo) {
                ijkMediaPlayer.setOption(4, "vn", 1L);
            }
            if (!this.mParams.enableAudio) {
                ijkMediaPlayer.setOption(4, "an", 1L);
            }
        }
        return ijkMediaPlayer;
    }

    public int getVideoWidth() {
        return this.mPlayer != null ? this.mPlayer.getVideoWidth() : 0;
    }

    public int getVideoHeight() {
        return this.mPlayer != null ? this.mPlayer.getVideoHeight() : 0;
    }

    public void setSpeed(float ratio) {
        if (this.mPlayer != null) {
            this.mPlayer.setSpeed(ratio);
        }
    }

    public void seekTo(int msec) {
        if (this.mPlayer != null && !this.isSeeking) {
            this.mPlayer.seekTo((long) msec);
            this.isSeeking = true;
        }
    }

    public long getCurrentPosition() {
        return this.mPlayer != null ? this.mPlayer.getCurrentPosition() : 0L;
    }

    public void resume() {
        if (this.mPlayer != null) {
            this.mPlayer.start();
        }
    }

    public void pause() {
        if (this.mPlayer != null) {
            this.mPlayer.pause();
        }
    }

    public long getDuration() {
        return this.mPlayer != null ? this.mPlayer.getDuration() : 0L;
    }

    public float getVideoOutputFps() {
        if (this.mPlayer != null) {
            IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) this.mPlayer;
            return ijkMediaPlayer.getVideoOutputFramesPerSecond();
        } else {
            return 0.0F;
        }
    }

    public void dispose() {
        this.mState = MediaPlayer.PLAYER_STATE.STOPPING;
        if (this.mPlayer != null) {
            try {
                Log.i(TAG, "---=== Dispose bjy media player");
                this.mPlayer.reset();
                this.mPlayer.stop();
            } catch (IllegalStateException var2) {
                Log.e(TAG, "Dispose bjy media player state exception:" + var2.getMessage());
                var2.printStackTrace();
            }

            this.mPlayer.release();
            this.mPlayer = null;
            this.mState = MediaPlayer.PLAYER_STATE.DISPOSED;
        }
    }

    public static class AVParameters {
        public String url;
        public int sessType;
        public boolean enableAudio;
        public boolean enableVideo;
        public boolean enableMediaCodec;
        public boolean isAutoClose;
        public float buffer_tcp_default;

        public AVParameters(String url) {
            this.url = url;
            this.enableAudio = true;
            this.enableVideo = true;
            this.enableMediaCodec = false;
            this.sessType = 0;
            this.isAutoClose = false;
            this.buffer_tcp_default = 1024;
        }
    }

    public enum PLAYER_STATE {
        UINT,
        INIT,
        CREATING,
        PREPARED,
        COMPLETE,
        STOPPING,
        DISPOSED;
    }
}

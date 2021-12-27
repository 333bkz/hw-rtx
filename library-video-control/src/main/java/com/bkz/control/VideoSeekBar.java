package com.bkz.control;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class VideoSeekBar extends FrameLayout {

    private static final int HIDE_SEEK_BAR = 100;
    private final SeekBar seekBar;
    private final ImageView iv_icon;
    private final TextView tv_progress;
    private final TextView tv_current;
    private final TextView tv_duration;
    private final InnerHandler handler;

    public VideoSeekBar(Context context) {
        this(context, null);
    }

    public VideoSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.layout_video_seek_bar, this);
        seekBar = view.findViewById(R.id.seek_bar);
        iv_icon = view.findViewById(R.id.iv_icon);
        tv_progress = view.findViewById(R.id.tv_progress);
        tv_current = view.findViewById(R.id.tv_current);
        tv_duration = view.findViewById(R.id.tv_duration);
        seekBar.setEnabled(false);
        handler = new InnerHandler(this);
        setVisibility(View.GONE);
    }

    public VideoSeekBar setProgress(int progress) {
        this.seekBar.setMax(100);
        this.seekBar.setProgress(progress);
        this.tv_progress.setText(String.valueOf(progress));
        return this;
    }

    public void showVideoProgress(int progress, int total) {
        setVisibility(VISIBLE);
        this.seekBar.setMax(total);
        this.seekBar.setProgress(progress);
        this.tv_progress.setVisibility(GONE);
        this.iv_icon.setVisibility(GONE);
        this.tv_current.setVisibility(VISIBLE);
        this.tv_duration.setVisibility(VISIBLE);
        tv_current.setText(ExtsKt.toTimeSlot(progress));
        tv_duration.setText(ExtsKt.toTimeSlot(total));
        delayHide();
    }

    public void showVolume() {
        setVisibility(VISIBLE);
        this.tv_progress.setVisibility(VISIBLE);
        this.iv_icon.setVisibility(VISIBLE);
        this.tv_current.setVisibility(GONE);
        this.tv_duration.setVisibility(GONE);
        iv_icon.setImageResource(R.mipmap.icon_horn);
        delayHide();
    }

    public void showBrightness() {
        setVisibility(VISIBLE);
        this.tv_progress.setVisibility(VISIBLE);
        this.iv_icon.setVisibility(VISIBLE);
        this.tv_current.setVisibility(GONE);
        this.tv_duration.setVisibility(GONE);
        iv_icon.setImageResource(R.mipmap.icon_moon);
        delayHide();
    }

    private void delayHide() {
        handler.removeMessages(HIDE_SEEK_BAR);
        handler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 1000);
    }

    private static class InnerHandler extends Handler {

        private final WeakReference<VideoSeekBar> wr;

        public InnerHandler(VideoSeekBar view) {
            super(Looper.getMainLooper());
            wr = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == HIDE_SEEK_BAR) {
                VideoSeekBar view = wr.get();
                if (view != null) {
                    view.setVisibility(GONE);
                }
            }
        }
    }
}

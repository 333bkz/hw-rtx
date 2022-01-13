package sx.ijk.iPlayer;

public interface MediaPlayerObserver {
    void onPrepared();

    void onError(int var1, int var2);

    void onVideoSizeChanged(int var1, int var2);

    void onCompletion();

    void onSeekComplete();

    void onBufferingUpdate(int var1);
}
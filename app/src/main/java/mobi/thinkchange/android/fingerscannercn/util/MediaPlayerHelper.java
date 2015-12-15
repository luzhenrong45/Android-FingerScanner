package mobi.thinkchange.android.fingerscannercn.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by FIMH on 2015/1/15.
 */
public class MediaPlayerHelper implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private MediaPlayer mMediaPlayer;

    private Context mContext;

    private boolean mRepeat = true;

    private boolean mPrepared = false;

    private MediaPlayerHelper() {
    }

    public MediaPlayerHelper(Context context, int rawId) {
        mContext = context.getApplicationContext();

        initDataSource(rawId);
    }

    public void initDataSource(int rawId) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return;
        }

        mMediaPlayer = new MediaPlayer();
        mPrepared = false;

        Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + rawId);
        try {
            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVolume(float volume) {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolume(volume, volume);
    }

    public void start() {
        if (mPrepared)
            mMediaPlayer.start();
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPrepared = true;

        setVolume(0.02f);
    }

    public void setRepeat(boolean repeat) {
        mRepeat = repeat;
    }
}

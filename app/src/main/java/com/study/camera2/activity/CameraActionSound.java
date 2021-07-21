package com.study.camera2.activity;

import android.media.MediaActionSound;
import android.os.Handler;
import android.os.Looper;

/**
 * 播放器相机声音的类，例如捕获声音，录制的声音，主要实现是调用系统的MediaActionSound类
 */
public class CameraActionSound {
    //拍照捕获的点击
    public static final int SOUND_TYPE_CAPTURE = MediaActionSound.SHUTTER_CLICK;
    //视频开始的录制声音
    public static final int SOUND_TYPE_START_PANORAMA = MediaActionSound.START_VIDEO_RECORDING;
    //视频结束的声音；
    public static final int SOUND_TYPE_STOP_PANORAMA = MediaActionSound.STOP_VIDEO_RECORDING;
    //延迟500毫秒释放的声音；
    private static final long RELEASE_MEDIA_ACTION_SOUND_DELAY_MILLIS = 500;
    private MediaActionSound mMediaActionSound;
    //绑定主线程的looper;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mReleased;

    public CameraActionSound() {

        mMediaActionSound = new MediaActionSound();
        mReleased = false;
    }


    /**
     * Preload sound to speedup sound play action.
     *
     * @param soundType the sound resource.
     */
    public void load(int soundType) {
        synchronized (this) {
            mMediaActionSound.load(soundType);
        }
    }

    /**
     * Play sound.
     *
     * @param soundType the sound resource.
     */
    public void play(int soundType) {
        synchronized (this) {
            if (mReleased) {
                MediaActionSound mas = new MediaActionSound();
                mas.play(soundType);
                delayRelease(mas);
            } else {
                mMediaActionSound.play(soundType);
            }
        }
    }

    private void delayRelease(final MediaActionSound mas) {
        mReleased = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mas.release();
            }
        }, RELEASE_MEDIA_ACTION_SOUND_DELAY_MILLIS);
    }

}

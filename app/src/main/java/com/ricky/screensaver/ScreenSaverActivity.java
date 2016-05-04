package com.ricky.screensaver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ricky.screensaver.util.MyUtil;

import java.io.File;

public class ScreenSaverActivity extends Activity {

    private VideoView mVideoView;
    private ImageView mImageView;

    private static PowerManager.WakeLock mWakeLock;

    private Uri ssUri;
    private Bitmap bmp=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        mImageView = (ImageView) findViewById(R.id.imgView);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        initConfig();

    }

    //读取缓存的配置信息，并对初始化屏保界面
    private void initConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.SP_NAME, 0);
        String mUri = sharedPreferences.getString(MainActivity.SPKEY_URI, "");
        Log.e("uri from sp", mUri);
        ssUri = Uri.parse(mUri);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "MyScreenSaver");
        if (ssUri == null) {
            // ssUri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.screensaver_1);
            Log.e("SSActivity", "uri is empty");
            Toast.makeText(this,"您未设置任何屏保资源！",Toast.LENGTH_LONG).show();
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            mImageView.setBackgroundResource(R.drawable.splash);
        }
        //如果是图片
        if (MyUtil.isImageFile(mUri)) {
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            String mPath = new File(mUri).getAbsolutePath();
            if(bmp!=null){
                bmp.recycle();
            }
            bmp = BitmapFactory.decodeFile(mPath);
            mImageView.setImageBitmap(bmp);
        } else {
            mImageView.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setVideoURI(ssUri);
            mVideoView.setMediaController(null);
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 间隔两秒，重复播放屏保视频
                    Log.e("ScreenSaver", "视频播放完毕，两秒后重新播放");
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mVideoView.setVideoURI(ssUri);
                    mVideoView.start();
                }
            });
            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // TODO Auto-generated method stub
                    Log.e("ScreenSaver", "Something went wrong,maybe the platform dosen't support the format");
                    return true;
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
        mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWakeLock.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // 监控/拦截菜单键
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            // 由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    // 搜索键
    @Override
    public boolean onSearchRequested() {
        finish();
        return super.onSearchRequested();
    }

    @Override
    public void finish() {
        super.finish();
        if(bmp!=null){
            bmp.recycle();
        }
        overridePendingTransition(0, R.anim.alpha_out);
    }


}

package com.ricky.screensaver.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ricky.screensaver.MainActivity;
import com.ricky.screensaver.ScreenSaverActivity;
import com.ricky.screensaver.util.MyUtil;

public class ScreenSaverService extends Service {

    /**
     * 后台屏保服务，每隔一定的时间显示屏保
    */
    private HUDView mView;

    private Handler mHandler01 = new Handler();
    private Handler mHandler02 = new Handler();

    /* 上一次User有动作的Time Stamp */
    private long lastUpdateTime;
    /* 计算User有几秒没有动作的 */
    private long timePeriod;

    /* 静止超过N秒将自动进入屏保 */
    private static int mHoldStillTime = 30;
    /* 标识当前是否进入了屏保 */
    private boolean isRunScreenSaver;

    /* 时间间隔 */
    private long intervalScreenSaver = 1000;
    private long intervalKeypadeSaver = 1000;

    public ScreenSaverService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        lastUpdateTime = System.currentTimeMillis();
        addOverLayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler01.postAtTime(mTask01, intervalKeypadeSaver);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE))
                    .removeView(mView);
            mView = null;
        }
        mHandler01.removeCallbacks(mTask01);
        mHandler02.removeCallbacks(mTask02);
    }

    //从缓存中读取屏保时间
    private void updateHoldTime() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                MainActivity.SP_NAME, 0);
        mHoldStillTime = sharedPreferences
                .getInt(MainActivity.SPKEY_TIME, 60);
        Log.e("period from sp", mHoldStillTime + "");
    }

    //添加浮层
    private void addOverLayer() {
        mView = new HUDView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(0,
                0, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);
    }

    /**
     * 计时线程
     */
    private Runnable mTask01 = new Runnable() {

        @Override
        public void run() {
            /** 当前的系统时间 - 上次触摸屏幕的时间 = 静止不动的时间 **/
            timePeriod = System.currentTimeMillis() - lastUpdateTime;

			/* 将静止时间毫秒换算成秒 */
            long timePeriodSecond = ((long) timePeriod / 1000);
            updateHoldTime();
            Log.e("period time", timePeriodSecond + "");
            if (timePeriodSecond >= mHoldStillTime) {
                if (isRunScreenSaver == false) { // 说明没有进入屏保
					/* 启动线程去显示屏保 */
                    Log.e("want show ss", "yes");
                    mHandler02.postAtTime(mTask02, intervalScreenSaver);
					/* 显示屏保置为true */
                    isRunScreenSaver = true;
                } else {
					/* 屏保正在显示中 */
                    lastUpdateTime = System.currentTimeMillis();
                }
            } else {
				/* 说明静止之间没有超过规定时长 */
                isRunScreenSaver = false;
            }
			/* 反复调用自己进行检查 */
            mHandler01.postDelayed(mTask01, intervalKeypadeSaver);
        }
    };

    /**
     * 持续屏保显示线程
     */
    private Runnable mTask02 = new Runnable() {

        @Override
        public void run() {
            // 如果当前不处于屏保状态并且想要显示屏保，显示屏保
            if (isRunScreenSaver == true
                    && !MyUtil.isActivityOnTop(ScreenSaverService.this,
                    "ScreenSaverActivity")) {
                showScreenSaver();
            } else {
                mHandler02.removeCallbacks(mTask02); // 如果屏保没有显示则移除线程
            }
        }
    };

    /**
     * 显示屏保
     */
    private void showScreenSaver() {
        Log.d("danxx", "显示屏保------>");
        lastUpdateTime = System.currentTimeMillis();
        Intent intent = new Intent(getApplicationContext(),
                ScreenSaverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }

    //浮层视图
    class HUDView extends ViewGroup {

        public HUDView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }

        @Override
        protected void onLayout(boolean arg0, int arg1, int arg2, int arg3,
                                int arg4) {
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // 在接收到触屏事件时候，重置屏保计时
            Log.e("My Service", "trigger overlayer ontouch event,update time");
            lastUpdateTime = System.currentTimeMillis();
            return super.dispatchTouchEvent(ev);
        }

    }

    public static void setScreenSaverTimePeriod(int t) {
        mHoldStillTime = t;
    }

}

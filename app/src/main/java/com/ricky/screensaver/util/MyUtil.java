package com.ricky.screensaver.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.List;

public class MyUtil {

	public static String formatFileUri(Uri origin){
		String originStr = origin.toString();
		int index = originStr.indexOf(":///");
		return originStr.substring(index+3);
	}

	//判断
	public static boolean isActivityOnTop(Context context,String name) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cn = list.get(0).topActivity;
			String simpleName = getName(cn.getClassName());
			Log.e("cn name:", simpleName);

			if (simpleName.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static String getName(String origin) {
		int index = origin.lastIndexOf(".");
		String name = origin.substring(index + 1);
		return name;
	}
	
	//判断service是否在运行
	public static boolean isServiceRunning(Context mContext,String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
                   = activityManager.getRunningServices(30);

        if (!(serviceList.size()>0)) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
//        	Log.e("service name", serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        String msg = isRunning?"service is running":"service is not running";
        Log.e("MyUtil", msg);
        return isRunning;
    }

	//判断选中的文件是否是视频文件
	public static boolean isVideoFile(String name){
		if(name.endsWith(".mp4")||name.endsWith(".wmv")||name.endsWith(".3gp")){
			return true;
		}
		return false;
	}

	//判断选中的文件是否是图片文件
	public static boolean isImageFile(String name){
		if(name.endsWith(".png")||name.endsWith(".jpg")||name.endsWith(".jpeg")){
			return true;
		}
		return false;
	}
		
}

package com.ricky.screensaver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ricky.screensaver.service.ScreenSaverService;

public class RootBroadcastReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("system start", "start when system start");
		Intent in = new Intent(context,ScreenSaverService.class);
		context.startService(in);		
	}

}

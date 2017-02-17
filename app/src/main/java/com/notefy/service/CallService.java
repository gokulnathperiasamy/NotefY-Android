package com.notefy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CallService extends Service {
	
	private CallServiceHelper callServiceHelper;
 
	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		callServiceHelper = new CallServiceHelper(this);
		callServiceHelper.start();
		return super.onStartCommand(intent, flags, startID);
	}
	
    @Override
	public void onDestroy() {
		super.onDestroy();
		callServiceHelper.stop();
	}

	@Override
    public IBinder onBind(Intent intent) {
    	return null;
    }
}

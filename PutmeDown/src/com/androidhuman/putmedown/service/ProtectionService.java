package com.androidhuman.putmedown.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ProtectionService extends Service {
	
	private IBinder mBinder = new IProtectionService.Stub() {
		
		@Override
		public void enableService() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void disableService() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public interface AntiTheftListener{
		
	}

}

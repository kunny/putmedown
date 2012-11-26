package com.androidhuman.putmedown.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;

public class ProtectionService extends Service implements SensorEventListener{
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mOrientation;
	
	private IBinder mBinder = new IProtectionService.Stub() {
		
		@Override
		public void enableService() throws RemoteException {
			mSensorManager.registerListener(ProtectionService.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		@Override
		public void disableService() throws RemoteException {
			mSensorManager.unregisterListener(ProtectionService.this, mAccelerometer);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public interface AntiTheftListener{
		
	}
	
	// SensorEventListener

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}

}

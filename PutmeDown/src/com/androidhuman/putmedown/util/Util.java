package com.androidhuman.putmedown.util;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import com.androidhuman.putmedown.service.ProtectionService.AntiTheftListener;

public class Util {
	
	public static class Charging{
		private static final String KEY_LAST_PLUGGED_STATE = "last_plugged_state";
		
		public static void setPlugged(Context context, boolean plugged){
			getEditor(context).putBoolean(KEY_LAST_PLUGGED_STATE, plugged).commit();
		}
		
		public static boolean getLastPluggedState(Context context){
			return getPref(context).getBoolean(KEY_LAST_PLUGGED_STATE, false);
		}
		
	}
	
	public static class SensorSupport implements SensorEventListener{
		private static final String KEY_ALARM_FIRED = "alarm_fired";
		
		private boolean isTracking = false;
		
		private AntiTheftListener mListener;
		private SensorManager mSensorManager;
		private Sensor mAccelerometer;
		private Sensor mOrientation;
		private Context mContext;
		
		private SensorSupport(){
			
		}
		
		public SensorSupport(Context context, AntiTheftListener listener){
			this();
			mContext = context;
			mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			
			mListener = listener;
		}
		
		private void setAlarmFired(boolean fired){
			getEditor(mContext).putBoolean(KEY_ALARM_FIRED, fired).commit();
		}
		
		private boolean isAlarmFired(){
			return getPref(mContext).getBoolean(KEY_ALARM_FIRED, false);
		}
		
		public synchronized void startTracking(){
			isTracking = true;
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public synchronized void stopTracking(){
			isTracking = false;
			mSensorManager.unregisterListener(this, mAccelerometer);
		}
		
		public boolean isTracking(){
			return isTracking;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO watch sensor values and recognizes
			// that this device is in 'stable' state or not
			// if(theft_recognized){
			//     mListener.onAlarm();
			// }else if(isAlarmFired())
			//     mListener.onDismiss();
		}
	}
	
	public static class Security{
		private static final String KEY_PIN = "unlock_pin";
		public static final String DEFAULT_PIN = "not_set";
		
		public static void setUnlockPIN(Context context, String pin){
			getEditor(context).putString(KEY_PIN, pin).commit();
		}
		
		public static String getUnlockPIN(Context context){
			return getPref(context).getString(KEY_PIN, DEFAULT_PIN);
		}
		
		public static String generateRandomPIN(){
			return UUID.randomUUID().toString();
		}
	}
	
	private static SharedPreferences.Editor getEditor(Context context){
		return getPref(context).edit();
	}
	
	private static SharedPreferences getPref(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}

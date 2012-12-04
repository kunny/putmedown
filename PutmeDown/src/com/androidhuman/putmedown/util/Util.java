package com.androidhuman.putmedown.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;

import com.androidhuman.putmedown.R;
import com.androidhuman.putmedown.service.ProtectionService.AntiTheftListener;

public class Util {

	public static class Charging {
		private static final String KEY_LAST_PLUGGED_STATE = "last_plugged_state";

		public static void setPlugged(Context context, boolean plugged) {
			getEditor(context).putBoolean(KEY_LAST_PLUGGED_STATE, plugged)
					.commit();
		}

		public static boolean getLastPluggedState(Context context) {
			return getPref(context).getBoolean(KEY_LAST_PLUGGED_STATE, false);
		}

	}

	public static class SensorSupport implements SensorEventListener {
		private static final String TAG = "SensorSupport";
		private static final String KEY_ALARM_FIRED = "alarm_fired";

		private boolean isTracking = false;

		private AntiTheftListener mListener;
		private SensorManager mSensorManager;
		private Sensor mAccelerometer;
		private Sensor mOrientation;
		private Context mContext;
		
		private ArrayList<Float> mAxis = new ArrayList<Float>();

		private float axis_x = 0.f;
		private float axis_y = 0.f;
		private float axis_z = 0.f;
		private float velo_x = 0.f;
		private float velo_y = 0.f;
		private float velo_z = 0.f;
		private float rot_x = 0.f;
		private float rot_y = 0.f;
		private float rot_z = 0.f;
		private float acc_x = 0.f;
		private float acc_y = 0.f;
		private float acc_z = 0.f;
		private float rot_criteria = 10.f;
		private float acc_criteria = 3.f;

		private int sensorChanged = 0;

		private SensorSupport() {

		}

		@SuppressWarnings("deprecation")
		public SensorSupport(Context context, AntiTheftListener listener) {
			this();
			mContext = context;
			mSensorManager = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			mAccelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mOrientation = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);

			mListener = listener;
		}

		public void setAlarmFired(boolean fired) {
			getEditor(mContext).putBoolean(KEY_ALARM_FIRED, fired).commit();
		}

		public boolean isAlarmFired() {
			return getPref(mContext).getBoolean(KEY_ALARM_FIRED, false);
		}

		public synchronized void startTracking() {
			isTracking = true;
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mOrientation,
					SensorManager.SENSOR_DELAY_NORMAL);

			Log.d(TAG, "Sensor tracking started.");
		}

		public synchronized void stopTracking() {
			isTracking = false;
			mSensorManager.unregisterListener(this, mAccelerometer);
			mSensorManager.unregisterListener(this, mOrientation);
			Log.d(TAG, "Sensor tracking stopped.");
		}

		public boolean isTracking() {
			return isTracking;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.d(TAG, "onAccuracyChanged: " + sensor + ", accuracy: "
					+ accuracy);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO watch sensor values and recognizes
			// that this device is in 'stable' state or not
			// if(theft_recognized){
			// mListener.onAlarm();
			// }else if(isAlarmFired())
			// mListener.onDismiss();

			if (event.sensor == mOrientation) {
				rot_x = event.values[0];
				rot_y = event.values[1];
				rot_z = event.values[2];
				Log.d(TAG, "onSensorChanged-rot: " + rot_x + ", " + rot_y + ", "
						+ rot_z);
				sensorChanged++;
			} else if (event.sensor == mAccelerometer) {
				acc_x = event.values[0];
				acc_y = event.values[0];
				acc_z = event.values[0];
				Log.d(TAG, "onSensorChanged-acc: " + acc_x + ", " + acc_y + ", "
						+ acc_z);
			}

			if (sensorChanged == 1) {
				axis_x = rot_x;
				axis_y = rot_y;
				axis_z = rot_z;
				mAxis.add(axis_x);
				mAxis.add(axis_y);
				mAxis.add(axis_z);
				Log.w(TAG, "onSensorChanged-axis: " + axis_x + ", " + axis_y + ", "
						+ axis_z);
			} else if (sensorChanged >= 2) {

				if (Math.abs(mAxis.get(0) - rot_x) > rot_criteria
						|| Math.abs(mAxis.get(1) - rot_y) > rot_criteria
						|| Math.abs(mAxis.get(2) - rot_z) > rot_criteria) {
					mListener.onAlarm();
					Log.w(TAG, "Ring the Alarm-rot");
					Log.w(TAG, "onSensorChanged-axis: " + mAxis.get(0) + ", " + mAxis.get(1) + ", "
							+ mAxis.get(2));
					Log.e(TAG, "sub: " + Math.abs(mAxis.get(0) - rot_x) + ", " + Math.abs(mAxis.get(1) - rot_y) + ", "
						+ Math.abs(mAxis.get(2) - rot_z));
				}else if(Math.abs(velo_x - acc_x) > acc_criteria
						|| Math.abs(velo_y - acc_y) > acc_criteria
						|| Math.abs(velo_z - acc_z) > acc_criteria){
					mListener.onAlarm();
					Log.w(TAG, "Ring the Alarm-acc");
				}else if(isAlarmFired()){
					mListener.onDismiss();
				}
			}
		}
	}

	public static class Security {
		private static final String KEY_PIN = "unlock_pin";
		private static final String KEY_PIN_NFC = "unlock_tag";

		public static final String DEFAULT_PIN = "not_set";

		public enum PinType {
			PIN, TAG
		};

		public static void setUnlockPIN(Context context, PinType type,
				String pin) {
			if (type == PinType.PIN) {
				getEditor(context).putString(KEY_PIN, pin).commit();
			} else {
				getEditor(context).putString(KEY_PIN_NFC, pin).commit();
			}
		}

		public static String getUnlockPIN(Context context, PinType type) {
			return PinType.PIN == type ? getPref(context).getString(KEY_PIN,
					DEFAULT_PIN) : getPref(context).getString(KEY_PIN_NFC,
					DEFAULT_PIN);
		}

		public static boolean isPINSet(Context context, PinType type) {
			if (type == PinType.PIN) {
				return getPref(context).getString(KEY_PIN, DEFAULT_PIN).equals(
						DEFAULT_PIN) ? false : true;
			} else {
				return getPref(context).getString(KEY_PIN_NFC, DEFAULT_PIN)
						.equals(DEFAULT_PIN) ? false : true;
			}
		}

		public static String generateRandomPIN() {
			return UUID.randomUUID().toString();
		}
	}
	public static class SoundSupport{
		
		public enum SoundType{ACTIVATE, CHARGER_PLUGGED, CHARGER_DETACHED, WARNING, ALARM, DISMISS, SHUTDOWN, ERROR};
		
		private Context mContext;
		private SoundPool mSoundPool;
		private Random mRandom;
		
		private String[] mActivateSounds;
		private String[] mChargerPluggedSounds;
		private String[] mChargerDetachedSounds;
		private String[] mWarningSounds;
		private String[] mAlarmSounds;
		private String[] mDismissSounds;
		private String[] mShutdownSounds;
		private String[] mErrorSounds;
		
		private HashMap<String, Integer> mSoundIds;
		
		public SoundSupport(Context context){
			mContext = context;
			mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
			mRandom = new Random();
			
			mActivateSounds = mContext.getResources().getStringArray(R.array.activate_sounds);
			mChargerPluggedSounds = mContext.getResources().getStringArray(R.array.charger_plugged_sounds);
			mChargerDetachedSounds = mContext.getResources().getStringArray(R.array.charger_detached_sounds);
			mWarningSounds = mContext.getResources().getStringArray(R.array.warning_sounds);
			mAlarmSounds = mContext.getResources().getStringArray(R.array.alarm_sounds);
			mDismissSounds = mContext.getResources().getStringArray(R.array.dismiss_sounds);
			mShutdownSounds = mContext.getResources().getStringArray(R.array.shutdown_sounds);
			mErrorSounds = mContext.getResources().getStringArray(R.array.error_sounds);
			
			mSoundIds = new HashMap<String, Integer>();
			
		}
		
		public void loadSounds(){
			loadSoundInArray(mActivateSounds);
			loadSoundInArray(mChargerPluggedSounds);
			loadSoundInArray(mChargerDetachedSounds);
			loadSoundInArray(mWarningSounds);
			loadSoundInArray(mAlarmSounds);
			loadSoundInArray(mDismissSounds);
			loadSoundInArray(mShutdownSounds);
			loadSoundInArray(mErrorSounds);
		}
		
		private void loadSoundInArray(String[] sounds){
			for(String soundName : sounds){
				int soundId = mSoundPool.load(mContext, getSoundIdByName(soundName), 1);
				mSoundIds.put(soundName, soundId);
			}
		}
		
		public void play(SoundType type){
			
			switch(type){
			case ACTIVATE:
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mActivateSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			case CHARGER_PLUGGED:
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mChargerPluggedSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			case CHARGER_DETACHED:
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mChargerDetachedSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			case WARNING:
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mWarningSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			case ALARM:
				mSoundPool.play(mSoundIds.get(mAlarmSounds[0]), 1.0f, 1.0f, 1, 0, 1.0f);
				mSoundPool.play(mSoundIds.get(mAlarmSounds[1]), 1.0f, 1.0f, 1, -1, 1.0f);
				break;
				
			case DISMISS:
				mSoundPool.stop(mSoundIds.get(mAlarmSounds[1]));
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mDismissSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			case SHUTDOWN:
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mShutdownSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			case ERROR:
				mSoundPool.play(mSoundIds.get(getRandomSoundNameOnList(mErrorSounds)), 1.0f, 1.0f, 1, 0, 1.0f);
				break;
				
			}
		}
		
		public void play(String soundName){
			mSoundPool.play(mSoundIds.get(soundName), 1.0f, 1.0f, 1, 0, 1.0f);
		}
		
		private String getRandomSoundNameOnList(String[] list){
			return list[getRandomIndex(list.length)];
		}
		
		private int getSoundIdByName(String name) throws IllegalArgumentException{
			int id = mContext.getResources().getIdentifier(name, "raw", mContext.getPackageName());
			if(id==0){
				throw new IllegalArgumentException("Cannot find sound resource with given name="+name);
			}
			
			return id;
		}
		
		private int getRandomIndex(int size){
			return mRandom.nextInt(size);
		}
		
		public void destroy(){
			mSoundPool.release();
		}
		
		@Override
		protected void finalize(){
			destroy();
		}
	}

	private static SharedPreferences.Editor getEditor(Context context) {
		return getPref(context).edit();
	}
	
	private static SharedPreferences getPref(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}

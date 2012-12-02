package com.androidhuman.putmedown.util;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

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

		private float axis_x;
		private float axis_y;
		private float axis_z;
		private float velo_x = 0.f;
		private float velo_y = 0.f;
		private float velo_z = 0.f;
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

			float rot_x = 0.f;
			float rot_y = 0.f;
			float rot_z = 0.f;
			float acc_x = 0.f;
			float acc_y = 0.f;
			float acc_z = 0.f;

			if (event.sensor == mOrientation) {
				rot_x = event.values[0];
				rot_y = event.values[1];
				rot_z = event.values[2];
				Log.d(TAG, "onSensorChanged: " + rot_x + ", " + rot_y + ", "
						+ rot_z);
			} else if (event.sensor == mAccelerometer) {
				acc_x = event.values[0];
				acc_y = event.values[0];
				acc_z = event.values[0];
				Log.d(TAG, "onSensorChanged: " + acc_x + ", " + acc_y + ", "
						+ acc_z);
			}

			if (sensorChanged == 0) {
				axis_x = rot_x;
				axis_y = rot_y;
				axis_z = rot_z;
			} else if (sensorChanged >= 1) {

				if (Math.abs(axis_x - rot_x) > rot_criteria
						|| Math.abs(axis_y - rot_y) > rot_criteria
						|| Math.abs(axis_z - rot_z) > rot_criteria) {
					mListener.onAlarm();
				}else if(Math.abs(velo_x - acc_x) > acc_criteria
						|| Math.abs(velo_y - acc_y) > acc_criteria
						|| Math.abs(velo_z - acc_z) > acc_criteria){
					mListener.onAlarm();
				}else if(isAlarmFired()){
					mListener.onDismiss();
				}
			}

			sensorChanged++;

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

	private static SharedPreferences.Editor getEditor(Context context) {
		return getPref(context).edit();
	}

	private static SharedPreferences getPref(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}

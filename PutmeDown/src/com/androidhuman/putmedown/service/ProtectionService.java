package com.androidhuman.putmedown.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.androidhuman.putmedown.R;
import com.androidhuman.putmedown.activity.NfcUnlockActivity;
import com.androidhuman.putmedown.activity.PinUnlockActivity;
import com.androidhuman.putmedown.util.Util;
import com.androidhuman.putmedown.util.Util.SensorSupport;

public class ProtectionService extends Service{
	
	private AntiTheftListener mAntiTheftListener = new AntiTheftListener(){

		@Override
		public void onChargerStateChanged(boolean plugged) {
			// If charger has plugged in, stop listening sensor values.
			if(plugged){
				mSensorSupport.stopTracking();
				
			}else{ // charger has detached. Enable sensor-based protection service.
				mSensorSupport.startTracking();
				
			}
		}
		
		@Override
		public void onWarning() {
			// TODO play warning sound
		}

		@Override
		public void onAlarm() {
			showNotification();
			// TODO Set device's media volume to MAX
			// TODO Play alarm sound
		}

		@Override
		public void onDismiss() {
			dismissNotification();
			// TODO stop alarm sound
		}

	};
	
	private IBinder mBinder = new IProtectionService.Stub() {
		
		@Override
		public void enableService() throws RemoteException {
			enableChargerTracking();
			
		}
		
		@Override
		public void disableService() throws RemoteException {
			disableChargerTracking(true);
		}

		@Override
		public void fireWarning() throws RemoteException {
			mAntiTheftListener.onWarning();
			
		}

		@Override
		public void fireAlarm() throws RemoteException {
			mAntiTheftListener.onAlarm();
		}

		@Override
		public void dismissAlarm() throws RemoteException {
			mAntiTheftListener.onDismiss();
		}
	};
	
	@SuppressWarnings("deprecation")
	private void showNotification(){
		Notification notification = new Notification();
		String unlockMethod = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext())
				.getString("unlock_method", "pin");
		
		PendingIntent intent = 
				PendingIntent.getActivity(getApplicationContext(), 0, 
						new Intent(this, unlockMethod.equals("pin") ? PinUnlockActivity.class : NfcUnlockActivity.class), 0);
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = getString(com.androidhuman.putmedown.R.string.anti_theft_alarm);
		notification.when = System.currentTimeMillis();
		notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.enter_pin_or_tag_to_dismiss), intent);
		
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(0, notification);
	}
	
	private void dismissNotification(){
		NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancelAll();
	}
	
	private void enableChargerTracking(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBatteryReceiver, filter);
	}
	
	private void disableChargerTracking(boolean disablingService){
		unregisterReceiver(mBatteryReceiver);
		if(!disablingService && !mSensorSupport.isTracking()){
			mSensorSupport.startTracking();
		}
	}
	
	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			int pluggedState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			
			// If device is not connected to charger
			if(pluggedState==0){
				Util.Charging.setPlugged(getApplicationContext(), false);
				mAntiTheftListener.onChargerStateChanged(false);
			}else{
				Util.Charging.setPlugged(getApplicationContext(), true);
				mAntiTheftListener.onChargerStateChanged(true);
			}
		}
		
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private SensorSupport mSensorSupport;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mSensorSupport = new SensorSupport(this, mAntiTheftListener);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	public interface AntiTheftListener{
		
		/**
		 * Called when charger plugged state has changed.
		 * @param plugged true when charger has attached to device, false otherwise
		 */
		public void onChargerStateChanged(boolean plugged);
		
		/**
		 * Called when device's state is unstable but still insufficient to invoke alarm
		 */
		public void onWarning();
		
		/**
		 * Called when device is about to stolen.
		 */
		public void onAlarm();
		
		/**
		 * Called when user dismissed alarm.
		 */
		public void onDismiss();
		
	}

}

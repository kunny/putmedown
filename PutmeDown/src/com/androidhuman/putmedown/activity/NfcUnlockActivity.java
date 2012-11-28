package com.androidhuman.putmedown.activity;

import java.nio.charset.Charset;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.widget.Toast;

import com.androidhuman.putmedown.R;
import com.androidhuman.putmedown.service.IProtectionService;
import com.androidhuman.putmedown.service.ProtectionService;
import com.androidhuman.putmedown.util.Util.Security;

public class NfcUnlockActivity extends Activity {
	
	private IProtectionService mService;
	private ServiceConnection conn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IProtectionService.Stub.asInterface(service);
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_nfc_unlock);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		bindService(new Intent(this, ProtectionService.class), conn, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(mService!=null){
			unbindService(conn);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        if (rawMsgs != null) {
	            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
	            for (int i = 0; i < rawMsgs.length; i++) {
	                msgs[i] = (NdefMessage) rawMsgs[i];
	            }
	            
	            if(msgs.length!=1){
	            	Toast.makeText(getApplicationContext(), R.string.invalid_tag, Toast.LENGTH_SHORT).show();
	            }else{
	            	NdefRecord[] records = msgs[0].getRecords();
	            	NdefRecord pinRecord = records[0];
	            	String pin = new String(pinRecord.getPayload(), Charset.forName("US-ASCII"));
	  
	            	String pinInPref = Security.getUnlockPIN(this);
	            	if(pinInPref.equals(pin)){
	            		try{
	            			mService.dismissAlarm();
	            			// TODO Play dismiss sound
	            		}catch(RemoteException e){
	            			e.printStackTrace();
	            		}
	            	}else{
	            		Toast.makeText(getApplicationContext(), R.string.invalid_tag, Toast.LENGTH_SHORT).show();
	            		// TODO play error sound
	            	}
	            }
	        }
		}
	}

}

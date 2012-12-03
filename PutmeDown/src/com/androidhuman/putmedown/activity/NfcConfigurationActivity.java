package com.androidhuman.putmedown.activity;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.androidhuman.putmedown.R;
import com.androidhuman.putmedown.service.IProtectionService;
import com.androidhuman.putmedown.service.ProtectionService;
import com.androidhuman.putmedown.util.Util;
import com.androidhuman.putmedown.util.Util.Security;
import com.androidhuman.putmedown.util.Util.Security.PinType;

public class NfcConfigurationActivity extends Activity {

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
	
	NfcAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.acrivity_nfc_configuration);
	    mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
	    if(mAdapter==null){
	    	throw new RuntimeException("NFC is not supported.");
	    }
	}
	
	@Override
	public void onResume(){
		super.onResume();
		bindService(new Intent(this, ProtectionService.class), conn, Context.BIND_AUTO_CREATE);
		PendingIntent pendingIntent = PendingIntent.getActivity(
	    	    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	    
	    mAdapter.enableForegroundDispatch(this, pendingIntent, 
	    		new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)}, 
	    		new String[][]{new String[]{Ndef.class.getName()}});
	}
	
	@Override
	public void onPause(){
		super.onPause();
		unbindService(conn);
		mAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Ndef ndef = Ndef.get(tag);
		boolean succeed = false;
		
		try{
			// Connect and write a PIN data into NFC Tag
			ndef.connect();
			ndef.writeNdefMessage(generateNfcMessage());
			succeed = true;
			
		}catch(IOException e){
			e.printStackTrace();
			succeed = false;
		} catch (FormatException e) {
			e.printStackTrace();
			succeed = false;
		}finally{
			try {
				ndef.close();
			} catch (IOException e) {
			}
		}
		
		if(succeed){
			try {
				mService.playSound("dispensing_product");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Toast.makeText(this, "Tag generated!", Toast.LENGTH_LONG).show();
			finish();
		}else{
			try {
				mService.playSound("critical_error");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Toast.makeText(this, "Failed to write information on Tag.", Toast.LENGTH_LONG).show();
		}
		
	}
	
	private NdefMessage generateNfcMessage(){
		String pin = Security.generateRandomPIN();
		Util.Security.setUnlockPIN(this, PinType.TAG, pin);
		NdefRecord record = new NdefRecord(
			    NdefRecord.TNF_MIME_MEDIA ,
			    "application/vnd.com.androidhuman.putmedown".getBytes(Charset.forName("US-ASCII")),
			    new byte[0], pin.getBytes(Charset.forName("US-ASCII")));
		NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
		return msg;
	}

}

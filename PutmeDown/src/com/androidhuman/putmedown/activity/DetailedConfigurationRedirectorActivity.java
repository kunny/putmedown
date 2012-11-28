package com.androidhuman.putmedown.activity;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidhuman.putmedown.R;
import com.androidhuman.putmedown.util.Util;
import com.androidhuman.putmedown.util.Util.Security;
import com.androidhuman.putmedown.util.Util.Security.PinType;

public class DetailedConfigurationRedirectorActivity extends Activity {
	
	TextView tvEnterPin;
	EditText edtPin;
	Button btnDone;
	TextView tvTouchTag;
	
	private boolean useForegroundDispatch = false;
	private NfcAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    String unlockMethod = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("unlock_method", "pin");
	   
	    // If no PIN has been set
	    if(unlockMethod.equals("pin") && !Security.isPINSet(getApplicationContext(), PinType.PIN)){
	    	startActivity(new Intent(this, PinConfigurationActivity.class));
	    	finish();
	    	return;
	    }else if(unlockMethod.equals("nfc") && !Security.isPINSet(getApplicationContext(), PinType.TAG)){
	    	startActivity(new Intent(this, NfcConfigurationActivity.class));
	    	finish();
	    	return;
	    }
		    
	    
	    setContentView(R.layout.activity_detailed_configuration_redirector);
	    
	    tvEnterPin = (TextView)findViewById(R.id.tv_activity_detailed_configuration_redirector_enter_pin);
	    edtPin = (EditText)findViewById(R.id.edt_activity_detailed_configuration_redirector_pin_input);
	    btnDone = (Button)findViewById(R.id.btn_activity_detailed_configuration_redirector_done);
	    tvTouchTag = (TextView)findViewById(R.id.tv_activity_detailed_configuration_redirector_touch_tag);
	    
	    if(unlockMethod.equals("pin")){
	    	tvTouchTag.setVisibility(View.GONE);
	    	useForegroundDispatch = false;
	    	
	    	edtPin.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if(edtPin.length() <4){
						btnDone.setEnabled(false);
					}else{
						btnDone.setEnabled(true);
					}
				}
	    		
	    	});
	    	
	    	btnDone.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					String pin = Util.Security.getUnlockPIN(getApplicationContext(), PinType.PIN);
					if(pin.equals(edtPin.getText().toString())){
						startActivity(new Intent(DetailedConfigurationRedirectorActivity.this, PinConfigurationActivity.class));
				    	finish();
				    	return;
					}else{
						Toast.makeText(getApplicationContext(), R.string.invalid_pin, Toast.LENGTH_SHORT).show();
					}
				}
	    		
	    	});
	    	
	    	
	    }else{
	    	tvEnterPin.setVisibility(View.GONE);
	    	edtPin.setVisibility(View.GONE);
	    	btnDone.setVisibility(View.GONE);
	    	useForegroundDispatch = true;
	    	mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
	    	if(mAdapter==null){
	    		throw new RuntimeException("NFC is not available.");
	    	}
	    }
	    
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(useForegroundDispatch){
			PendingIntent pendingIntent = PendingIntent.getActivity(
		    	    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		    
		    mAdapter.enableForegroundDispatch(this, pendingIntent, 
		    		new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)}, 
		    		new String[][]{new String[]{Ndef.class.getName()}});
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(useForegroundDispatch){
			mAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
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
  
            	String pinInPref = Security.getUnlockPIN(this, PinType.TAG);
            	if(pinInPref.equals(pin)){
            		String unlockMethod = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("unlock_method", "pin");
            	    
        		    if(unlockMethod.equals("pin")){
        		    	startActivity(new Intent(this, PinConfigurationActivity.class));
        		    }else{
        		    	startActivity(new Intent(this, NfcConfigurationActivity.class));
        		    }
        		    finish();
        		    return;
            	    
            	}else{
            		Toast.makeText(getApplicationContext(), R.string.invalid_tag, Toast.LENGTH_SHORT).show();
            		// TODO play error sound
            	}
            }
        }
	}

}

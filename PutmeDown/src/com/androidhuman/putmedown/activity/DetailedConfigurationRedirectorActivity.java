package com.androidhuman.putmedown.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class DetailedConfigurationRedirectorActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    String value = PreferenceManager
	    		.getDefaultSharedPreferences(getApplicationContext())
	    		.getString("unlock_method", "pin");
	    if(value.equals("pin")){
	    	startActivity(new Intent(this, PinConfigurationActivity.class));
	    }else{
	    	startActivity(new Intent(this, NfcConfigurationActivity.class));
	    }
	    finish();
	}

}

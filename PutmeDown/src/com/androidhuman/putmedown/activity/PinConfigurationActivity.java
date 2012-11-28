package com.androidhuman.putmedown.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidhuman.putmedown.R;
import com.androidhuman.putmedown.util.Util.Security;

public class PinConfigurationActivity extends Activity {

	EditText edtPin;
	Button btnDone;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_pin_configuration);
	    edtPin = (EditText)findViewById(R.id.edt_activity_configuration_pin_input);
	    btnDone = (Button)findViewById(R.id.btn_activity_pin_configuration_done);
	    
	    edtPin.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable edi) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int end,
					int count) {
				if(edtPin.length()>=4){
					btnDone.setEnabled(true);
				}else{
					btnDone.setEnabled(false);
				}
			}
	    	
	    });
	    
	    btnDone.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Security.setUnlockPIN(getApplicationContext(), edtPin.getText().toString());
				Toast.makeText(getApplicationContext(), R.string.pin_has_been_set, Toast.LENGTH_SHORT).show();
				finish();
			}
	    	
	    });
	}

}

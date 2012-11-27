package com.androidhuman.putmedown.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.androidhuman.putmedown.R;

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
	}

}

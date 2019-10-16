package com.example.android.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.TextView;
import android.util.Log;


public class BlueActivity extends Activity {
	
	private static final String TAG = "BlueVvnx";
	private BleGattVvnx mBleGattVvnx;

	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        

		mBleGattVvnx = new BleGattVvnx();

    }
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
		mBleGattVvnx.connectEnGatt(this);
	}
	
	public void ActionPressBouton_2(View v) {
		Log.d(TAG, "press bouton 2");
	}
	
	public void ActionPressBouton_3(View v) {
		Log.d(TAG, "press bouton 3");
	}
	
	public void ActionPressBouton_4(View v) {
		Log.d(TAG, "press bouton 4");
	}
	
	public void updateText(String myString) {
		TextView textview1 = findViewById(R.id.text1);	
        textview1.setText(myString);
    }
	
}


package com.example.android.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.util.Log;


public class BlueActivity extends Activity {
	private static final String TAG = "BlueVvnx";
		

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity.  You can find it
        // in res/layout/hello_activity.xml
        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        
        final Button button1 = findViewById(R.id.button_1);
        final Button button2 = findViewById(R.id.button_2);
        final Button button3 = findViewById(R.id.button_3);
        final Button button4 = findViewById(R.id.button_4);


    }
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
		BleGattVvnx.connectEnGatt(this);
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
	
}


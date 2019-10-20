package com.example.android.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;

import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class BlueActivity extends Activity {
	
	private static final String TAG = "BlueVvnx";
	private BleGattVvnx mBleGattVvnx;
	private Drawable default_btn;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        
        //récup le background par default du bouton pour le remettre
        Button button1 = findViewById(R.id.button_1);
        default_btn = button1.getBackground();
        

		mBleGattVvnx = new BleGattVvnx();

    }
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
		mBleGattVvnx.connectmGatt(this);
	}
	
	public void ActionPressBouton_3(View v) {
		Log.d(TAG, "press bouton 3");
		mBleGattVvnx.disconnectmGatt();
	}
	
	public void ActionPressBouton_2(View v) {
		Log.d(TAG, "press bouton 2");
		//foreground service pour importance (am package-importance com.example.android.hellogps) à 125
		startForegroundService(new Intent(this, BlueService.class));			
	}

	public void ActionPressBouton_4(View v) {
		Log.d(TAG, "press bouton 4");
		stopService(new Intent(this, BlueService.class));
	}
	
	public void updateText(String myString) {
		TextView textview1 = findViewById(R.id.text1);	
        textview1.setText(myString);
    }
    
    public void btn1_to_blue() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundColor(Color.BLUE);
	}
	
	public void btn1_to_def() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundDrawable(default_btn);
	}
	
}


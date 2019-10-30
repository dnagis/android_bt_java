package com.example.android.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class BlueActivity extends Activity implements OnItemSelectedListener {
	
	private static final String TAG = "BlueVvnx";
	private BleGattVvnx mBleGattVvnx;
	private Drawable default_btn;
	private Spinner bdarr_spinner;
	public String BDADDR;
	
	//private final String BDADDR = "30:AE:A4:04:C3:5A"; //plaque de dev
	//private final String BDADDR = "30:AE:A4:07:84:16"; //breakout rouge pour tests doorlock
	//private final String BDADDR = "30:AE:A4:45:C8:26"; //premier anémo

    @Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        
        //récup le background par default du bouton pour le remettre
        Button button1 = findViewById(R.id.button_1);
        default_btn = button1.getBackground();
        
        //menu déroulant choix BDADDR
        bdarr_spinner = (Spinner) findViewById(R.id.bdaddr_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.bdaddr_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bdarr_spinner.setAdapter(adapter); 
		bdarr_spinner.setOnItemSelectedListener(this);               

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
		Log.d(TAG, "updateText dans BlueActivity");
		TextView textview1 = findViewById(R.id.text1);	
        textview1.setText(myString);
        textview1.invalidate();
    }
    
    public void btn1_to_blue() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundColor(Color.BLUE);
	}
	
	public void btn1_to_def() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundDrawable(default_btn);
	}
	
	public void onItemSelected(AdapterView<?> parent, View view,
            int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.d(TAG, "spinner selected: " + parent.getItemAtPosition(pos));
        BDADDR = "30:AE:A4:" + parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
	
}


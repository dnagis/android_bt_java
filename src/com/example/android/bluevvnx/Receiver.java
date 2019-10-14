package com.example.android.bluevvnx;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
		private static final String TAG = "BlueVvnx";
	
	 @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive dans mon Receiver, action=" + intent.getAction());
        
        if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
			Log.d(TAG, "intent bond state changed received");	
			
		}
		
	}
	
	
	
}

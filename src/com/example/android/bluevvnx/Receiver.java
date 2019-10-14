package com.example.android.bluevvnx;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;


public class Receiver extends BroadcastReceiver {
	
	private static final String TAG = "BlueVvnx";
	private Context mContext;

	
	@Override
    public void onReceive(Context context, Intent intent) {
		mContext = context;
        Log.d(TAG, "onReceive dans mon Receiver, action=" + intent.getAction());
        
        if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
			Log.d(TAG, "intent bond state changed received");
			BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(TAG, "adresse=" + mDevice.getAddress());
			if (mDevice.getAddress().equals("30:AE:A4:04:C3:5A")) {
				connecteLeDevice(mDevice);
			}			
		}		
	}
	
	private void connecteLeDevice(BluetoothDevice mDevice){
			Log.d(TAG, "connexion...");
			BluetoothGatt mBluetoothGatt = mDevice.connectGatt(mContext, true, gattCallback);			
	}
	
	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }
        
        @Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
					Log.i(TAG, "onServicesDiscovered callback.");
				}	
			
        }

        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				Log.i(TAG, "onCharacteristicRead callback.");
        }
        
        
        //réception des notifications: 
        //côté serveur esp32: esp_ble_gatts_send_indicate(0x03, 0, gl_profile_tab[PROFILE_A_APP_ID].char_handle, sizeof(notify_data), notify_data, false);
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				Log.i(TAG, "onCharacteristicChanged callback.");
        }	
	};
	
	
	
}

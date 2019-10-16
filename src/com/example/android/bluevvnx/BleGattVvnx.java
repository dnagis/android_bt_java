package com.example.android.bluevvnx;

import android.content.Context;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import java.util.UUID;

import android.view.View;

public class BleGattVvnx  {

	private Context mContext;
	private BlueActivity mBlueActivity;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothAdapter mBluetoothAdapter = null;	
	private BluetoothGattCharacteristic mCharacteristic = null;	
	private final String TAG = "BlueVvnx";
	private final String BDADDR = "30:AE:A4:04:C3:5A";
	//uuid du service: gatttool --> [30:AE:A4:04:C3:5A][LE]> primary
    private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
    //[30:AE:A4:04:C3:5A][LE]> characteristics
	private static final UUID CHARACTERISTIC_PRFA_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
	
	 
	void connectEnGatt(Context rContext){
	mContext = rContext;
	mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
	final BluetoothManager bluetoothManager = (BluetoothManager)mContext.getSystemService(mContext.BLUETOOTH_SERVICE);	
	mBluetoothAdapter = bluetoothManager.getAdapter();	
	if (mBluetoothAdapter == null) {
		Log.d(TAG, "fail à la récup de l'adapter");
		return;
		}
	BluetoothDevice monEsp = mBluetoothAdapter.getRemoteDevice(BDADDR);        
    mBluetoothGatt = monEsp.connectGatt(mContext, true, gattCallback);
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
			Log.i(TAG, "onServicesDiscovered callback.");
			mCharacteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_PRFA_UUID);
			gatt.setCharacteristicNotification(mCharacteristic, true);
	}

	
	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicRead callback.");
	}
	
	
	//réception des notifications: 
	//côté serveur esp32: esp_ble_gatts_send_indicate(0x03, 0, gl_profile_tab[PROFILE_A_APP_ID].char_handle, sizeof(notify_data), notify_data, false);
	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			String timeStamp = String.valueOf(System.currentTimeMillis());
			Log.i(TAG, "onCharacteristicChanged callback @" + timeStamp);
			mBlueActivity.updateText(timeStamp);
			}	
	};
	  

	      

}

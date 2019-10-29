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

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

public class BleGattVvnx  {
	
	private final String BDADDR = "30:AE:A4:04:C3:5A"; //plaque de dev
	//private final String BDADDR = "30:AE:A4:07:84:16"; //breakout rouge pour tests doorlock

	private Context mContext;
	private BlueActivity mBlueActivity;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothAdapter mBluetoothAdapter = null;	
	private BluetoothGattCharacteristic mCharacteristic = null;	
	private final String TAG = "BlueVvnx";
	
	
	//uuid du service: gatttool --> [30:AE:A4:04:C3:5A][LE]> primary
    private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
    /**[30:AE:A4:04:C3:5A][LE]> characteristics
    *...
    *handle: 0x0029, char properties: 0x1a, char value handle: 0x002a, uuid: 0000ff01-0000-1000-8000-00805f9b34fb
    *si je comprends bien quand tu vois ça avec gatttool faut que tu send la notif côté esp32 (esp_ble_gatts_send_indicate) avec attr_handle (arg 3) -> 0x002a**/
	private static final UUID CHARACTERISTIC_PRFA_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
	
	//sql
    private BaseDeDonnees maBDD;
    private SQLiteDatabase bdd;
	
	 
	void connectmGatt(Context rContext){
		mContext = rContext;
		
		final BluetoothManager bluetoothManager = (BluetoothManager)mContext.getSystemService(mContext.BLUETOOTH_SERVICE);	
		mBluetoothAdapter = bluetoothManager.getAdapter();	
		if (mBluetoothAdapter == null) {
			Log.d(TAG, "fail à la récup de l'adapter");
			return;
		}
		
		BluetoothDevice monEsp = mBluetoothAdapter.getRemoteDevice(BDADDR);   
		
		if (mBluetoothGatt == null) {
			Log.d(TAG, "pas encore de mBluetoothGatt: on la crée");
			mBluetoothGatt = monEsp.connectGatt(mContext, true, gattCallback);
		} else {
			mBluetoothGatt.connect();
		}
		     
		

	}
	
	void disconnectmGatt(){
		mBluetoothGatt.disconnect();
	}
	
	/**
	 * 
	 * 
	 * 
	 * le gros paquet de callbacks GATT
	 * 
	 * 
	 * 
	 * **/
	
	
	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
		if (newState == BluetoothProfile.STATE_CONNECTED) {
			Log.i(TAG, "Connected to GATT server.");					
			mBlueActivity.btn1_to_blue();
			gatt.discoverServices();
		} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
			Log.i(TAG, "Disconnected from GATT server.");
			mBlueActivity.btn1_to_def();
		}
        //si je mets pas ça  j'ai n+1 onCharacteristicChanged() à chaque passage (nouvelle instance BluetoothGattCallback?)
		//***MAIS***
		//close() la connexion du coup j'ai pas d'auto-reconnect...
		//mBluetoothGatt.close(); 
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
			Log.i(TAG, "Rx notif: onCharacteristicChanged");
			byte[] data = characteristic.getValue();
			//parseBMX280(data);	
			parseGPIO(data);
			}	
	};
	  
	//anémo (esp32: gatts_gpio) encodage dans 2 bytes
	private void parseGPIO(byte[] data) {
			int valeur = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
			Log.i(TAG, "parseGPIO data: "+valeur);			
			//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
			mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
			mBlueActivity.updateText(String.valueOf(valeur));
	 }

	private void parseBMX280(byte[] data) {
		long ts = System.currentTimeMillis()/1000;
		//voir esp32_bmx280_gatts pour l'encodage des valeurs dans un array de bytes
		double temp = (double)(data[0]+(data[1]/100.0));
        if (data[2]==0) temp=-temp;
        double press = (double)(data[3]+872+(data[4]/100.0));
        double hum = (double)(data[5]+(data[6]/100.0));		
		Log.i(TAG, "recup data de la characteristic: " + temp + " " + press + " " + hum + " @" + ts);
		logMoiEnBdd(temp, press, hum, ts);
		
		//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
		mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
		mBlueActivity.updateText(String.valueOf(ts));
		}
	
	private void logMoiEnBdd(double temp, double press, double hum, long ts) {
		//sqlite3 /data/data/com.example.android.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), TEMP, PRES, HUM from envdata;"
		
		maBDD = new BaseDeDonnees(mContext);
		bdd = maBDD.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ALRMTIME", ts);
		values.put("TEMP", temp);
		values.put("PRES", press);
		values.put("HUM", hum);
		bdd.insert("envdata", null, values);
	}
	
	


	      

}

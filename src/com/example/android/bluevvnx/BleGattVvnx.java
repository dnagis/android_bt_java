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

import java.util.UUID;

import android.view.View;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

public class BleGattVvnx  {
	


	private Context mContext;
	private BlueActivity mBlueActivity;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothAdapter mBluetoothAdapter = null;	
	private BluetoothGattCharacteristic mCharacteristic = null;	
	private final String TAG = "BlueVvnx";
	
	
	/**	Correspondance avec l'esp32: 
	 * 	UUID du service: 
	 * 	gatttool -b 30:AE:A4:04:C3:5A --primary 
	*	attr handle: 0x0028, end grp handle: 0x002b uuid: 000000ff-0000-1000-8000-00805f9b34fb 
	* 		--> ce qui correspond dans le code esp32 à GATTS_SERVICE_UUID_TEST_A   0x00FF
	*	(il y en a un deuxième: attr handle = 0x002c, end grp handle = 0xffff uuid: 000000ee-0000-1000-8000-00805f9b34fb)
	* 	
	* 	au bootup de l'esp32 tu vois:
	* 	I (1019) GATTS_DEMO: SERVICE_START_EVT, status 0, service_handle 40
	* 	printf %x 40 --> 28 --> 0x0028 --> explique ce que tu as vu avec gatttool --primary
	*  
	**/
    private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
    
    
    
    /**
     * Characteristics: chaque service a des char. Pour comprendre comment esp32 les voit: 
     * 
     * https://github.com/espressif/esp-idf/blob/master/examples/bluetooth/bluedroid/ble/gatt_server/tutorial/Gatt_Server_Example_Walkthrough.md
     * surtout la section "Creating Services"
     * 
     * Chaque service a 4 handles, la première c'est celle du service lui même (0x0028 pour le premier service ci dessus), et celle qui nous interesse c'est la troisième
     * donc en hexa si 0x0028 est la première 0x002a est la characteristique que l'on veut lire. Quelle est son UUID?
     * 
     * gatttool -b 30:AE:A4:04:C3:5A --characteristics
     * 
     * handle = 0x0029, char properties = 0x1a, char value handle = 0x002a, uuid = 0000ff01-0000-1000-8000-00805f9b34fb
     * --> la char value handle 0x002a est celle que l'on cherche, d'où l'UUID ci dessous
     * 
     * correspond aussi au define côté esp32: GATTS_CHAR_UUID_TEST_A 0xFF01
     * 
     * au bootup de l'esp32 tu vois:
     * I (1019) GATTS_DEMO: ADD_CHAR_EVT, status 0,  attr_handle 42, service_handle 40
     * printf %x 42 --> 2a --> 0x002a --> explique ce que tu as vu avec gatttool --characteristics
     * 
     * 
     * 
	 * ancienne note: 
	 * si je comprends bien quand tu vois ça avec gatttool faut que tu send la notif côté esp32 (esp_ble_gatts_send_indicate) avec attr_handle (arg 3) -> 0x002a**/
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
		
		
		mBlueActivity = (BlueActivity) mContext; //pour pouvoir accéder à ses fields
		Log.d(TAG, "on crée un device avec adresse:" + mBlueActivity.BDADDR);
		
		BluetoothDevice monEsp = mBluetoothAdapter.getRemoteDevice(mBlueActivity.BDADDR);   
		
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
			//Log.i(TAG, "onCharacteristicRead callback.");
			byte[] data = characteristic.getValue();
			Log.i(TAG, "onCharacteristicRead callback -> char data: " + data[0] + " " + data[1] + " " + data[2]); //donne pour data[0]: -86 et printf %x -86 --> ffffffffffffffaa or la value côté esp32 est 0xaa 
			}
	
	
	//réception des notifications: 
	//côté serveur esp32: esp_ble_gatts_send_indicate(0x03, 0, gl_profile_tab[PROFILE_A_APP_ID].char_handle, sizeof(notify_data), notify_data, false);
	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			Log.i(TAG, "Rx notif: onCharacteristicChanged");
			byte[] data = characteristic.getValue();
			parseBMX280(data);	
			//parseGPIO(data);
			}	
	};
	  
	//anémo (esp32: gatts_gpio) encodage dans 2 bytes
	private void parseGPIO(byte[] data) {
			int valeur = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
			Log.i(TAG, "parseGPIO data: "+valeur);			
			//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
			//mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
			mBlueActivity.updateText(String.valueOf(valeur));
			long ts = System.currentTimeMillis()/1000;
			logCountEnBdd(ts, valeur);
	 }
	 
	 private void logCountEnBdd(long ts, int count) {
		//sqlite3 /data/data/com.example.android.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), COUNT from envdata;"
		
		maBDD = new BaseDeDonnees(mContext);
		bdd = maBDD.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ALRMTIME", ts);
		values.put("COUNT", count);
		bdd.insert("envdata", null, values);
	}
	 
	

	private void parseBMX280(byte[] data) {
		long ts = System.currentTimeMillis()/1000;
		//voir esp32_bmx280_gatts pour l'encodage des valeurs dans un array de bytes
		double temp = (double)(data[0]+(data[1]/100.0));
        if (data[2]==0) temp=-temp;
        double press = (double)(data[3]+872+(data[4]/100.0));
        double hum = (double)(data[5]+(data[6]/100.0));		
		Log.i(TAG, "recup data de la characteristic: " + temp + " " + press + " " + hum + " @" + ts);
		logBMX280EnBdd(temp, press, hum, ts);
		
		//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
		//mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
		//mBlueActivity.updateText(String.valueOf(ts));
		}
	
	private void logBMX280EnBdd(double temp, double press, double hum, long ts) {
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
	
	//Read char, l'équivalent de gatttool -b <bdaddr> --char-read -a 0x002a
	public void lireCharacteristic() {
		Log.i(TAG, "lireCharacteristic dans BleGattVvnx");		
		mBluetoothGatt.readCharacteristic(mCharacteristic); //mCharacteristic est construite dans la BluetoothGattCallback onServicesDiscovered()
	}
	
	//Write char, l'équivalent de gatttool -b <bdaddr> --char-write-req -a 0x002e -n 0203ffabef
	public void ecrireCharacteristic() {
		Log.i(TAG, "ecrireCharacteristic dans BleGattVvnx");	
		//mCharacteristic est construite dans la BluetoothGattCallback onServicesDiscovered(), c'est seulement sa value que je veux modifier
		mCharacteristic.setValue("43.458900,4.549026");	//ou "hello" of course...		
		mBluetoothGatt.writeCharacteristic(mCharacteristic); 
	}
	


	      

}

package com.example.android.bluevvnx;

import android.app.Service;
import android.util.Log;
import android.os.IBinder;
import android.os.Handler;
import android.content.Intent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanRecord;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

//pb de multiples instances avec incrémentation du nombre de onCharacteristicChanged
//https://stackoverflow.com/questions/33274009/how-to-prevent-bluetoothgattcallback-from-being-executed-multiple-times-at-a-tim



public class BlueVvnx extends Service {
	
	private static final String TAG = "BlueVvnx";
	

    private BluetoothAdapter mBluetoothAdapter = null;    
	private BluetoothLeScanner mBluetoothLeScanner = null;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothGattCharacteristic mCharacteristic = null;
	
    
    private static final long TIMEOUT = 30000;
    
    //uuid du service: gatttool --> [30:AE:A4:04:C3:5A][LE]> primary
    private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
    //[30:AE:A4:04:C3:5A][LE]> characteristics
	private static final UUID CHARACTERISTIC_PRFA_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
 
    @Override
    public void onCreate() {
		Log.d(TAG, "onCreate");	
		
		// Get local Bluetooth adapter
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //serait l'ancienne version selon BluetoothAdapter.java
        
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "fail à la récup de l'adapter");
            return;
        }
        
        //scan: chronologiquement: 1ère fonction implémentée dans ce projet        
        /*mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();        
        if (mBluetoothLeScanner == null) {
            Log.d(TAG, "fail à la récup du LeScanner");
            return;
        } 		
		scanLeDevice();*/
        
         
        //Gatt client 
        BluetoothDevice monEsp = mBluetoothAdapter.getRemoteDevice("30:AE:A4:04:C3:5A");
        
        //BluetoothDevice monEsp = mBluetoothAdapter.getRemoteDevice("30:AE:A4:45:C5:8E");
        

        
        
        mBluetoothGatt = monEsp.connectGatt(this, false, gattCallback);
        
        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			Log.d(TAG, "disconnectGatt");
			mBluetoothGatt.disconnect();
			stopSelf();
			}
		}, TIMEOUT); //sinon s'arrête jamais. permet auto reconnect ??				

    }
    
    
    //Les 3 fonctions pour extends Service
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "OnStartCommand");
		return START_NOT_STICKY;
	}

    @Override
    public void onDestroy() {		
		Log.d(TAG, "OnDestroy");
		stopSelf();		
	 }
	 
	@Override
	public IBinder onBind(Intent intent) {
      // We don't provide binding, so return null
      return null;
	}
	


	/**GATT: ça marche que par cb, comme dans l'esp32**/
	
	
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                mBluetoothGatt.close(); //si je mets pas ça  j'ai n+1 onCharacteristicChanged() à chaque passage (nouvelle instance BluetoothGattCallback?)
            }
        }
        
        @Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			
			if (status != BluetoothGatt.GATT_SUCCESS) {
					return;
				}	
			
			// Get the characteristic
			//BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_PRFA_UUID);	
			    
			mCharacteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_PRFA_UUID);
			gatt.readCharacteristic(mCharacteristic);
			
			//enable les notifs, indispensable sinon marche pas...
			gatt.setCharacteristicNotification(mCharacteristic, true);
        }

        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				Log.i(TAG, "onCharacteristicRead callback.");
				byte[] data = characteristic.getValue();
				maFonctionParseData(data);				
        }
        
        
        //réception des notifications: 
        //côté serveur esp32: esp_ble_gatts_send_indicate(0x03, 0, gl_profile_tab[PROFILE_A_APP_ID].char_handle, sizeof(notify_data), notify_data, false);
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				Log.i(TAG, "onCharacteristicChanged callback.");
				byte[] data = characteristic.getValue();
				maFonctionParseData(data);				
        }	
	};
	
	
	private void maFonctionParseData(byte[] data) {
		//voir esp32_bmx280_gatts
		double temp = (double)(data[0]+(data[1]/100.0));
        if (data[2]==0) temp=-temp;
        double press = (double)(data[3]+872+(data[4]/100.0));
        double hum = (double)(data[5]+(data[6]/100.0));		
		Log.i(TAG, "recup data de la characteristic: " + temp + " " + press + " " + hum);
	}


	/**Partie scan**/
	
	
	private void scanLeDevice() {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			Log.d(TAG, "stopLeScan");
			mBluetoothLeScanner.stopScan(mScanCallback);
			stopSelf();
			}
		}, TIMEOUT);
		
		
		//ScanFilter.Builder fbuilder = new ScanFilter.Builder().setDeviceAddress("30:AE:A4:04:C3:5A");	
	
		
		ScanFilter.Builder fbuilder = new ScanFilter.Builder();

		//fbuilder.setDeviceAddress("30:AE:A4:45:C8:86");
		//fbuilder.setDeviceAddress("30:AE:A4:04:C3:5A");// il ne prend que le dernier...
			
		ScanFilter filter = fbuilder.build();		
		final List<ScanFilter> filters = Collections.singletonList(filter);
		
		
		ScanSettings.Builder sbuilder = new ScanSettings.Builder()
			.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
			.setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT);
		ScanSettings settings = sbuilder.build();
		
		Log.d(TAG, "startScan");
		mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        }


	private ScanCallback mScanCallback = new ScanCallback() {
	    @Override
	    public void onScanResult(int callbackType, ScanResult result) {
			String addr_result = result.getDevice().getAddress();
			
			//filtre perso. 
			String addrfilter1 = new String("30:AE:A4:45:C8:86");
			String addrfilter2 = new String("30:AE:A4:04:C3:5A");
			if (!addr_result.equals(addrfilter1) && !addr_result.equals(addrfilter2)) return;

			
			ScanRecord scanRecord = result.getScanRecord();
			byte[] scan_data = scanRecord.getBytes();
			int temp_intpart = scan_data[5];
			float temp_decpart = scan_data[6];
			float temp = temp_intpart + (temp_decpart/100);
			if (scan_data[4] == 0) temp = -temp;
	        Log.d(TAG, "onScanResult addr=" + addr_result + " temp=" + temp); 
	   }
	   
	   	@Override
	    public void onScanFailed(int errorCode) {
	        Log.d(TAG, "onScanFailed");
	   }
	   
	   	@Override
	    public void onBatchScanResults(List<ScanResult> results) {
	        Log.d(TAG, "onBatchScanResults");
	   }
	};


}


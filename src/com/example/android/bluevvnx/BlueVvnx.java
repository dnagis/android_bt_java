/**
 *
 * adb uninstall com.example.android.bluevvnx
 * 
 * 
 * adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk
 * ou
 * adb install out/target/product/generic_arm64/system/app/BlueVvnx/BlueVvnx.apk
 * 
 * Lancement du service en shell (nom du service: celui déclaré dans le manifest -component name-) 
 * 
 * #indispensable, survit au reboot (tant que tu réinstalles pas l'appli), sinon app is in background uid null
 * dumpsys deviceidle whitelist +com.example.android.bluevvnx
 * 
 * am start-service com.example.android.bluevvnx/.BlueVvnx  
 * am stop-service com.example.android.bluevvnx/.BlueVvnx 
 * 
 * si problème de permisssions:
 * 
 * 	pm grant com.example.android.bluevvnx android.permission.ACCESS_COARSE_LOCATION
 *  pm grant com.example.android.bluevvnx android.permission.ACCESS_FINE_LOCATION
 *  
 * 
 * logcat -s StartVvnx
 * 
 * 
 * Lancement avec un intent explicite, syntaxe:
 * am start-service -a android.intent.action.DIAL com.example.android.bluevvnx/.BlueVvnx
 *
 * 
 * 
 */

package com.example.android.bluevvnx;

import android.app.Service;
import android.util.Log;
import android.os.IBinder;
import android.os.Handler;
import android.content.Intent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
//gestion des filtres le 18 01
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import java.util.Collections;
import java.util.List;



public class BlueVvnx extends Service {
	
	private static final String TAG = "BlueVvnx";
	

    private BluetoothAdapter mBluetoothAdapter = null;    
	private BluetoothLeScanner mBluetoothLeScanner = null;
	
    
    private static final long SCAN_PERIOD = 32000;
 
    @Override
    public void onCreate() {
		Log.d(TAG, "onCreate");	
		
		// Get local Bluetooth adapter
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //serait l'ancienne version selon BluetoothAdapter.java
        
        final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        
        mBluetoothAdapter = bluetoothManager.getAdapter();


        if (mBluetoothAdapter == null) {
            Log.d(TAG, "fail à la récup de l'adapter");
            return;
        }
        
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        
        if (mBluetoothLeScanner == null) {
            Log.d(TAG, "fail à la récup du LeScanner");
            return;
        }        
		
		scanLeDevice();
			
				

    }
    
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
	





	
	
	
	private void scanLeDevice() {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			Log.d(TAG, "stopLeScan");
			mBluetoothLeScanner.stopScan(mScanCallback);
			}
		}, SCAN_PERIOD);
		
		
		ScanFilter.Builder fbuilder = new ScanFilter.Builder();
		ScanFilter filter = fbuilder.build();
		final List<ScanFilter> filters = Collections.singletonList(filter);
		
		ScanSettings.Builder sbuilder = new ScanSettings.Builder();
		ScanSettings settings = sbuilder.build();
		
		Log.d(TAG, "startScan");
		mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        }
        
        








	private ScanCallback mScanCallback = new ScanCallback() {
	    @Override
	    public void onScanResult(int callbackType, ScanResult result) {
	        Log.d(TAG, "onScanResult");
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


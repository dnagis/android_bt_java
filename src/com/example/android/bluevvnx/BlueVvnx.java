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



public class BlueVvnx extends Service {
	
	private static final String TAG = "BlueVvnx";
	
	// Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    
    //private Handler mHandler;
    
    private static final long SCAN_PERIOD = 32000;
 
    @Override
    public void onCreate() {
		Log.d(TAG, "onCreate");	
		
		// Get local Bluetooth adapter
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //serait l'ancienne version selon BluetoothAdapter.java
        
        final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");
            return;
        }
		
		scanLeDevice();
			
				
        //stopSelf(); //j'avais mis ça juste parce que le dev guide disait qu'il fallait faire le ménage soi-même
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "OnStartCommand");
		//stopSelf(); //j'avais mis ça juste parce que le dev guide disait qu'il fallait faire le ménage soi-même
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
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
            }, SCAN_PERIOD);

			Log.d(TAG, "startLeScan");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        
        



	private BluetoothAdapter.LeScanCallback mLeScanCallback =
	        new BluetoothAdapter.LeScanCallback() {
	    @Override
	    public void onLeScan(BluetoothDevice device, int rssi,
	            byte[] scanRecord) {
	        Log.d(TAG, "retour de scan addr=" + device.getAddress());
	   }
	};


}


package vvnx.bluevvnx;




import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Handler;

import android.content.ContentResolver;
import android.content.ContentValues;

import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.content.Intent;


	/**
	 * 
	 * 
	 * 
	 * Détection de sortie mode avion -> relance du service
	 * 
	 * 
	 * 
	 * **/
	
// Receiver pour détecter sortie de mode avion
public class MonReceiver extends BroadcastReceiver {
	
	private final String TAG = "BlueVvnx";
	
	@Override
	public void onReceive(Context context, Intent intent) {            
		boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
		Log.i(TAG, "onReceive d'un broadcast dans MonReceiver --> state = " + isAirplaneModeOn);

		//si je null pas la gatt j'ai toujours BluetoothGatt: android.os.DeadObjectException à la tentative de .connect() dessus
		
		 if (isAirplaneModeOn) {
			Log.i(TAG, "receiver -> airplanemodeon");
			//closeGatt() ;
			} else { 
				Log.i(TAG, "receiver -> airplanemodeoff");
					//il faut attendre que le bluetooth se rallume, si tu tentes aussitôt marche pas
		            final Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
					    @Override
					    public void run() { 
							Intent i = new Intent(context, GattService.class);
							context.startService(i); 
							}			
						}, 2000);
					}
			 
		}

	// constructor
	public MonReceiver(){
			Log.i(TAG, "receiver registered");
	}
}

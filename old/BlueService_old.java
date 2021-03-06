package vvnx.bluevvnx;

import android.app.Service;
import android.util.Log;
import android.os.IBinder;
import android.os.Handler;
import android.content.Intent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanRecord;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;

//foreground service
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;

//intents
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

//pb de multiples instances avec incrémentation du nombre de onCharacteristicChanged à chaque nouveau call (am start-service):
//https://stackoverflow.com/questions/33274009/how-to-prevent-bluetoothgattcallback-from-being-executed-multiple-times-at-a-tim



public class BlueService_old extends Service {
	
	private static final String TAG = "BlueVvnx";
	private static final String BDADDR = "30:AE:A4:04:C3:5A"; //Plaque de dev
	

    private BluetoothAdapter mBluetoothAdapter = null;    
	private BluetoothLeScanner mBluetoothLeScanner = null;
	
    
    private static final long TIMEOUT = 25000;
    
    //uuid du service: gatttool --> [30:AE:A4:04:C3:5A][LE]> primary
    private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
    //[30:AE:A4:04:C3:5A][LE]> characteristics
	private static final UUID CHARACTERISTIC_PRFA_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
	
	//sql
    private BaseDeDonnees maBDD;
    private SQLiteDatabase bdd;
    
    //foreground service
    Notification mNotification;
 
    @Override
    public void onCreate() {
		Log.d(TAG, "onCreate");	
    }
    

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "OnStartCommand");
		
		/**
		 * 
		 * 
		 * Foreground service -> pour pas qu'il s'arrête automatiquement
		 * voir tileview
		 * Fonctionne aussi sur un service démarré en shell
		 * 
		 * 
		 * 
		 * **/
		
		//https://developer.android.com/training/notify-user/channels
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String CHANNEL_ID = "UNE_CHAN_ID";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "channel_blue", importance);
        channel.setDescription("android_fait_chier_avec_sa_channel");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
		
		// Build the notification object.
        mNotification = new Notification.Builder(this, CHANNEL_ID)  //  The builder requires the context
                .setSmallIcon(R.drawable.icon)  // the status icon
                .setTicker("NotifText")  // the status text
                .setContentTitle("bluevvnx")  // the label of the entry
                .setContentText("Mec ton appli est en foreground service!")  // the contents of the entry
                .build();		
		
        startForeground(1001,mNotification);		
		
		return START_STICKY;
		//return START_NOT_STICKY;
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
	


	/**Partie scan
	 * Tu te demandes ce que ça fout là*... normal...
	 * En fait ça date du tout début, quand je lançais tout en shell (am start-service com.example.android.bluevvnx/.BlueService )
	 * 
	 * **/
	
	
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


package vvnx.bluevvnx;

import android.app.Service;
import android.util.Log;
import android.os.IBinder;
import android.os.Handler;
import android.content.Intent;

import android.content.Context;
import android.util.Log;

import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;

import java.util.UUID;



public class GattService extends Service  {

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothDevice mEspDevice = null;
	private BluetoothGatt mBluetoothGatt_1 = null;
	//private BluetoothGatt mBluetoothGatt_2 = null;		
	private BluetoothGattCharacteristic mCharacteristic = null;	
	private final String TAG = "BlueVvnx";
	//private static final String BDADDR_1 = "30:AE:A4:05:0C:BE"; //Plaque de dev 	
	//private static final String BDADDR_1 = "30:AE:A4:04:C3:5A"; 
	private static final String BDADDR_1 = "30:AE:A4:47:55:B2"; //PCB Thor
	
	private boolean mFlagGattDropAsked = false;
	
	
	private Notification mNotification;
	
	private UtilsVvnx mUtilsVvnx = new UtilsVvnx();
	private Context mContext;
	

	public static final int MSG_REG_CLIENT = 200;//enregistrer le client dans le service
	public static final int MSG_STOP = 400;
	public static final int MSG_BT_CONNECTED = 500;
	public static final int MSG_BT_DISCONNECTED = 600;
	
	
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
	
	
	
	
	
	/**
	 * système IPC Messenger / Handler basé sur le Binder
	 */
	  
	private Messenger mClient; // l'activité

	private class IncomingHandler extends Handler {
        

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
				//REG_CLIENT: juste un trick pour avoir un messenger vers l'activité (=client)
				case MSG_REG_CLIENT:
                    Log.d(TAG, "Service: handleMessage() -> REG_CLIENT");
                    mClient = msg.replyTo;
                    break;
				case MSG_STOP:
                    Log.d(TAG, "Service: handleMessage() -> STOP");
                    dropGatt();
                    break;                    
                default:
                    super.handleMessage(msg);
            }
        }
    }

	 
	
	final Messenger mMessenger = new Messenger(new IncomingHandler()); //le messenger local
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		mContext = this;
		//Foreground
        /*int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String CHANNEL_ID = "MA_CHAN_ID";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "ma_channel", importance);
        channel.setSound(null, null);
        channel.setDescription("android_fait_chier_avec_sa_channel");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
		
        mNotification = new Notification.Builder(this, CHANNEL_ID)  //  The builder requires the context
                .setSmallIcon(R.drawable.icon)  // the status icon
                .setTicker("NotifText")  // the status text
                .setContentTitle("BlueVvnx")  // the label of the entry
                .setContentText("BlueVvnx")  // the contents of the entry
                .build();	
			
		startForeground(1, mNotification);*/
		
		//Bluetooth
		connectmGatt();
		
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
      return mMessenger.getBinder(); //envoyé vers onServiceConnected() dans l'activité
	}
	
	 
	void connectmGatt(){	
		
		final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);	
		mBluetoothAdapter = bluetoothManager.getAdapter();	
		
		if (mBluetoothAdapter == null) {
			Log.d(TAG, "fail à la récup de l'adapter");
			return;
		}		

		Log.d(TAG, "on crée un device avec adresse:" + BDADDR_1);
		
		mEspDevice = mBluetoothAdapter.getRemoteDevice(BDADDR_1);   
		
		if (mBluetoothGatt_1 == null) {
			Log.d(TAG, "pas encore de mBluetoothGatt_1: on la crée");
			//Attention: même si l'esp n'est pas disponible et que l'adapter ne s'y est jamais connecté, dès son apparition il se connectera
			mBluetoothGatt_1 = mEspDevice.connectGatt(this, true, gattCallback);
		} else {
			Log.d(TAG, "mBluetoothGatt_1 existe: on lance connect() dessus");
			mBluetoothGatt_1.connect();
		}
		
		/** Deuxième device
		Log.d(TAG, "on crée un device avec adresse:" + BDADDR_2);		
		BluetoothDevice monEsp_2 = mBluetoothAdapter.getRemoteDevice(BDADDR_2);  		
		if (mBluetoothGatt_2 == null) {	mBluetoothGatt_2 = monEsp_2.connectGatt(this, true, gattCallback);
		} else {	mBluetoothGatt_2.connect();	}**/

	}
	

	public void dropGatt() {
			Log.d(TAG, "dropGatt...");
			
			//on vérifie connectionState du device
			final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
			int connState = bluetoothManager.getConnectionState(mEspDevice, BluetoothProfile.GATT);
			
			Log.d(TAG, "connState du device: "+connState); //https://developer.android.com/reference/android/bluetooth/BluetoothProfile#STATE_CONNECTED
			if (connState == BluetoothProfile.STATE_CONNECTED) {
				mFlagGattDropAsked = true; //permet à la callback de savoir que l'on doit passer par closeGatt()
				mBluetoothGatt_1.disconnect(); 				
			} else {
				closeGatt();
			}
 
	}
	
	public void closeGatt() {
		Log.d(TAG, "on lance close() sur la gatt");
		if (mBluetoothGatt_1 != null) {
			mBluetoothGatt_1.close(); 
			//close() désenregistre la gatt de l'adapter. Si on veut pouvoir se reconnecter ultérieurement il faut nuller la gatt pour repasser par connectGatt()
			//https://stackoverflow.com/questions/23110295/difference-between-close-and-disconnect
			mBluetoothGatt_1 = null;		
			}
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

		if (newState == BluetoothProfile.STATE_CONNECTED) {
			Log.i(TAG, "Connected to GATT server addr=" + gatt.getDevice().getAddress());
			//on envoie le message à l'activité pour refresh affichage
			Message msg = Message.obtain(null, MSG_BT_CONNECTED);
			Bundle bundle = new Bundle();
			bundle.putString("bdaddr", gatt.getDevice().getAddress());
			msg.setData(bundle);
			try {
                mClient.send(msg);
            } catch (RemoteException e) {
                 e.printStackTrace();
            }					

			gatt.discoverServices();
			
		} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
			Log.i(TAG, "Disconnected from GATT server.");
			//on est là parce que disconnect() a été lancé (voir plus haut), on close la gatt
			if (mFlagGattDropAsked) {
				mFlagGattDropAsked = false;
				closeGatt();
			}
			
			//on envoie le message à l'activité pour refresh affichage
			Message msg = Message.obtain(null, MSG_BT_DISCONNECTED);
			try {
                mClient.send(msg);
            } catch (RemoteException e) {
                 e.printStackTrace();
            }
		}
        //si je mets pas ça  j'ai n+1 onCharacteristicChanged() à chaque passage (nouvelle instance BluetoothGattCallback?)
		//***MAIS***
		//close() la connexion du coup j'ai pas d'auto-reconnect...
		//mBluetoothGatt.close(); 
	}
	
	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			//Log.i(TAG, "onServicesDiscovered callback.");
			mCharacteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_PRFA_UUID);
			//gatt.setCharacteristicNotification(mCharacteristic, true);
			gatt.readCharacteristic(mCharacteristic); 
	}

	
	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			//Log.i(TAG, "onCharacteristicRead callback.");
			byte[] data = characteristic.getValue();
			//Log.i(TAG, "onCharacteristicRead callback -> char data: " + data[0] + " " + data[1] + " " + data[2]); //donne pour data[0]: -86 et printf %x -86 --> ffffffffffffffaa or la value côté esp32 est 0xaa 
			//mUtilsVvnx.parseGPIO(mContext, data);
			mUtilsVvnx.parseBMX280(mContext, characteristic);
			}
	
	
	//réception des notifications: 
	//côté serveur esp32: esp_ble_gatts_send_indicate(0x03, 0, gl_profile_tab[PROFILE_A_APP_ID].char_handle, sizeof(notify_data), notify_data, false);
	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			Log.i(TAG, "Rx notif: onCharacteristicChanged");
			byte[] data = characteristic.getValue();
			//parseBMX280(data);	//voir UtilsVvnx.java désormais
			//parseGPIO(data);
			}	
	};
	  
		
	//Read char, l'équivalent de gatttool -b <bdaddr> --char-read -a 0x002a
	public void lireCharacteristic() {
		Log.i(TAG, "lireCharacteristic dans BleGattVvnx");		
		mBluetoothGatt_1.readCharacteristic(mCharacteristic); //mCharacteristic est construite dans la BluetoothGattCallback onServicesDiscovered()
	}
	
	//Write char, l'équivalent de gatttool -b <bdaddr> --char-write-req -a 0x002e -n 0203ffabef
	public void ecrireCharacteristic() {
		Log.i(TAG, "ecrireCharacteristic dans BleGattVvnx");	
		//mCharacteristic est construite dans la BluetoothGattCallback onServicesDiscovered(), c'est seulement sa value que je veux modifier
		//mCharacteristic.setValue("43.458900,4.549026");	//ou "hello" of course...		
		//mBluetoothGatt.writeCharacteristic(mCharacteristic); 
	}
	


	      

}

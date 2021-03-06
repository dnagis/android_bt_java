package vvnx.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import java.text.SimpleDateFormat;
import java.util.Date; 

import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.RemoteException;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class BlueActivity extends Activity {
	
	private static final String TAG = "BlueVvnx";
	boolean mSceBound = false;
	Messenger mService = null;

	private Drawable default_btn;
	
	TextView textview1, textview2, textview3;

    /**
     * manifest attribut d'activity pour prevent passage ici quand rotation:
     * android:configChanges="orientation|screenLayout|screenSize"
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        
        textview1 = findViewById(R.id.text1);	
        textview2 = findViewById(R.id.text2);
        textview3 = findViewById(R.id.text3);
        
        //récup le background par default du bouton pour le remettre
        Button button1 = findViewById(R.id.button_1);
        default_btn = button1.getBackground();   
        
        
    }
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
		Intent i = new Intent(this, GattService.class);
        startService(i); 
        bindService(i, connection, Context.BIND_AUTO_CREATE); //Déclenche onBind() dans le service

	}
	
	public void ActionPressBouton_2(View v) {
		Log.d(TAG, "press bouton 2");
		Message msg = Message.obtain(null, GattService.MSG_STOP);
        try {
               mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

	}

	
	public void ActionPressBouton_3(View v) {
		
		/*Accéder à la base de données en utilisant des méthodes de la classe BaseDeDonnees*/
		
		BaseDeDonnees maBDD = new BaseDeDonnees(this);		
		int tailleArrayTimes = maBDD.fetchAllTimes().size();
		Log.d(TAG, "press bouton 3 taille array = " + tailleArrayTimes + "et last = " + maBDD.fetchAllTimes().get(tailleArrayTimes - 1));
		
		
		/*Accéder à la base de données en faisant la requête ici		
		String countQuery = "SELECT  * FROM envdata";
		BaseDeDonnees maBDD = new BaseDeDonnees(this);
		SQLiteDatabase bdd = maBDD.getReadableDatabase();
		Cursor cursor = bdd.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();*/
		
		
		
		/**Tentative d'export de la bdd vers du storage où je peux récupérer sur un tel de production
		select datetime(alrmtime, 'unixepoch', 'localtime'), count from envdata;
				
		File currentDBFile = new File("/data/data/vvnx.bluevvnx/databases/data.db");
		if(currentDBFile.exists()) Log.d(TAG, "yes le fichier existe");		
		
		Sur le mido lineage: File backupDBFile = new File("/storage/emulated/0/bluevvnx/data.db");
		 * Il faut créer le dir bluevvnx, et la permission doit être donnée dans les paramètres:
		 * sans l'autorisation write storage donnée dans les paramètres j'ai en logcat:
		 BlueVvnx: erreur export bdd = java.io.FileNotFoundException: /storage/emulated/0/bluevvnx/data.db (Permission denied)
		
		File backupDBFile = new File("/storage/emulated/0/bluevvnx/data.db"); 
		
		Moto Z production. Idem faut créer le dir bluevvnx (possible en shell), et donner la permission write storage dans les paramètres
		Ne marche pas: je n'ai pas une copie des dernières valeurs de la bdd
		File backupDBFile = new File("/sdcard/bluevvnx/data.db"); 
				
		try {
		FileChannel src = new FileInputStream(currentDBFile).getChannel();
		FileChannel dst = new FileOutputStream(backupDBFile).getChannel();
		dst.transferFrom(src, 0, src.size());
		Log.d(TAG, "bdd exportdb size="+src.size());		
		src.close();
		dst.close();
		} catch (Exception e) {
				Log.d(TAG, "erreur export bdd = "+e.toString());
            }
		**/
		
	}
	
	public void updateConnText(String bdaddr) {
        Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");	//dd MMM HH:mm:ss
		textview1.setText("Last connect: "+ sdf.format(d));
		textview2.setText(bdaddr);
    }
    
    public void updateNotifText(int data) {
        Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		textview3.setText("Last notif:     "+ sdf.format(d) + "  " + data);
    }
    
    public void btn1_to_blue() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundColor(Color.BLUE);
	}
	
	public void btn1_to_def() {
	    Button button1 = findViewById(R.id.button_1);
	    button1.setBackgroundDrawable(default_btn);
	}
	
	/**
	 * système IPC Messenger / Handler basé sur le Binder
	 */ 
	 
	private Handler mIncomingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GattService.MSG_BT_CONNECTED:
					String bdaddr = msg.getData().getString("bdaddr");
                    Log.d(TAG, "BlueActivity: handler -> MSG_BT_CONNECTED: " + bdaddr);
                    btn1_to_blue();
                    updateConnText(bdaddr);                    
                    break;
                case GattService.MSG_BT_DISCONNECTED:
                    Log.d(TAG, "BlueActivity: handler -> MSG_BT_DISCONNECTED");
                    btn1_to_def();
                    break;
                case GattService.MSG_BT_NOTIF:
                    Log.d(TAG, "BlueActivity: handler -> MSG_BT_NOTIF");
                    updateNotifText(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };		
	
	private final Messenger mMessenger = new Messenger(mIncomingHandler);
	
	/** callbacks for service binding */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "onServiceConnected()");
            mService = new Messenger(service); 
            mSceBound = true;
            //Enregistrer un handler ici pour que le service puisse l'appeler, l'envoyer au service
            Message msg = Message.obtain(null, GattService.MSG_REG_CLIENT);
            msg.replyTo = mMessenger; //pour dire au service où envoyer ses messages
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to register client to service.");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "onServiceDisconnected()");
			mService = null;
			mSceBound = true;
        }
    };
	
}


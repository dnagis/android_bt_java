package vvnx.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

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

public class BlueActivity extends Activity {
	
	private static final String TAG = "BlueVvnx";
	boolean mSceBound = false;
	Messenger mService = null;

	private Drawable default_btn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        
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

	}

	
	public void ActionPressBouton_3(View v) {
		Log.d(TAG, "press bouton 3");

	}
	
	public void updateText(String myString) {
		Log.d(TAG, "updateText dans BlueActivity");
		TextView textview1 = findViewById(R.id.text1);	
        textview1.setText(myString);
        textview1.invalidate();
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
                    Log.d(TAG, "Activity: handler -> MSG_BT_CONNECTED");
                    break;
                case GattService.MSG_BT_DISCONNECTED:
                    Log.d(TAG, "Activity: handler -> MSG_BT_DISCONNECTED");
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


package vvnx.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class BlueActivity extends Activity {
	
	private static final String TAG = "BlueVvnx";

	private Drawable default_btn;
	public String BDADDR;

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

	}
	
	public void ActionPressBouton_3(View v) {
		Log.d(TAG, "press bouton 3");

	}

	
	public void ActionPressBouton_5(View v) {
		Log.d(TAG, "press bouton 5");

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
	

	
}


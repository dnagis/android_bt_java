package com.example.android.bluevvnx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.util.Log;


public class BlueActivity extends Activity {
	private static final String TAG = "BlueVvnx";
		

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity.  You can find it
        // in res/layout/hello_activity.xml
        View view = getLayoutInflater().inflate(R.layout.bluevvnxmain, null);
        setContentView(view);
        
        final Button button = findViewById(R.id.button_id);


    }
    
    public void ActionPressBouton(View v) {
		Log.d(TAG, "press bouton hello world");
	}
}


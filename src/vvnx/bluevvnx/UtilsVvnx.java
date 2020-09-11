package vvnx.bluevvnx;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import android.bluetooth.BluetoothGattCharacteristic;

	/**
	 * 
	* juste une zone de stockage de fonctions "utils" pour pas alourdir le code de bluevvnx
	 * 
	 * 
	 * 
	 * **/


public class UtilsVvnx  {
	
	private final String TAG = "BlueVvnx";
	
	//sql
    private BaseDeDonnees maBDD;
    private SQLiteDatabase bdd;
	
	//anémo (esp32)
	public void parseAnemo(Context context, byte[] data) {
			
			//int valeur = (data[0] & 0xFF) << 8 | (data[1] & 0xFF); //avant je faisais un encodage dans 2 bytes
			Log.i(TAG, "parseGPIO data: " + data[0]);			
			
			//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
			//mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
			//mBlueActivity.updateText(String.valueOf(valeur));
			
			//long ts = System.currentTimeMillis()/1000;
			//logCountEnBdd(context, ts, valeur);
	 }
	 
	 private void logCountEnBdd(Context context, long ts, int count) {
		//sqlite3 /data/data/vvnx.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), COUNT from envdata;"
		
		maBDD = new BaseDeDonnees(context);
		bdd = maBDD.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ALRMTIME", ts);
		values.put("COUNT", count);
		bdd.insert("envdata", null, values);
	}
	


	public void parseBMX280(Context context, BluetoothGattCharacteristic rxData) {
		long ts = System.currentTimeMillis()/1000;
		//voir esp32_bmx280_gatts pour l'encodage des valeurs dans un array de bytes	
		//avant de faisait avec un byte[] mais pour la pression ça passe pas: les bytes en java: aussi grands: il croient que c'est un two's complement donc donne une valeur negative	
        double temp = (double)(rxData.getIntValue(17,0) + (rxData.getIntValue(17,1)/100.0)); //17 = FORMAT_UINT8
        double press = (double)(rxData.getIntValue(17,3)+872+(rxData.getIntValue(17,4)/100.0));
        double hum = (double)(rxData.getIntValue(17,5)+(rxData.getIntValue(17,6)/100.0));
		//Log.i(TAG, "recup data de la characteristic: " + temp + " " + press + " " + hum + " @" + ts);
		logBMX280EnBdd(context, temp, press, hum, ts);
		}
	
	private void logBMX280EnBdd(Context context, double temp, double press, double hum, long ts) {
		//sqlite3 /data/data/vvnx.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), TEMP, PRES, HUM from envdata;"
		
		maBDD = new BaseDeDonnees(context);
		bdd = maBDD.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ALRMTIME", ts);
		values.put("TEMP", temp);
		values.put("PRES", press);
		values.put("HUM", hum);
		bdd.insert("envdata", null, values);
	}
	
	
	
	
}

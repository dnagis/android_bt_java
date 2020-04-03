package vvnx.bluevvnx;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;

	/**
	 * 
	* juste une zone de stockage de fonctions "utils" pour pas alourdir le code de bluevvnx
	 * 
	 * 
	 * 
	 * **/


public class UtilsVvnx  {
	
	//sql
    private BaseDeDonnees maBDD;
    private SQLiteDatabase bdd;
	
	//anémo (esp32: gatts_gpio) encodage dans 2 bytes
	private void parseGPIO(Context context, byte[] data) {
			int valeur = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
			//Log.i(TAG, "parseGPIO data: "+valeur);			
			//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
			//mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
			//mBlueActivity.updateText(String.valueOf(valeur));
			long ts = System.currentTimeMillis()/1000;
			logCountEnBdd(context, ts, valeur);
	 }
	 
	 private void logCountEnBdd(Context context, long ts, int count) {
		//sqlite3 /data/data/com.example.android.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), COUNT from envdata;"
		
		maBDD = new BaseDeDonnees(context);
		bdd = maBDD.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ALRMTIME", ts);
		values.put("COUNT", count);
		bdd.insert("envdata", null, values);
	}
	
		/**
	 * 
	 * ça ne peut pas passer: les bytes en java peuvent pas contenir la valeur de la pression
	 * il faut faire comme alarmGatt, j'ai la flemme de changer ce soir. Mais ne t'étonnes pas si tu as
	 * des pressions à 700-800 en java: la valeur récupérée est négative (-144+872)
	 * 
	 * 
	 * 
	 * **/

	private void parseBMX280(Context context, byte[] data) {
		long ts = System.currentTimeMillis()/1000;
		//voir esp32_bmx280_gatts pour l'encodage des valeurs dans un array de bytes
		double temp = (double)(data[0]+(data[1]/100.0));
        if (data[2]==0) temp=-temp;
        double press = (double)(data[3]+872+(data[4]/100.0));
        double hum = (double)(data[5]+(data[6]/100.0));		
		//Log.i(TAG, "recup data de la characteristic: " + temp + " " + press + " " + hum + " @" + ts);
		logBMX280EnBdd(context, temp, press, hum, ts);
		
		//Seulement si c'est via UI (BlueActivity), sinon si lancé à partir du service en adb shell->plante
		//mBlueActivity = (BlueActivity) mContext; //pour pouvoir appeler ses methods
		//mBlueActivity.updateText(String.valueOf(ts));
		}
	
	private void logBMX280EnBdd(Context context, double temp, double press, double hum, long ts) {
		//sqlite3 /data/data/com.example.android.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), TEMP, PRES, HUM from envdata;"
		
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

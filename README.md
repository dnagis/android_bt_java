 # Bluetooth en java (BlueVvnx)
 
 2 Entrées possibles: via UI ou en shell via am start-service ***mais*** en 10/19 je veux faire des tests outdoor. Donc entrée = UI
 mais je veux aussi un foreground service. Donc attention je commente dans BlueService.onCreate() la partie qui lance bluetoothGATT, parce 
 que sinon: l'UI crée une instance bluetoothGATT, et ensuite le service pourrait aussi en créer une.
 
 
 Historique:
 Première fonction implémentée (chronologiquement): LeScan mBluetoothLeScanner.startScan(filters, settings, mScanCallback) --> mScanCallback 
 
 Deuxième fonction: gatt client: se connecte à l'esp32
	bluetooth/bluedroid/ble/gatt_server = point de départ, 
	https://github.com/dnagis/esp32_bmx280_gatts (le gros de mon travail)
	
 Intent BluetoothDevice.ACTION_BOND_STATE_CHANGED reçu (Receiver.java) -> permettait de réagir à des choses qui se passent quand on 
	manipule paramètres -> bluetooth -> association
 
 
  
 routine aosp:
	 make BlueVvnx 
	 adb uninstall com.example.android.bluevvnx 
	 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

 
 adb shell, mais quand uninstall réinstall juste après je n'ai pas besoin de le faire... chelou...
 dumpsys deviceidle whitelist +com.example.android.bluevvnx;\
 pm grant com.example.android.bluevvnx android.permission.ACCESS_FINE_LOCATION;\
 pm grant com.example.android.bluevvnx android.permission.ACCESS_COARSE_LOCATION 
 
 am start-service com.example.android.bluevvnx/.BlueService  
 
 ou avec un intent explicite, syntaxe:
 
 am start-service -a android.intent.action.DIAL com.example.android.bluevvnx/.BlueService
  
 logcat -s BlueVvnx
 
 ****attention!!****: pour arrêter c'est:
 am force-stop com.example.android.bluevvnx
	 et pas:
 am stop-service com.example.android.bluevvnx/.BlueService
 
 sqlite3 /data/data/com.example.android.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), TEMP, PRES, HUM from envdata;"
 

 Page très helpfull pour GATT:
 http://nilhcem.com/android-things/bluetooth-low-energy
 
 



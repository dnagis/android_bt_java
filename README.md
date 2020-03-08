 # Bluetooth en java (BlueVvnx)
 
 BDADDR:
	 nouvelle bdaddr: juste modifier res/values/strings.xml -> la première de la liste sera prise au démarrage dans BlueActivity à onItemSelected() même sans rien faire
	 Multiples bdaddr possibles via menu déroulant (spinner), mais je dois avoir un pb de recyclage que j'ai la flemme de gérer: du coup 
	 workaround quand tu veux passer de l'un à l'autre: fermer appli puis la relancer
	 
 routine aosp:
	 make BlueVvnx 
	 adb uninstall com.example.android.bluevvnx 
	 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

 
 adb shell, la première fois seulement! Inutile de se faire chier à le refaire chaque fois!
 dumpsys deviceidle whitelist +com.example.android.bluevvnx;\
 pm grant com.example.android.bluevvnx android.permission.ACCESS_FINE_LOCATION;\
 pm grant com.example.android.bluevvnx android.permission.ACCESS_COARSE_LOCATION 
 
 
 
 
 
 
 logcat -s BlueVvnx

 

 
 2 Entrées possibles: via UI ou en shell via am start-service com.example.android.bluevvnx/.BlueService  
 Atention si tu utilises le service: je commente dans BlueService.onCreate() la partie qui lance bluetoothGATT, parce 
 que sinon: l'UI crée une instance bluetoothGATT, et ensuite le service pourrait aussi en créer une.
 

 
 Historique:
 Première fonction implémentée (chronologiquement): LeScan mBluetoothLeScanner.startScan(filters, settings, mScanCallback) --> mScanCallback 
 
 Deuxième fonction: gatt client: se connecte à l'esp32
	bluetooth/bluedroid/ble/gatt_server = point de départ, pour la logique bluetooth voir: morphotox/bluetooth
	https://github.com/dnagis/esp32_bmx280_gatts (le gros de mon travail)
	

 
 
  

 
 ****attention!!****: pour arrêter c'est:
 am force-stop com.example.android.bluevvnx
	 et pas:
 am stop-service com.example.android.bluevvnx/.BlueService
 
 sqlite3 /data/data/com.example.android.bluevvnx/databases/data.db "select datetime(ALRMTIME, 'unixepoch','localtime'), TEMP, PRES, HUM from envdata;"
 

 Page très helpfull pour GATT:
 http://nilhcem.com/android-things/bluetooth-low-energy

 **Intents: lancement avec un intent explicite, syntaxe:
 
 am start-service -a android.intent.action.DIAL com.example.android.bluevvnx/.BlueService
 
  Intent BluetoothDevice.ACTION_BOND_STATE_CHANGED reçu (Receiver.java) -> permettait de réagir à des choses qui se passent quand on 
	manipule paramètres -> bluetooth -> association
 
 



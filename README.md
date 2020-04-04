# Bluetooth Gatt en java (BlueVvnx)
 
## BDADDR:
	 nouvelle bdaddr: juste modifier res/values/strings.xml -> la première de la liste sera prise au démarrage dans BlueActivity à onItemSelected() même sans rien faire
	 Multiples bdaddr possibles via menu déroulant (spinner), mais je dois avoir un pb de recyclage que j'ai la flemme de gérer: du coup 
	 workaround quand tu veux passer de l'un à l'autre: fermer appli puis la relancer
	 
## routine aosp:
	 make BlueVvnx 
	 adb uninstall vvnx.bluevvnx 
	 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

# repo / rsync
Avril 2020 pendant le travail pas de push tout le temps, enovi sur kimsufi c'est plus rapide:
rsync -azvhu /initrd/mnt/dev_save/android/lineageOS/sources/development/samples/BlueVvnx ks:/home/android
 
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
	
 "Cronability"
 Premier run: bouton qui connectGatt, et l'esp32 lançait régulièrement des esp_ble_gatts_send_indicate, qui arrivaient dans onCharacteristicChanged()
	problème de persistance au bout de qqes heures (deux ou trois je dirais). L'esp32 sendait des notifs toutes les 10 secondes. La batterie de l'esp32
	fonctionnait. L'UI de BlueVvnx était noire. Je propose donc:
 Deuxième système à faire:
	Alarm: Bouton -> repeating alarm, au bout de laquelle un service, qui connecte, et dans une callback récupère de la data (char read), puis disconnect.
	Voir si persistance. Combien de temps?
	
 
 
  

 
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
 
 



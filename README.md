# Bluetooth Gatt en java (BlueVvnx)
 
## Ergonomie
	 
### routine aosp:
	 make BlueVvnx 
	 adb uninstall vvnx.bluevvnx 
	 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk
	 
	 logcat -s BlueVvnx

### repo / rsync

Avril 2020 simplification de l'envoi remote:
rsync -azvhu /initrd/mnt/dev_save/android/lineageOS/sources/development/samples/BlueVvnx ks:/home/android
 



## LifeCycle / Physiologie de connexion gatt

-BluetoothDevice.connectGatt() -> arg2 à true = présence du device non indispensable: 
	L'adapter a l'adresse enregistrée, dès que le device se présentera il sera connecté (je n'ai pas testé s'il y avait un timeout, à mon avis il n'y en a pas).
	Donc pas nécessaire d'avoir le device allumé au moment de lancer le connectGatt().
-BluetoothGatt.close() -> désenregistre la gatt. si tu ne repasses pas par connectGatt() il n'y aura plus de reconnexion possible sur ce device.	
	solution: après le close() nuller la BluetoothGatt pour repasser par connectGatt()




## Reconnexion gatt: "voie royale"???

esp32 gatt_server + deep sleep (examples/system/ du sdk)
sur un tel lineageos, après une première connexion, lorsque l'esp32 réapparait au wakeup, le gatt android se reconnecte, toutes les 20min. (un test en cours fin avril: 20 jours)
mais UI on top, je ne touchais au tel que 2/3 fois par jour, et jamais de mode avion

les essais sur un tel de production sont moins positifs. Le pb principal = disparition de l'UI et pas de reconnect. 
tentative de broadcastreceiver sortie de mode avion. En cours.












## 2 connexions simultanées: possible sans difficulté:
-après récupération d'un adapter chez le bluetoothManager:
-tu crées 2 bluetooth devices avec getRemoteDevice(BDADDR_[1/2])
-tu connectGatt() sur chacune d'entre elles. Elles peuvent sharer la même BluetoothGattCallback(). tu distingues dans la cb avec
	gatt.getDevice().getAddress() 








## Old
 
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
 
 



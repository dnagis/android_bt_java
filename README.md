 Bluetooth en java, keep simple please
 
 Basé sur stub_service (StartVvnx) Squelette (arborescence et Android.mk) tiré de development/samples/
 
 Première fonction implémentée: LeScan mBluetoothLeScanner.startScan(filters, settings, mScanCallback) --> mScanCallback 
 
 Deuxième fonction: gatt client: se connecte à l'esp32->bluetooth/bluedroid/ble/gatt_server
 
 
  
 aosp:
 make BlueVvnx 
 adb uninstall com.example.android.bluevvnx 
 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

 
 adb shell:
 dumpsys deviceidle whitelist +com.example.android.bluevvnx
 pm grant com.example.android.bluevvnx android.permission.ACCESS_FINE_LOCATION
 pm grant com.example.android.bluevvnx android.permission.ACCESS_COARSE_LOCATION 
 
 am start-service com.example.android.bluevvnx/.BlueVvnx  
 
 ou avec un intent explicite, syntaxe:
 
 am start-service -a android.intent.action.DIAL com.example.android.bluevvnx/.BlueVvnx
  
 logcat -s BlueVvnx
 
 am stop-service com.example.android.bluevvnx/.BlueVvnx
 

 Page très helpfull pour GATT:
 http://nilhcem.com/android-things/bluetooth-low-energy
 
 



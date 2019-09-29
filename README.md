 Bluetooth en java, le plus simple possible 
 
 Basé sur stub_service (StartVvnx) Squelette (arborescence et Android.mk) tiré de development/samples/
 
 Première fonction implémentée: LeScan mBluetoothLeScanner.startScan(filters, settings, mScanCallback) --> mScanCallback 
 
 
  
 aosp:
 make BlueVvnx 
 adb uninstall com.example.android.bluevvnx 
 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

 
 adb shell:
 dumpsys deviceidle whitelist +com.example.android.bluevvnx
 am start-service com.example.android.bluevvnx/.BlueVvnx  
 am stop-service com.example.android.bluevvnx/.BlueVvnx
 --car les autorisations de localisation ne sont plus possibles programmatically, remember? (vie privée etc...)
 pm grant com.example.android.bluevvnx android.permission.ACCESS_FINE_LOCATION
 pm grant com.example.android.bluevvnx android.permission.ACCESS_COARSE_LOCATION
  
 
 logcat -s BlueVvnx
 
 
 ***essai septembre 2019: fonctionnement très inconstant, natif (BT HAL) marche un peu mieux***


 
 



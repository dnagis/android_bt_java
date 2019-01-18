 Le plus simple possible pour avoir des retours de LeScan
 
 Basé sur stub_service (StartVvnx) Squelette (arborescence et Android.mk) tiré de development/samples/
  
 aosp:
 make BlueVvnx 
 adb uninstall com.example.android.bluevvnx 
 adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

 
 adb shell:
 dumpsys deviceidle whitelist +com.example.android.bluevvnx
 am start-service com.example.android.bluevvnx/.BlueVvnx  
 am stop-service com.example.android.bluevvnx/.BlueVvnx
 --chiant mais il capte pas les permissions du manifest:
 pm grant com.example.android.bluevvnx android.permission.ACCESS_FINE_LOCATION
 pm grant com.example.android.bluevvnx android.permission.ACCESS_COARSE_LOCATION
  
 
 logcat -s BlueVvnx
 


 
 



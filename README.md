# Basé sur stub_service (StartVvnx)
  
 Squelette (arborescence et Android.mk) tiré de development/samples/
 ## make BlueVvnx (LOCAL_PACKAGE_NAME dans le Android.mk
 
 ## adb uninstall com.example.android.bluevvnx
 
 
 ## adb install out/target/product/mido/system/app/BlueVvnx/BlueVvnx.apk

 
 Lancement du service en shell (nom du service: celui déclaré dans le manifest -component name-) 
 
 indispensable, survit au reboot (tant que tu réinstalles pas l'appli), sinon app is in background uid null
 dumpsys deviceidle whitelist +com.example.android.bluevvnx
 
 # am start-service com.example.android.bluevvnx/.BlueVvnx  
  
 
 logcat -s BlueVvnx
 
 
 Lancement avec un intent explicite, syntaxe:
 am start-service -a android.intent.action.DIAL com.example.android.bluevvnx/.BlueVvnx

 
 



package app.com.trizesolutions.webprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {
    private WebPrint app;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    app = (WebPrint) context.getApplicationContext();
                    boolean checkStartYn = app.preferences.getBoolean("prefStartYn", true);

                    if(checkStartYn) {
                        Intent sintent = new Intent(context, RelayService.class);
                        sintent.putExtra("sourceport", app.preferences.getString("prefsourceport", "8080"));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(sintent);
                        } else {
                            context.startService(sintent);
                        }
                        // Log.d("BootReceiver", "Service loaded at start..!");
                    }else{
                        // Log.d("BootReceiver", "STOP ..!");
                    }

                }
            }


        }
    }
}

package namcap.dnscryptAndroidclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created on 3/15/2017.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if (action == null) return;
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (!sharedPreferences.getBoolean(Constants.PREF_AUTOSTART, false)) {
                return;
            }

            //Restore settings
            DataBucket.data_dir = context.getFilesDir().toString();
            Set<String> serv = sharedPreferences.getStringSet(Constants.SETTING_SERVERS, null);
            if (serv != null) {
                DataBucket.servers.addAll(serv);
                if (!DataBucket.servers.isEmpty()) {
                    try {
                        DataBucket.portSelected = Integer.parseInt(
                                sharedPreferences.getString(Constants.PREF_PORT,String.valueOf(Constants.INIT_SELECTED_PORT))
                    );
                    } catch (NumberFormatException e) {
                        return;
                    }
                    DataBucket.logLevel = sharedPreferences.getString(Constants.PREF_LOG_LEVEL,Constants.DEFAULT_LOG_LEVEL).charAt(0);
                    DataBucket.ephemeral_keys = sharedPreferences.getBoolean(Constants.PREF_EPHEMERAL_KEYS,false);
                    //Start service
                    Intent mIntent = new Intent(context, DnscryptService.class);
                    context.startService(mIntent);
                }
            }
        }
    }
}
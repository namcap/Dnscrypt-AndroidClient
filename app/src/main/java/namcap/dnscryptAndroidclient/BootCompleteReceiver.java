package namcap.dnscryptAndroidclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created on 3/15/2017.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREF_FNAME, Context.MODE_PRIVATE);

            if (!sharedPreferences.getBoolean(Constants.PREF_AUTOSTART, false)) {
                return;
            }

            //Restore settings
            ArrayList<String> servers = DataBucket.getServers();
            Set<String> serv = sharedPreferences.getStringSet(Constants.PREF_SERVERS, null);
            if (serv != null) {
                servers.addAll(serv);
                if (!servers.isEmpty()) {
                    DataBucket.portSelected = sharedPreferences.getInt(Constants.PREF_PORT, Constants.INIT_SELECTED_PORT);
                    DataBucket.ephemeral_keys = sharedPreferences.getBoolean(Constants.PREF_EPHEMERAL_KEYS,false);
                    //Start service
                    Intent mIntent = new Intent(context, DnscryptService.class);
                    context.startService(mIntent);
                }
            }
        }
    }
}
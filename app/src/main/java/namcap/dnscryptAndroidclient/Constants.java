package namcap.dnscryptAndroidclient;


//import android.os.Environment; //Debug

/**
 * Created on 3/12/2017.
 */

class Constants {
    static final String SERVICE_STATUS_EVENT="service_status_event";
    static final String SERVICE_STATUS_EVENT_NEW_STATUS="new_status";
    static final String SERVICE_LOG_EVENT="service_log_event";
    static final String SERVICE_LOG_EVENT_APPEND="service_append_log";
    static final String SERVICE_LOG_EVENT_CLEAR="service_clear_log";
    //Strings PREF_* below should match those in the file preference.xml
    static final String PREF_PORT="pref_port";
    static final String PREF_AUTOSTART="pref_auto_start";
    static final String PREF_LOG_LEVEL="pref_log_level";
    static final String PREF_EPHEMERAL_KEYS="pref_ephemeral_keys";
    static final String PREF_SELECT_SERVER="dummy_pref_sel_serv";
    //Strings PREF_* above should match those in the file preference.xml
    static final String SETTING_SERVERS ="setting_selected_servers";
    static final String CSV_FILE="/etc/dnscrypt-proxy/dnscrypt-resolvers.csv";
    //static final String CSV_FILE= Environment.getExternalStorageDirectory().getAbsolutePath()+"/dnscrypt-resolvers.csv"; //Debug
    static final String[] CSV_FIELD={"Name","DNSSEC validation","No logs","Namecoin"};
    static final int INIT_SELECTED_PORT=1024;
    static final String DEFAULT_LOG_LEVEL="3";

}

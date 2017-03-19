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
    static final String PREF_FNAME ="preference_filename";
    static final String PREF_PORT="port";
    static final String PREF_SERVERS="selected_servers";
    static final String PREF_AUTOSTART="auto_start";
    static final String CSV_FILE="/etc/dnscrypt-proxy/dnscrypt-resolvers.csv";
    //static final String CSV_FILE= Environment.getExternalStorageDirectory().getAbsolutePath()+"/dnscrypt-resolvers.csv"; //Debug
    static final String[] CSV_FIELD={"Name","DNSSEC validation","No logs","Namecoin"};
    static final int INIT_SELECTED_PORT=1024;

}

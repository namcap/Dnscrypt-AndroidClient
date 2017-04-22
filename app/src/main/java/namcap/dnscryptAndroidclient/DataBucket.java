package namcap.dnscryptAndroidclient;

import java.util.ArrayList;

/**
 * Created on 3/14/2017.
 */

class DataBucket {
    private final static ArrayList<String> servers=new ArrayList<>();
    static ArrayList<String> getServers() {
        return servers;
    }

    static int portSelected;
    static boolean autoStart;
    static boolean ephemeral_keys;

    static final StringBuilder serviceLogBuilder=new StringBuilder();

}

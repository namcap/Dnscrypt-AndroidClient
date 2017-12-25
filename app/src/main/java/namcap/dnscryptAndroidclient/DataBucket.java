package namcap.dnscryptAndroidclient;

import java.util.ArrayList;

/**
 * Created on 3/14/2017.
 */

class DataBucket {
    final static ArrayList<String> servers=new ArrayList<>();

    //A list of all available servers
    static ArrayList<String[]> server_list=null;
    static int portSelected;
    static Character logLevel;
    static boolean ephemeral_keys;
    static String data_dir;

}

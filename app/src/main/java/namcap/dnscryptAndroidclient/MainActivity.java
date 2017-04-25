package namcap.dnscryptAndroidclient;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 3/11/2017.
 */

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    //A list of all available servers
    //server_list -> DataBucket.server_list
    private ArrayList<String[]> server_list;
    //A list of selected servers
    private final ArrayList<String> serverSelected=DataBucket.servers;
    ///\Make sure only one thread writes to server list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Parse CSV file
        try {
            ArrayList<String> colName=CSVReader.getFieldName(Constants.CSV_FILE);
            ArrayList<Integer> filter=new ArrayList<>();
            int ind;
            for (String i : Constants.CSV_FIELD) {
                ind=colName.indexOf(i);
                if (0<=ind) {
                    filter.add(ind);
                }
            }
            server_list=CSVReader.getData(Constants.CSV_FILE,1,filter);
        } catch (FileNotFoundException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            //Empty list
            server_list=new ArrayList<>();
        }
        DataBucket.server_list=server_list;

        //Add toolbar
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize tabLayout
        final int tabCnt = 3;
        tabLayout=(TabLayout) findViewById(R.id.tablayout);
        //Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("HOME"));
        tabLayout.addTab(tabLayout.newTab().setText("SETTING"));
        tabLayout.addTab(tabLayout.newTab().setText("LOG"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //Make sure that number_of_tabs_added==tabCnt
        if (BuildConfig.DEBUG && tabLayout.getTabCount()!= tabCnt) {
            throw new AssertionError("tabLayout.getTabCount()!=tabCnt");
        }
        //Add listener to enable users to change tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos=tab.getPosition();
                viewPager.setCurrentItem(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Initialize viewPager
        viewPager=(ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new Pager(getFragmentManager(), tabCnt));
        viewPager.setOffscreenPageLimit(tabCnt -1);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position){
                TabLayout.Tab tab=tabLayout.getTabAt(position);
                if (tab!=null){
                    tab.select();
                }
            }
        });

        //Restore preferences
        restorePreference();
    }

    private void restorePreference() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            DataBucket.portSelected = Integer.parseInt(
                    settings.getString(Constants.PREF_PORT, String.valueOf(Constants.INIT_SELECTED_PORT))
                    );
        } catch (NumberFormatException e) {
            DataBucket.portSelected = Constants.INIT_SELECTED_PORT;
        }
        DataBucket.logLevel=settings.getString(Constants.PREF_LOG_LEVEL,Constants.DEFAULT_LOG_LEVEL).charAt(0);
        DataBucket.ephemeral_keys=settings.getBoolean(Constants.PREF_EPHEMERAL_KEYS,false);
        Set<String> serv=settings.getStringSet(Constants.SETTING_SERVERS,null);
        if (serv != null) {
            serverSelected.clear();
            //Validate setting
            for (String i : serv) {
                for (String[] k : server_list) {
                    if (k[0].equals(i)) {
                        serverSelected.add(i);
                        break;
                    }
                }
            }
        }
    }

    private void savePreferences() {
        SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=settings.edit();
        Set<String> servers=new HashSet<>(DataBucket.servers);
        editor.putStringSet(Constants.SETTING_SERVERS,servers);
        //Preference framework will handle the rest of preferences
        //pref_port: String
        //pref_log_level: String
        //pref_auto_start: boolean
        //pref_ephemeral_keys: boolean

        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        savePreferences();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}

package namcap.dnscryptAndroidclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created on 3/11/2017.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    //A list of available servers
    private ArrayList<String[]> server_list;
    //A list of selected servers, not read-only
    private final ArrayList<String> serverSelected=DataBucket.getServers();
    ///\Make sure only one thread writes to server list
    //A list of servers to be shown in a dialog
    private CharSequence[] serverSelectionList=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Parse CSV file and fetch server list
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
        viewPager.setAdapter(new Pager(getSupportFragmentManager(), tabCnt));
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

        //Broadcast receiver to receive the status change message from service
        LocalBroadcastManager.getInstance(this).registerReceiver(mMsgRecv_tab1,
                                                                new IntentFilter(Constants.SERVICE_STATUS_EVENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMsgRecv_log,
                                                                new IntentFilter(Constants.SERVICE_LOG_EVENT));

        //Restore preferences
        restorePreference();
    }

    private void restorePreference() {
        SharedPreferences settings=getSharedPreferences(Constants.PREF_FNAME,MODE_PRIVATE);
        DataBucket.autoStart=settings.getBoolean(Constants.PREF_AUTOSTART,true);
        DataBucket.portSelected=settings.getInt(Constants.PREF_PORT,Constants.INIT_SELECTED_PORT);
        DataBucket.ephemeral_keys=settings.getBoolean(Constants.PREF_EPHEMERAL_KEYS,false);
        Set<String> serv=settings.getStringSet(Constants.PREF_SERVERS,null);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tab1_button1:
                //Button, Start service
                Button btn=(Button) v;
                btn.setEnabled(false);
                if (btn.getText()==getString(R.string.stop)) {
                    stopService();
                }
                else {
                    startService();
                }
                break;
            case R.id.tab2_button1:
                //Button, Set port number
                EditText et=(EditText)findViewById(R.id.tab2_edittext1);
                final int min=1024,max=65535;
                if (et != null) {
                    try {
                        int inp=Integer.parseInt(et.getText().toString());
                        if (min<=inp && inp<=max) {
                            //Input looks good
                            DataBucket.portSelected=inp;
                        }
                        else {
                            //Bad input
                            et.setText(String.valueOf(Constants.INIT_SELECTED_PORT));
                        }
                    } catch (NumberFormatException e) {
                        //Bad input
                        et.setText(String.valueOf(Constants.INIT_SELECTED_PORT));
                    }
                }
                break;
            case R.id.tab2_button2:
                //Button: Select servers
                showServerSelectionDialog();
                break;
            case R.id.tab2_checkBox1:
                //Checkbox: Auto start
                DataBucket.autoStart=((CheckBox)v).isChecked();
                break;
            case R.id.tab2_checkBox2:
                //Checkbox: Ephemeral keys
                DataBucket.ephemeral_keys=((CheckBox)v).isChecked();
                if (DataBucket.ephemeral_keys) {
                    new AlertDialog.Builder(this)
                            .setTitle("Alert")
                            .setMessage("This mitigates server logging by generating an ephemeral key pair for each query.\n\n" +
                                    "But this may be slow, especially on non-Intel CPUs.")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
                break;
            default:
                break;
        }
    }

    private void startService() {
        Intent mIntent=new Intent(getBaseContext(),DnscryptService.class);
        startService(mIntent);
    }

    private void stopService() {
        stopService(new Intent(getBaseContext(),DnscryptService.class));
    }


    private void showServerSelectionDialog() {
        if (serverSelectionList==null) {
            ArrayList<CharSequence> listItem=new ArrayList<>();
            for (String[] i : server_list) {
                listItem.add(TextUtils.join(",",i));
            }
            serverSelectionList=listItem.toArray(new CharSequence[0]);
        }

        int num=server_list.size();
        boolean[] checkedServer=new boolean[num];
        for (int i=0;i<num;++i) {
            checkedServer[i]=false;
            for (String k : serverSelected) {
                if (k.equals(server_list.get(i)[0])) {
                    checkedServer[i]=true;
                    break;
                }
            }
        }

        DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener=new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    serverSelected.add(server_list.get(which)[0]);
                }
                else {
                    serverSelected.remove(server_list.get(which)[0]);
                }
            }
        };

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(TextUtils.join(",",Constants.CSV_FIELD))
                .setMultiChoiceItems(serverSelectionList,checkedServer,onMultiChoiceClickListener)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateServerCount();
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void updateServerCount() {
        TextView textView=(TextView)findViewById(R.id.tab2_text1);
        if (textView != null) {
            textView.setText(String.format(Locale.getDefault(),"%s %d", getString(R.string.selected_servers_num),DataBucket.getServers().size()));
        }
    }

    private void savePreferences() {
        SharedPreferences settings=getSharedPreferences(Constants.PREF_FNAME,MODE_PRIVATE);
        SharedPreferences.Editor editor=settings.edit();
        editor.putBoolean(Constants.PREF_AUTOSTART,DataBucket.autoStart);
        editor.putBoolean(Constants.PREF_EPHEMERAL_KEYS,DataBucket.ephemeral_keys);
        editor.putInt(Constants.PREF_PORT,DataBucket.portSelected);
        Set<String> servers=new HashSet<>(DataBucket.getServers());
        editor.putStringSet(Constants.PREF_SERVERS,servers);

        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        savePreferences();
    }

    @Override
    protected void onDestroy(){
        //Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMsgRecv_tab1);
        super.onDestroy();
    }

    //Update tab1_button1 when receiving broadcast
    private final BroadcastReceiver mMsgRecv_tab1 =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean serviceRunning=intent.getExtras().getBoolean(Constants.SERVICE_STATUS_EVENT_NEW_STATUS);
            Button btn=(Button) findViewById(R.id.tab1_button1);
            if (btn!=null) {
                if (serviceRunning) {
                    btn.setText(R.string.stop);
                }
                else {
                    btn.setText(R.string.start);
                }
                btn.setEnabled(true);
            }
            ImageView img=(ImageView) findViewById(R.id.tab1_imageView1);
            if (img!=null) {
                if (serviceRunning) {
                    img.setImageResource(R.drawable.lock);
                }
                else {
                    img.setImageResource(R.drawable.broken_lock);
                }
            }
        }
    };

    //Update logView when receiving broadcast
    private final BroadcastReceiver mMsgRecv_log=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView textView=(TextView) findViewById(R.id.log);
            if (textView != null) {
                Bundle extra=intent.getExtras();
                if (extra != null) {
                    String str=extra.getString(Constants.SERVICE_LOG_EVENT_APPEND);
                    if (str!=null) {
                        textView.append(str);
                    }
                    if (extra.getBoolean(Constants.SERVICE_LOG_EVENT_CLEAR,false)) {
                        textView.setText("");
                    }
                }
            }
        }
    };

}

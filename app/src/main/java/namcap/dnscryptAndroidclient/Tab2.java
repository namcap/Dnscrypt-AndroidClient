package namcap.dnscryptAndroidclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;

/**
 * Created on 3/12/2017.
 */

public class Tab2 extends PreferenceFragment {

    private final ArrayList<String> serverSelected=DataBucket.servers;
    //A list of all available servers
    private ArrayList<String[]> server_list;
    //A list to be shown in a dialog
    private CharSequence[] serverSelectDialogList=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);

        Preference pref=findPreference(Constants.PREF_PORT);
        if (pref != null) {
            pref.setSummary(getString(R.string.pref_port_summary)+DataBucket.portSelected);
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int newVal = Integer.parseInt((String) newValue);
                        if (1024<=newVal && newVal<=65535) {
                            DataBucket.portSelected=newVal;
                            //Update summary
                            Preference pref=findPreference(Constants.PREF_PORT);
                            if (pref != null) {
                                pref.setSummary(getString(R.string.pref_port_summary)+DataBucket.portSelected);
                            }
                            return true;
                        }
                        return false;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            } );
        }

        pref=findPreference(Constants.PREF_LOG_LEVEL);
        if (pref != null) {
            pref.setSummary(getString(R.string.pref_log_level_summary)+DataBucket.logLevel);
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    DataBucket.logLevel=((String)newValue).charAt(0);
                    //Update summary
                    Preference pref=findPreference(Constants.PREF_LOG_LEVEL);
                    if (pref != null) {
                        pref.setSummary(getString(R.string.pref_log_level_summary)+DataBucket.logLevel);
                    }
                    return true;
                }
            });
        }

        pref=findPreference(Constants.PREF_EPHEMERAL_KEYS);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    DataBucket.ephemeral_keys=(boolean)newValue;
                    if (DataBucket.ephemeral_keys) {
                        //AlertDialog
                        View view=getView();
                        if (view != null) {
                            new AlertDialog.Builder(view.getContext())
                                    .setMessage(R.string.alert_ephemeral_keys_msg)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                    return true;
                }
            });
        }

        pref=findPreference(Constants.PREF_SELECT_SERVER);
        if (pref != null) {
            pref.setSummary(getString(R.string.pref_sel_serv_summary)+serverSelected.size());
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Pop up a dialog
                    showServerSelectDialog();
                    return true;
                }
            });
        }
    }

    private void showServerSelectDialog() {
        server_list=DataBucket.server_list;
        if (serverSelectDialogList == null) {
            ArrayList<CharSequence> listItem=new ArrayList<>();
            for (String[] i : server_list) {
                listItem.add(TextUtils.join(",",i));
            }
            serverSelectDialogList=listItem.toArray(new CharSequence[0]);
        }

        int num=server_list.size();
        boolean[] serverSelectDialogCheckedItem=new boolean[num];
        for (int i=0;i<num;++i) {
            serverSelectDialogCheckedItem[i]=false;
            for (String k : serverSelected) {
                if (k.equals(server_list.get(i)[0])) {
                    serverSelectDialogCheckedItem[i]=true;
                    break;
                }
            }
        }

        DialogInterface.OnMultiChoiceClickListener
                onMultiChoiceClickListener = new DialogInterface.OnMultiChoiceClickListener() {
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

        View view=getView();
        if (view != null) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle(TextUtils.join(",",Constants.CSV_FIELD))
                    .setMultiChoiceItems(serverSelectDialogList,
                            serverSelectDialogCheckedItem,
                            onMultiChoiceClickListener)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateServerSelectSummary();
                        }
                    })
                    .show();
        }
    }

    private void updateServerSelectSummary() {
        Preference pref=findPreference(Constants.PREF_SELECT_SERVER);
        if (pref != null) {
            pref.setSummary(getString(R.string.pref_sel_serv_summary)+serverSelected.size());
        }
    }

}
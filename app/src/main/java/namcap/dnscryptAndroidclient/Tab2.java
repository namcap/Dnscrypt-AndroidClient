package namcap.dnscryptAndroidclient;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created on 3/12/2017.
 */

public class Tab2 extends Fragment {

    public Tab2(){

    }

    /*@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.tab2,container,false);

        //Restore port number
        EditText et=(EditText)view.findViewById(R.id.tab2_edittext1);
        if (et != null) {
            et.setText(String.valueOf(DataBucket.portSelected));
        }

        //Restore selected server count
        TextView textView=(TextView)view.findViewById(R.id.tab2_text1);
        if (textView != null) {
            textView.setText(String.format(Locale.getDefault(),"%s %d", getString(R.string.selected_servers_num),DataBucket.getServers().size()));
        }

        //Restore auto start
        CheckBox checkBox=(CheckBox)view.findViewById(R.id.tab2_checkBox1);
        if (checkBox != null) {
            checkBox.setChecked(DataBucket.autoStart);
        }

        return view;
    }

}

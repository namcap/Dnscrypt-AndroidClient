package namcap.dnscryptAndroidclient;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created on 3/12/2017.
 */

public class Tab3 extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Register LocalBroadcastReceiver
        //Don't forget to unregister in onDestroy()
        Context context=getActivity().getApplicationContext();
        LocalBroadcastManager
                .getInstance(context)
                .registerReceiver(mMsgRecv_log,new IntentFilter(Constants.SERVICE_LOG_EVENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.tab3,container,false);
        TextView textView=(TextView) view.findViewById(R.id.log);
        if (textView!=null) {
            textView.setMovementMethod(new ScrollingMovementMethod());
        }
        return view;
    }

    @Override
    public void onDestroy(){
        //Unregister LocalBroadcastReceiver
        Context context=getActivity().getApplicationContext();
        LocalBroadcastManager
                .getInstance(context)
                .unregisterReceiver(mMsgRecv_log);

        super.onDestroy();
    }

    private final BroadcastReceiver mMsgRecv_log=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View view=getView();
            if (view != null) {
                TextView textView = (TextView)view.findViewById(R.id.log);
                if (textView != null) {
                    Bundle bundle=intent.getExtras();
                    if (bundle != null) {
                        String str=bundle.getString(Constants.SERVICE_LOG_EVENT_APPEND);
                        if (str != null) {
                            textView.append(str);
                        }
                        if (bundle.getBoolean(Constants.SERVICE_LOG_EVENT_CLEAR)) {
                            textView.setText("");
                        }
                    }
                }
            }
        }
    };

}

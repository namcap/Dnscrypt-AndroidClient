package namcap.dnscryptAndroidclient;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created on 3/12/2017.
 */

public class Tab1 extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Register LocalBroadcastReceiver
        //Don't forget to unregister in onDestroy()
        Context context=getActivity().getApplicationContext();
        LocalBroadcastManager
                .getInstance(context)
                .registerReceiver(mMsgRecv_btn1,new IntentFilter(Constants.SERVICE_STATUS_EVENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.tab1,container,false);
        boolean serviceIsRunning=DnscryptService.isRunning();
        //Set up tab1_button1
        final Button btn=(Button) view.findViewById(R.id.tab1_button1);
        if (btn!=null){
            if (serviceIsRunning) {
                btn.setText(R.string.stop);
            }
            else {
                btn.setText(R.string.start);
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn.setEnabled(false);
                    if (btn.getText() == getString(R.string.stop)) {
                        stopService();
                    }
                    else {
                        startService();
                    }
                }
            });
        }
        //Set up tab1_imageView1
        ImageView img=(ImageView) view.findViewById(R.id.tab1_imageView1);
        if (img!=null) {
            if (serviceIsRunning) {
                img.setImageResource(R.drawable.lock);
            }
            else {
                img.setImageResource(R.drawable.broken_lock);
            }
        }
        return view;
    }

    private void startService() {
        Context context=getActivity().getApplicationContext();
        Intent mIntent=new Intent(context,DnscryptService.class);
        context.startService(mIntent);

    }

    private void stopService() {
        Context context=getActivity().getApplicationContext();
        Intent mIntent=new Intent(context,DnscryptService.class);
        context.stopService(mIntent);
    }

    @Override
    public void onDestroy() {
        //Unregister LocalBroadcastReceiver
        Context context=getActivity().getApplicationContext();
        LocalBroadcastManager
                .getInstance(context)
                .unregisterReceiver(mMsgRecv_btn1);

        super.onDestroy();
    }

    private final BroadcastReceiver mMsgRecv_btn1=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View view=getView();
            if (view != null) {
                boolean serviceRunning = intent.getExtras().getBoolean(Constants.SERVICE_STATUS_EVENT_NEW_STATUS);
                Button btn = (Button)view.findViewById(R.id.tab1_button1);
                if (btn != null) {
                    if (serviceRunning) {
                        btn.setText(R.string.stop);
                    }
                    else {
                        btn.setText(R.string.start);
                    }
                    btn.setEnabled(true);
                }
                ImageView imageView = (ImageView)view.findViewById(R.id.tab1_imageView1);
                if (imageView != null) {
                    if (serviceRunning) {
                        imageView.setImageResource(R.drawable.lock);
                    }
                    else {
                        imageView.setImageResource(R.drawable.broken_lock);
                    }
                }
            }
        }
    };

}

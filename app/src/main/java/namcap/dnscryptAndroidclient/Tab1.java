package namcap.dnscryptAndroidclient;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private Bitmap bmp_lock;
    private Bitmap bmp_broken_lock;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Decode images

        bmp_lock= BitmapFactory.decodeResource(getResources(),R.drawable.lock);
        bmp_broken_lock= BitmapFactory.decodeResource(getResources(),R.drawable.broken_lock);

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
        final Button btn=view.findViewById(R.id.tab1_button1);
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
        setImage(view, serviceIsRunning);
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

    private void setImage(final View view, final boolean serviceIsRunning) {
        ImageView imageView = view.findViewById(R.id.tab1_imageView1);
        if (imageView != null) {
            if (serviceIsRunning) {
                imageView.setImageBitmap(bmp_lock);
            }
            else {
                imageView.setImageBitmap(bmp_broken_lock);
            }
        }
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
            Bundle extras=intent.getExtras();
            if (view != null && extras != null) {
                boolean serviceIsRunning = extras.getBoolean(Constants.SERVICE_STATUS_EVENT_NEW_STATUS);
                Button btn = view.findViewById(R.id.tab1_button1);
                if (btn != null) {
                    if (serviceIsRunning) {
                        btn.setText(R.string.stop);
                    }
                    else {
                        btn.setText(R.string.start);
                    }
                    btn.setEnabled(true);
                }
                setImage(view, serviceIsRunning);
            }
        }
    };

}

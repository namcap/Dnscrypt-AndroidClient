package namcap.dnscryptAndroidclient;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created on 3/12/2017.
 */

public class Tab1 extends Fragment {

    public Tab1(){

    }

    /*@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.tab1,container,false);
        boolean serviceIsRunning=DnscryptService.isRunning();
        //Update tab1_button1
        Button btn=(Button) view.findViewById(R.id.tab1_button1);
        if (btn!=null){
            if (serviceIsRunning) {
                btn.setText(R.string.stop);
            }
            else {
                btn.setText(R.string.start);
            }
        }
        //Update tab1_imageView1
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

}

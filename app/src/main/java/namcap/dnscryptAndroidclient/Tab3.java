package namcap.dnscryptAndroidclient;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created on 3/12/2017.
 */

public class Tab3 extends Fragment {
    public Tab3(){

    }

    /*@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.tab3,container,false);
        TextView textView=(TextView) view.findViewById(R.id.log);
        if (textView!=null) {
            textView.setMovementMethod(new ScrollingMovementMethod());
        }
        return view;
    }

}

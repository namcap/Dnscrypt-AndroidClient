package namcap.dnscryptAndroidclient;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created on 3/12/2017.
 */

class Pager extends FragmentPagerAdapter {

    private final int tabCnt;

    Pager(FragmentManager fm, int tabCnt){
        super(fm);
        this.tabCnt=tabCnt;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Tab1();
            case 1:
                return new Tab2();
            case 2:
                return new Tab3();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCnt;
    }
}

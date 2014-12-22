/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import jp.ne.docomo.smt.dev.calendarview.R;

public class BaseFragment extends Fragment {
    protected int fragmentLevel = 1;

    public static void setFragment(FragmentActivity context, BaseFragment fragment) {
        if (context == null) {
            return;
        }

        FragmentManager fragmentManager = context.getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate(
                    fragment.getTransactionName(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_content, fragment);

        fragmentTransaction.addToBackStack(fragment.getTransactionName()).commit();
    }

    public String getTransactionName() {
        return "Level" + fragmentLevel;
    }

    public void setFragmentLevel(int fragmentLevel) {
        this.fragmentLevel = fragmentLevel;
    }

    public int getFragmentLevel() {
        return fragmentLevel;
    }
}

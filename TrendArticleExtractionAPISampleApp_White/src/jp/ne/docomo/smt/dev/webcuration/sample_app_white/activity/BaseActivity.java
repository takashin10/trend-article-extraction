/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.activity;

import java.util.List;

import jp.ne.docomo.smt.dev.webcuration.data.CurationGenreData;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.loader.GenreLoader;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * MainActivity(記事表示画面)およびKeywordSearchActivity(キーワード検索画面)の基底Activity
 * SDKから取得したジャンル一覧をTabNavigationに表示しViewPagerと連動させる
 */
public abstract class BaseActivity extends Activity implements
        LoaderCallbacks<List<CurationGenreData>>, TabListener, OnPageChangeListener {

    /** Loaderが生成される際に呼び出されるコールバック */
    @Override
    public Loader<List<CurationGenreData>> onCreateLoader(int id, Bundle args) {
        return new GenreLoader(this);
    }

    /** Loaderがリセットされる際に呼び出されるコールバック */
    @Override
    public void onLoaderReset(Loader<List<CurationGenreData>> loader) {
        /* NOOP */
    }

    /** ActionBar上のTabが選択状態になった際に呼び出されるコールバック */
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // ViewPagerの選択状態を追従させる
        ViewPager pager = getViewPager();
        if (pager != null) {
            int position = tab.getPosition();
            PagerAdapter adapter = pager.getAdapter();
            if ((position >= 0) && (adapter != null)
                    && (position < adapter.getCount())
                    && (position != pager.getCurrentItem())) {
                pager.setCurrentItem(position, true);
            }
        }
    }

    /** ActionBar上のTabが非選択状態になった際に呼び出されるコールバック */
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        /* NOOP */
    }

    /** 選択状態となっているTabが再度選択された際に呼び出されるコールバック */
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        /* NOOP */
    }

    /** ViewPagerのスクロール操作状態が変化した際に呼び出されるコールバック */
    @Override
    public void onPageScrollStateChanged(int state) {
        /* NOOP */
    }

    /** ViewPagerのスクロール座標が変化した際に呼び出されるコールバック */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* NOOP */
    }

    /** ViewPagerのページが選択された際に呼び出されるコールバック */
    @Override
    public void onPageSelected(int position) {
        // Tabの選択状態を追従させる
        ActionBar actionBar = getActionBar();
        if ((position >= 0) && (position < actionBar.getTabCount())) {
            actionBar.getTabAt(position).select();
        }
    }

    /**
     * Tabに連動させるViewPagerを返却
     * 
     * @return Tabに連動させるViewPager
     */
    protected abstract ViewPager getViewPager();

}

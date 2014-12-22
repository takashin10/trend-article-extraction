/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.activity;

import java.util.List;

import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import jp.ne.docomo.smt.dev.webcuration.data.CurationGenreData;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.loader.BaseLoader;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider.TrendArticleDb;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

/** キーワード検索画面 */
public class KeywordSearchActivity extends BaseActivity
        implements OnClickListener, TextWatcher, OAuthCallback {

    // field
    // ----------------------------------------------------------------

    private ViewPager mPager;
    private EditText mEditKeyword;
    private ImageButton mBtnClear;

    // method
    // ----------------------------------------------------------------

    /** Viewがクリックされた際に呼び出されるコールバック */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_search_clear) {
            mEditKeyword.setText(null);
        }
    }

    /** 文字列入力が更新される直前に呼び出されるコールバック */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        /* NOOP */
    }

    /** 文字列入力が更新される際に呼び出されるコールバック */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /* NOOP */
    }

    /** 文字列入力が更新された直後に呼び出されるコールバック */
    @Override
    public void afterTextChanged(Editable s) {
        ResultListFragmentAdapter adapter = (ResultListFragmentAdapter) mPager.getAdapter();
        if (adapter != null) {
            adapter.setKeyword(s.toString());
        }
        if (TextUtils.isEmpty(s)) {
            mBtnClear.setVisibility(View.GONE);
        } else {
            mBtnClear.setVisibility(View.VISIBLE);
        }
    }

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
        if (BuildConfig.DEBUG) {
            Log.d(KeywordSearchActivity.class.getSimpleName(), "OAuth has been Refreshed.");
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    /** docomo IDの認証に失敗した際に呼ばれるコールバック */
    @Override
    public void onError(OAuthError error) {
        if (BuildConfig.DEBUG) {
            if (error != null) {
                Log.d("OAuthCallback", "Error"
                        + " Code:" + error.getErrorCode()
                        + " Msg:" + error.getErrorMessage());
                Throwable cause = error.getCause();
                if (cause != null) {
                    cause.printStackTrace();
                }
            }
        }
        runOnUiThread(new Runnable() {

            /** UIスレッドにポストされるView表示処理 */
            @Override
            public void run() {
                setEmptyViewVisibility(true);
            }

        });
    }

    /** Loaderの処理が完了した際に呼び出されるコールバック */
    @Override
    public void onLoadFinished(Loader<List<CurationGenreData>> loader,
            List<CurationGenreData> list) {
        // Tabを全て削除
        ActionBar actionBar = getActionBar();
        actionBar.removeAllTabs();

        // アクセストークンが有効期限切れの場合はリフレッシュする
        if (((BaseLoader<?>) loader).isAccessTokenExpired()) {
            AccessTokenManager.getInstance(this).startRefresh(this);
            return;
        }

        // ジャンル情報の取得
        if ((list == null) || list.isEmpty()) {
            setEmptyViewVisibility(true);
            return;
        }

        // 「すべて」Tabの生成と設定
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.txt_search_tab_all)
                .setTabListener(this));

        // 各ジャンルTabの生成と設定
        for (CurationGenreData genre : list) {
            actionBar.addTab(actionBar.newTab()
                    .setText(genre.getTitle())
                    .setTabListener(this));
        }

        // ViewPagerの更新
        mPager.setAdapter(new ResultListFragmentAdapter(getFragmentManager(), list));
        setEmptyViewVisibility(false);
    }

    /** Tabに連動させるViewPagerを返却 */
    @Override
    protected ViewPager getViewPager() {
        return mPager;
    }

    /** Activityが生成される際に呼び出されるコールバック */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ViewPagerの取得と設定
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);

        // ActionBarの初期設定
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.title_search);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ((View) findViewById(android.R.id.home).getParent()).setVisibility(View.GONE);

        // ActionBar上のカスタムViewの取得と設定
        View customView = actionBar.getCustomView();
        mEditKeyword = (EditText) customView.findViewById(R.id.edit_search_keyword);
        mBtnClear = (ImageButton) customView.findViewById(R.id.btn_search_clear);
        mEditKeyword.addTextChangedListener(this);
        mBtnClear.setOnClickListener(this);

        // GenreLoader起動
        getLoaderManager().restartLoader(0, null, this);
    }

    /** Activityが破棄される際に呼び出されるコールバック */
    @Override
    protected void onDestroy() {
        // GenreLoaderおよびジャンルデータの破棄
        getLoaderManager().destroyLoader(0);

        // 各種リスナの解放
        mPager.setOnPageChangeListener(null);
        mEditKeyword.removeTextChangedListener(this);
        mBtnClear.setOnClickListener(null);

        // 各種Viewの解放
        mPager = null;
        mEditKeyword = null;
        mBtnClear = null;
        super.onDestroy();
    }

    /**
     * ジャンル情報の取得に失敗した場合に専用のTextViewを表示する
     * 
     * @param isVisible trueでTextView表示、falseで非表示
     */
    private void setEmptyViewVisibility(boolean isVisible) {
        if (isVisible) {
            findViewById(R.id.txt_main_empty).setVisibility(View.VISIBLE);
            mPager.setVisibility(View.GONE);
        } else {
            findViewById(R.id.txt_main_empty).setVisibility(View.GONE);
            mPager.setVisibility(View.VISIBLE);
        }
    }

    // class
    // ----------------------------------------------------------------

    /** キーワード検索結果データと検索結果をジャンル毎に表示するViewPagerとを紐付けるFragmentStatePagerAdapter */
    private static class ResultListFragmentAdapter extends FragmentStatePagerAdapter {

        // field
        // ----------------------------------------------------------------

        private final List<CurationGenreData> mList;

        private String mKeyword = null;

        // method
        // ----------------------------------------------------------------

        /** 表示するFragmentを返却 */
        @Override
        public Fragment getItem(int position) {
            if (position <= 0) {
                return ResultListFragment.newInstance(
                        TrendArticleDb.Article.GENRE_ID_SEARCH_ALL, mKeyword);
            } else {
                return ResultListFragment.newInstance(
                        mList.get(position - 1).getGenreId(), mKeyword);
            }
        }

        /** 表示するページ数を返却 */
        @Override
        public int getCount() {
            return (mList.size() + 1);
        }

        /**
         * キーワードを設定
         * 
         * @param keyword 設定するキーワード文字列
         */
        void setKeyword(String keyword) {
            this.mKeyword = keyword;
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param fm FragmentManagerオブジェクト
         * @param list 検索結果記事のListオブジェクト
         */
        ResultListFragmentAdapter(FragmentManager fm, List<CurationGenreData> list) {
            super(fm);
            this.mList = list;
        }

    }

}

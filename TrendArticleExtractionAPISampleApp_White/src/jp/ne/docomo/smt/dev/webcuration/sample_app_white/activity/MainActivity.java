/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.activity;

import java.util.List;
import java.util.Locale;

import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import jp.ne.docomo.smt.dev.webcuration.data.CurationGenreData;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.loader.BaseLoader;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider.TrendArticleDb;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.service.ArticleUpdateService;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/** 記事表示画面 */
public class MainActivity extends BaseActivity implements OAuthCallback {

    // field
    // ----------------------------------------------------------------

    // API キー(開発者ポータルから取得したAPIキーを設定)
	private static final String API_KEY = 開発者ポータルから取得したAPIキーを設定してください;

    private static final int REQUEST_AUTH = 0;

    private static final int ID_MENU_SEARCH = Menu.FIRST;
    private static final int ID_MENU_AUTH = Menu.FIRST + 1;
    private static final int ID_MENU_RELOAD = Menu.FIRST + 2;
    private static final int ID_MENU_LANG = Menu.FIRST + 3;
    private static final int ID_MENU_ABOUT = Menu.FIRST + 4;
    private static final int ID_MENU_GENRES_FIRST = Menu.FIRST + 5;

    private boolean mForceUpdate;
    private ViewPager mPager;

    private List<CurationGenreData> mGenreList = null;
    private Locale mLocale = Locale.getDefault();


    // method
    // ----------------------------------------------------------------

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
    	System.out.println("test");
    	
        if (BuildConfig.DEBUG) {
            Log.d(MainActivity.class.getSimpleName(), "OAuth has been Refreshed.");
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
        // ActionBarアイテムの更新
        mGenreList = list;
        invalidateOptionsMenu();

        // Tabを全て削除
        ActionBar actionBar = getActionBar();
        actionBar.removeAllTabs();

        // アクセストークンが有効期限切れの場合はリフレッシュする
        if (((BaseLoader<?>) loader).isAccessTokenExpired()) {
            AccessTokenManager.getInstance(this).startRefresh(this);
            return;
        }

        // 各ジャンルの記事コンテンツおよびTabの更新
        if ((list == null) || list.isEmpty()) {
            setEmptyViewVisibility(true);
            return;
        }
        for (CurationGenreData genre : list) {
            // ジャンルを指定してDB上の記事コンテンツを更新
            ArticleUpdateService.startService(this,
                    genre.getGenreId(), mForceUpdate);

            // ジャンルTabを生成してActionBarに追加
            actionBar.addTab(actionBar.newTab()
                    .setText(genre.getTitle())
                    .setTabListener(this));
        }

        // アクセストークンを保持している場合はレコメンド記事コンテンツおよびTabの更新を行う
        boolean hasAccessToken = AccessTokenManager.getInstance(this).hasAccessToken();
        if (hasAccessToken) {
            // DB上のレコメンド記事コンテンツを更新
            ArticleUpdateService.startService(this,
                    TrendArticleDb.Article.GENRE_ID_RECOMEND, mForceUpdate);

            // レコメンドTabを生成してActionBarに追加
            actionBar.addTab(actionBar.newTab()
                    .setText(R.string.txt_genre_tab_recomend)
                    .setTabListener(this));
        }

        // ViewPagerの更新
        mPager.setAdapter(new ArticleListFragmentAdapter(
                getFragmentManager(), list, hasAccessToken));
        setEmptyViewVisibility(false);

        // 強制更新を解除
        mForceUpdate = false;
    }

    /** ActionBarのアイテムが更新される際に呼び出されるコールバック */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int order = Menu.FIRST;
        menu.clear();

        // キーワード検索
        menu.add(Menu.NONE, ID_MENU_SEARCH, order++, R.string.action_search)
                .setIcon(R.drawable.ic_action_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // 各ページへのリンク
        if ((mGenreList != null) && !mGenreList.isEmpty()) {
            int genreItemId = ID_MENU_GENRES_FIRST;

            // ジャンルリンクを追加
            for (CurationGenreData genre : mGenreList) {
                menu.add(Menu.NONE, genreItemId++, order++, genre.getTitle())
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }

            // アクセストークンを保持している場合はレコメンドリンクを追加
            if (AccessTokenManager.getInstance(this).hasAccessToken()) {
                menu.add(Menu.NONE, genreItemId++, order++, R.string.txt_genre_tab_recomend)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }

        // 認証設定
        menu.add(Menu.NONE, ID_MENU_AUTH, order++, R.string.action_auth)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        // 再読み込み
        menu.add(Menu.NONE, ID_MENU_RELOAD, order++, R.string.action_reload)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        // 言語切替
        menu.add(Menu.NONE, ID_MENU_LANG, order++, R.string.action_lang)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        // About
        menu.add(Menu.NONE, ID_MENU_ABOUT, order++, R.string.action_about)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    /** ActionBarのアイテムが選択された際に呼び出されるコールバック */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case ID_MENU_SEARCH:
                // キーワード検索
                startActivity(new Intent(this, KeywordSearchActivity.class));
                return true;
            case ID_MENU_AUTH:
                // 認証設定
                startActivityForResult(new Intent(this, AuthenticationActivity.class),
                        REQUEST_AUTH);
                return true;
            case ID_MENU_RELOAD:
                // 再読み込み
                mForceUpdate = true;
                getLoaderManager().restartLoader(0, null, this);
                return true;
            case ID_MENU_LANG:
                mLocale = setLocale(mLocale);
                initialize(mLocale);
                return true;
            case ID_MENU_ABOUT: {
                // アプリについて
                // Version Code, Version Nameを取得
                int versionCode = 0;
                String versionName = "";
                try {
                    PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                    versionCode = info.versionCode;
                    versionName = info.versionName;
                } catch (NameNotFoundException e) {
                    versionCode = 0;
                    versionName = "";
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }

                // MessageDialogを開く
                MessageDialog.show(getFragmentManager(), getString(R.string.app_name),
                        getString(R.string.msg_about_app_format, versionCode, versionName));
                return true;
            }
            default:
                break;
        }

        // ジャンルリンクの判別
        itemId -= ID_MENU_GENRES_FIRST;
        ActionBar actionBar = getActionBar();
        if ((itemId >= 0) && (itemId < actionBar.getTabCount())) {
            actionBar.getTabAt(itemId).select();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** ロケール切り替える */
    Locale setLocale(Locale locale) {
        if (locale.equals(Locale.JAPAN)) {
             locale = Locale.US;
        } else {
            locale = Locale.JAPAN;
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, null);
        return locale;
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
        initialize(mLocale);
    }

    /** Activityが破棄される際に呼び出されるコールバック */
    @Override
    protected void onDestroy() {
        // GenreLoaderおよびジャンル一覧の破棄
        getLoaderManager().destroyLoader(0);
        mGenreList = null;

        // 各種リスナの解放
        mPager.setOnPageChangeListener(null);

        // 各種Viewの解放
        mPager = null;
        super.onDestroy();
    }

    /** ActivityがstartActivityForResultで起動した別のActivityから結果を受け取った際に呼び出されるコールバック */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_AUTH:
                if (resultCode == RESULT_OK) {
                    mForceUpdate = true;
                    getLoaderManager().restartLoader(0, null, this);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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

    /** 記事コンテンツと記事コンテンツをジャンル毎に表示するViewPagerとを紐付けるFragmentStatePagerAdapter */
    private static class ArticleListFragmentAdapter extends FragmentStatePagerAdapter {

        // field
        // ----------------------------------------------------------------

        private final int mItemCount;
        private final List<CurationGenreData> mList;

        // method
        // ----------------------------------------------------------------

        /** 表示するFragmentを返却 */
        @Override
        public Fragment getItem(int position) {
            if (position >= mList.size()) {
                return ArticleListFragment.newInstance(
                        TrendArticleDb.Article.GENRE_ID_RECOMEND);
            } else {
                return ArticleListFragment.newInstance(
                        mList.get(position).getGenreId());
            }
        }

        /** 表示するページ数を返却 */
        @Override
        public int getCount() {
            return mItemCount;
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param fm FragmentManagerオブジェクト
         * @param list 表示するジャンルのListオブジェクト
         * @param hasAccessToken アクセストークンを保持している場合はtrue
         */
        ArticleListFragmentAdapter(FragmentManager fm,
                List<CurationGenreData> list, boolean hasAccessToken) {
            super(fm);
            this.mList = list;
            mItemCount = (list.size() + (hasAccessToken ? 1 : 0));
        }

    }

    void initialize(Locale locale) {

        setContentView(R.layout.activity_main);

        // APIキーを設定して認証クラスを初期化
        AuthApiKey.initializeAuth(API_KEY);

        // ViewPagerの取得と設定
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);

        // ActionBarの初期設定
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.title_main);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ((View) findViewById(android.R.id.home).getParent()).setVisibility(View.GONE);

        // GenreLoader起動
        mForceUpdate = false;
        getLoaderManager().restartLoader(0, null, this);
    }

}

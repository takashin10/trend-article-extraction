/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.activity;

import java.util.ArrayList;
import java.util.List;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import jp.ne.docomo.smt.dev.webcuration.data.CurationArticleContentsData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentsResultData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationImageSizeData;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.loader.BaseLoader;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.loader.SearchLoader;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider.TrendArticleDb;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.service.ArticleStateUpdateService;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.service.LogDataSendingService;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * キーワード検索結果一覧をListViewで表示するFragment
 * KeywordSearchActivity内のViewPagerの子要素として生成される
 */
public class ResultListFragment extends Fragment
        implements LoaderCallbacks<CurationContentsResultData>,
        OnItemClickListener, OnScrollListener, TextWatcher, OAuthCallback {

    // field
    // ----------------------------------------------------------------

    private static final String ARG_GENRE_ID = "arg_genre_id";
    private static final String ARG_KEYWORD = "arg_keyword";

    private ImageView mShadowView;
    private ResultListAdapter mAdapter;
    private ReadStateObserver mObserver;

    // method
    // ----------------------------------------------------------------

    /**
     * ResultListFragmentインスタンスを新規に生成するファクトリメソッド
     * 
     * @param genreId 取得する検索結果一覧のジャンルID
     * @param keyword 検索対象となるキーワード文字列
     * @return 新規に生成されたResultListFragmentインスタンス
     */
    static ResultListFragment newInstance(int genreId, String keyword) {
        Bundle args = new Bundle();
        args.putInt(ARG_GENRE_ID, genreId);
        args.putString(ARG_KEYWORD, keyword);
        ResultListFragment instance = new ResultListFragment();
        instance.setArguments(args);
        return instance;
    }

    /** Loaderが生成される際に呼び出されるコールバック */
    @Override
    public Loader<CurationContentsResultData> onCreateLoader(int id, Bundle args) {
        return new SearchLoader(getActivity(),
                args.getInt(ARG_GENRE_ID, TrendArticleDb.Article.GENRE_ID_SEARCH_ALL),
                args.getString(ARG_KEYWORD, null));
    }

    /** Loaderの処理が完了した際に呼び出されるコールバック */
    @Override
    public void onLoadFinished(
            Loader<CurationContentsResultData> loader, CurationContentsResultData data) {
        mAdapter.clear();

        // アクセストークンが有効期限切れの場合はリフレッシュする
        if (((BaseLoader<?>) loader).isAccessTokenExpired()) {
            AccessTokenManager.getInstance(getActivity()).startRefresh(this);
            return;
        }

        if (data == null) {
            return;
        }
        ArrayList<CurationArticleContentsData> list =
                data.getCurationArticleContentsDataList();
        if (list == null) {
            return;
        }
        mAdapter.addAll(list);
    }

    /** Loaderがリセットされる際に呼び出されるコールバック */
    @Override
    public void onLoaderReset(Loader<CurationContentsResultData> loader) {
        onLoadFinished(loader, null);
    }

    /** ListView内の項目がクリックされた際に呼び出されるコールバック */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 記事コンテンツIDとリンクURLを取得
        Context context = getActivity();
        CurationArticleContentsData data =
                (CurationArticleContentsData) parent.getItemAtPosition(position);
        CurationContentData content = data.getCurationContentData();
        if (content != null) {
            String url = content.getLinkUrl();
            if (!TextUtils.isEmpty(url)) {
                // ブラウザアプリへIntentでリンクURLを伝送する
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                // 記事コンテンツの未読・既読状態を既読に設定する
                long contentId = data.getContentId();
                ArticleStateUpdateService.startService(context, contentId, true);

                // ログデータを収集・送信
                int genreId = getArguments().getInt(ARG_GENRE_ID,
                        TrendArticleDb.Article.GENRE_ID_SEARCH_ALL);
                String keyword = getArguments().getString(ARG_KEYWORD, null);
                LogDataSendingService.insertLogData(context, contentId,
                        data.getContentType(), genreId, url, keyword);
                LogDataSendingService.startService(context);
                return;
            }
        }
        // 記事URLを取得できませんでした
        Toast.makeText(context,
                R.string.msg_article_url_empty,
                Toast.LENGTH_SHORT).show();
    }

    /** ListViewのスクロール操作状態が変化した際に呼び出されるコールバック */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /** NOOP */
    }

    /** ListViewのスクロール座標が変化した際に呼び出されるコールバック */
    @Override
    public void onScroll(AbsListView view,
            int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem <= 0) {
            View child = view.getChildAt(0);
            if ((child == null) || (child.getTop() >= 0)) {
                mShadowView.setVisibility(View.GONE);
                return;
            }
        }
        mShadowView.setVisibility(View.VISIBLE);
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
        Bundle args = getArguments();
        String keyword = s.toString();
        if (!keyword.equals(args.getString(ARG_KEYWORD))) {
            args.putString(ARG_KEYWORD, keyword);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
        if (BuildConfig.DEBUG) {
            Log.d(ResultListFragment.class.getSimpleName(), "OAuth has been Refreshed.");
        }
        if (isAdded()) {
            getLoaderManager().restartLoader(0, getArguments(), this);
        }
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
    }

    /** FragmentのViewが生成される際に呼び出されるコールバック */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        // 各種Viewを取得
        mShadowView = (ImageView) view.findViewById(R.id.img_list_shadow);
        TextView emptyView = (TextView) view.findViewById(R.id.txt_list_empty);
        emptyView.setText(R.string.txt_result_list_empty);

        // ListViewの取得と設定
        mAdapter = new ResultListAdapter(getActivity(),
                new ArrayList<CurationArticleContentsData>());
        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        listView.setEmptyView(emptyView);
        listView.setAdapter(mAdapter);
        return view;
    }

    /** FragmentのViewが破棄される際に呼び出されるコールバック */
    @Override
    public void onDestroyView() {
        // Loaderを破棄
        getLoaderManager().destroyLoader(0);

        // 各種リスナを解放
        ListView listView = (ListView) getView().findViewById(R.id.list);
        listView.setOnItemClickListener(null);
        listView.setOnScrollListener(null);

        // 各種メンバを解放
        mShadowView = null;
        mAdapter = null;
        super.onDestroyView();
    }

    /** FragmentがアタッチされたActivityが生成された際に呼び出されるコールバック */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, getArguments(), this);
    }

    /** Fragmentの処理が開始される際に呼び出されるコールバック */
    @Override
    public void onStart() {
        super.onStart();

        // ActionBar上のEditTextに対して検索キーワードの変更検知リスナとしてこのFragmentを追加する
        ((EditText) getActivity().getActionBar().getCustomView()
                .findViewById(R.id.edit_search_keyword)).addTextChangedListener(this);

        // 未読・既読状態の更新を検知するContentObserverを生成して登録
        mObserver = new ReadStateObserver(new Handler(), mAdapter);
        getActivity().getContentResolver().registerContentObserver(
                TrendArticleDb.ArticleState.CONTENT_URI, true, mObserver);
    }

    /** Fragmentの処理が終了される際に呼び出されるコールバック */
    @Override
    public void onStop() {
        // ContentObserverの登録を解除して破棄
        getActivity().getContentResolver().unregisterContentObserver(mObserver);
        mObserver = null;

        // ActionBar上のEditTextに検索キーワードの変更検知リスナとして追加したこのFragmentを削除する
        ((EditText) getActivity().getActionBar().getCustomView()
                .findViewById(R.id.edit_search_keyword)).removeTextChangedListener(this);
        super.onStop();
    }

    // class
    // ----------------------------------------------------------------

    /** SDKから取得したキーワード検索結果データをListViewに紐付けるArrayAdapter */
    private static class ResultListAdapter extends ArrayAdapter<CurationArticleContentsData> {

        // field
        // ----------------------------------------------------------------

        private static final String SELECTION = ("( "
                + TrendArticleDb.ArticleState.KEY_CONTENT_ID + " == ? ) AND ( "
                + TrendArticleDb.ArticleState.KEY_READ_STATE + " == "
                + TrendArticleDb.ArticleState.READ_STATE_ALREADY_READ + " )");

        private static final String[] SELECTION_ARGS = new String[1];

        private final LayoutInflater mInflater;
        private final ContentResolver mResolver;

        // method
        // ----------------------------------------------------------------

        /** Viewにデータを紐付ける */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Viewがnullの場合は生成
            ArticleItemViewHolder holder;
            Context context = getContext();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_article, parent, false);
                holder = new ArticleItemViewHolder(context, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ArticleItemViewHolder) convertView.getTag();
            }

            // デフォルト値を設定
            int readState = TrendArticleDb.ArticleState.READ_STATE_UNREAD;
            int contentType = 0;
            int imageWidth = 0;
            int imageHeight = 0;
            String title = null;
            String body = null;
            String imageUrl = null;
            String sourceName = null;
            long createdDate = 0;

            // バインド対象のデータを取得
            CurationArticleContentsData article = getItem(position);

            // 未読・既読状態を取得
            SELECTION_ARGS[0] = Long.toString(article.getContentId());
            Cursor cursor = mResolver.query(
                    TrendArticleDb.ArticleState.CONTENT_URI,
                    null, SELECTION, SELECTION_ARGS, null);
            if ((cursor != null) && !cursor.isClosed()) {
                try {
                    readState = ((cursor.getCount() > 0)
                            ? TrendArticleDb.ArticleState.READ_STATE_ALREADY_READ
                            : TrendArticleDb.ArticleState.READ_STATE_UNREAD);
                } finally {
                    cursor.close();
                }
            }

            contentType = article.getContentType();
            CurationContentData data = article.getCurationContentData();
            if (data != null) {
                title = data.getTitle();
                body = data.getBody();
                imageUrl = data.getImageUrl();
                sourceName = data.getSourceName();
                try {
                    createdDate = data.getCreatedDateOnCalendar().getTimeInMillis();
                } catch (SdkException e) {
                    createdDate = 0;
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                CurationImageSizeData size = data.getCurationImageSizeData();
                if (size != null) {
                    imageWidth = size.getWidth();
                    imageHeight = size.getHeight();
                }
            }

            // データをViewにバインド
            holder.bind(context, readState, contentType, title, body,
                    imageWidth, imageHeight, imageUrl, sourceName, createdDate);
            return convertView;
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param context Contextオブジェクト
         * @param objects Viewに紐付けるデータオブジェクトのリスト
         */
        ResultListAdapter(Context context, List<CurationArticleContentsData> objects) {
            super(context, 0, objects);
            mInflater = LayoutInflater.from(context);
            mResolver = context.getContentResolver();
        }

    }

    /** キーワード検索結果データが閲覧され既読状態となった際にResultAdapterに更新を促すContentObserver */
    private static class ReadStateObserver extends ContentObserver {

        // field
        // ----------------------------------------------------------------

        private final ResultListAdapter mAdapter;

        // method
        // ----------------------------------------------------------------

        /** ContentProviderが管理するデータに変更があった際に呼び出されるコールバック */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mAdapter.notifyDataSetChanged();
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param handler コールバックをポストするHandlerオブジェクト
         * @param adapter 表示更新を行うResultListAdapterオブジェクト
         */
        ReadStateObserver(Handler handler, ResultListAdapter adapter) {
            super(handler);
            this.mAdapter = adapter;
        }

    }

}

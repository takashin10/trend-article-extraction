/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.activity;

import jp.ne.docomo.smt.dev.webcuration.sample_app.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app.provider.TrendArticleDb;
import jp.ne.docomo.smt.dev.webcuration.sample_app.service.ArticleStateUpdateService;
import jp.ne.docomo.smt.dev.webcuration.sample_app.service.LogDataSendingService;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/** 記事一覧をListViewで表示するFragment MainActivity内のViewPagerの子要素として生成される */
public class ArticleListFragment extends Fragment implements
        LoaderCallbacks<Cursor>, OnItemClickListener, OnScrollListener {

    // field
    // ----------------------------------------------------------------

    private static final String ARG_GENRE_ID = "arg_genre_id";

    private ImageView mShadowView;
    private ArticleListAdapter mAdapter;

    // method
    // ----------------------------------------------------------------

    /**
     * ArticleListFragmentインスタンスを新規に生成するファクトリメソッド
     * 
     * @param genreId 取得する記事一覧のジャンルID
     * @return 新規に生成されたArticleListFragmentインスタンス
     */
    static ArticleListFragment newInstance(int genreId) {
        Bundle args = new Bundle();
        args.putInt(ARG_GENRE_ID, genreId);
        ArticleListFragment instance = new ArticleListFragment();
        instance.setArguments(args);
        return instance;
    }

    /** Loaderが生成される際に呼び出されるコールバック */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), TrendArticleDb.Article.CONTENT_URI, null,
                (TrendArticleDb.Article.KEY_GENRE_ID + " == " + args.getInt(ARG_GENRE_ID)),
                null, null);
    }

    /** Loaderの処理が完了した際に呼び出されるコールバック */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor oldCursor = mAdapter.swapCursor(data);
        if ((oldCursor != null) && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /** Loaderがリセットされる際に呼び出されるコールバック */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }

    /** ListView内の項目がクリックされた際に呼び出されるコールバック */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        String url = cursor.getString(cursor.getColumnIndex(TrendArticleDb.Article.KEY_LINK_URL));
        if (!TextUtils.isEmpty(url)) {
            Context context = getActivity();

            // ブラウザアプリへIntentでリンクURLを伝送する
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

            // 記事コンテンツの未読・既読状態を既読に設定する
            long contentId = cursor.getLong(
                    cursor.getColumnIndex(TrendArticleDb.Article.KEY_CONTENT_ID));
            ArticleStateUpdateService.startService(context, contentId, true);

            // ログデータを収集・送信
            LogDataSendingService.insertLogData(context, contentId,
                    cursor.getInt(cursor.getColumnIndex(TrendArticleDb.Article.KEY_CONTENT_TYPE)),
                    getArguments().getInt(ARG_GENRE_ID), url, null);
            LogDataSendingService.startService(context);
        } else {
            // 記事URLを取得できませんでした
            Toast.makeText(getActivity(),
                    R.string.msg_article_url_empty,
                    Toast.LENGTH_SHORT).show();
        }
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

    /** FragmentのViewが生成される際に呼び出されるコールバック */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        // 各種Viewを取得
        mShadowView = (ImageView) view.findViewById(R.id.img_list_shadow);
        TextView emptyView = (TextView) view.findViewById(R.id.txt_list_empty);
        emptyView.setText(R.string.txt_article_list_empty);

        // ListViewの取得と設定
        mAdapter = new ArticleListAdapter(getActivity(), null);
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

    // class
    // ----------------------------------------------------------------

    /** アプリ内DB上の記事コンテンツデータをListViewに紐付けるCursorAdapter */
    private static class ArticleListAdapter extends CursorAdapter {

        // field
        // ----------------------------------------------------------------

        private final LayoutInflater mInflater;

        private int mIdxReadState = 0;
        private int mIdxContentType = 0;
        private int mIdxTitle = 0;
        private int mIdxBody = 0;
        private int mIdxImageWidth = 0;
        private int mIdxImageHeight = 0;
        private int mIdxImageUrl = 0;
        private int mIdxSourceName = 0;
        private int mIdxCreatedDate = 0;

        // method
        // ----------------------------------------------------------------

        /** Adapterが提供するViewを新規に生成して返却する */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.item_article, parent, false);
            view.setTag(new ArticleItemViewHolder(context, view));
            return view;
        }

        /** ViewにCursorが保持するデータを紐付ける */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((ArticleItemViewHolder) view.getTag()).bind(context,
                    cursor.getInt(mIdxReadState), cursor.getInt(mIdxContentType),
                    cursor.getString(mIdxTitle), cursor.getString(mIdxBody),
                    cursor.getInt(mIdxImageWidth), cursor.getInt(mIdxImageHeight),
                    cursor.getString(mIdxImageUrl), cursor.getString(mIdxSourceName),
                    cursor.getLong(mIdxCreatedDate));
        }

        /** ListViewに接続するCursorを差し替える */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            updateColumnIndexes(newCursor);
            return super.swapCursor(newCursor);
        }

        /**
         * DBカラムのインデックスをCursorから取得して更新する
         * 
         * @param cursor インデックスを取得するCursorオブジェクト
         */
        private void updateColumnIndexes(Cursor cursor) {
            if (cursor == null) {
                return;
            }
            mIdxReadState = cursor.getColumnIndex(TrendArticleDb.ArticleState.KEY_READ_STATE);
            mIdxContentType = cursor.getColumnIndex(TrendArticleDb.Article.KEY_CONTENT_TYPE);
            mIdxTitle = cursor.getColumnIndex(TrendArticleDb.Article.KEY_TITLE);
            mIdxBody = cursor.getColumnIndex(TrendArticleDb.Article.KEY_BODY);
            mIdxImageWidth = cursor.getColumnIndex(TrendArticleDb.Article.KEY_IMAGE_WIDTH);
            mIdxImageHeight = cursor.getColumnIndex(TrendArticleDb.Article.KEY_IMAGE_HEIGHT);
            mIdxImageUrl = cursor.getColumnIndex(TrendArticleDb.Article.KEY_IMAGE_URL);
            mIdxSourceName = cursor.getColumnIndex(TrendArticleDb.Article.KEY_SOURCE_NAME);
            mIdxCreatedDate = cursor.getColumnIndex(TrendArticleDb.Article.KEY_CREATED_DATE);
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param context Contextオブジェクト
         * @param c Viewに紐付けるデータを保持したCursorオブジェクト
         */
        ArticleListAdapter(Context context, Cursor c) {
            super(context, c, false);
            mInflater = LayoutInflater.from(context);
            updateColumnIndexes(c);
        }

    }

}

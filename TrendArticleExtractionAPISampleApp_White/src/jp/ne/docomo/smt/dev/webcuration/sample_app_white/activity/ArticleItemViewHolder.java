/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider.TrendArticleDb;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/** 記事一覧およびキーワード検索結果一覧でListView内に表示する項目のViewHolderクラス */
class ArticleItemViewHolder implements OnGlobalLayoutListener {

    // field
    // ----------------------------------------------------------------

    private static final int CONTENT_TYPE_RECOMEND = 11;

    private final int mDefaultRootPadB;
    private final int mLongImgRootPadB;

    private final int mDefaultRecomendMarginR;
    private final int mWithImgRecomendMarginR;
    private final int mRecomendMarginT;

    private final View mArticleRoot;
    private final View mProgressLayout;
    private final ImageView mDefaultImageView;
    private final ImageView mLongImgImageView;
    private final ImageView mRecomendView;
    private final TextView mSourceView;
    private final TextView mTitleView;
    private final TextView mBodyView;

    private final SimpleDateFormat mDateFormat;

    private String mImageUrl = null;
    private ImageLoadTask mTask = null;

    // method
    // ----------------------------------------------------------------

    /** Viewの描画が完了した際に呼び出されるコールバック */
    @Override
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void onGlobalLayout() {
        // 記事タイトルの表示行数によって記事本文の配置と最大表示行数を変更
        LayoutParams params = (LayoutParams) mBodyView.getLayoutParams();
        if (mTitleView.getLineCount() > 1) {
            params.addRule(RelativeLayout.LEFT_OF, RelativeLayout.NO_ID);
        } else {
            params.addRule(RelativeLayout.LEFT_OF, R.id.img_article_default_image);
        }
        mBodyView.setLayoutParams(params);

        // Viewの描画完了を検知するためリスナを解除
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mTitleView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            mTitleView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    /**
     * 記事画像の読み込みが完了した際に呼び出されるコールバック
     * 
     * @param imageView 記事画像を表示するImageView
     * @param drawable 読み込まれた記事画像のDrawableオブジェクト
     */
    void onImageLoaded(ImageView imageView, Drawable drawable) {
        mProgressLayout.setVisibility(View.GONE);
        imageView.setImageDrawable(drawable);
        if (drawable == null) {
            imageView.setVisibility(View.GONE);
            if (mRecomendView.getVisibility() == View.VISIBLE) {
                LayoutParams recomendParams = new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                recomendParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                recomendParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                recomendParams.setMargins(0, mRecomendMarginT, mDefaultRecomendMarginR, 0);
                mRecomendView.setLayoutParams(recomendParams);
            }
        }
    }

    /**
     * 各Viewにデータをバインド
     * 
     * @param context Contextオブジェクト
     * @param readState 未読・既読状態
     * @param contentType 記事コンテンツ種別
     * @param title 記事コンテンツタイトル
     * @param body 記事コンテンツ本文
     * @param imageWidth 記事コンテンツ画像の横幅
     * @param imageHeight 記事コンテンツ画像の高さ
     * @param imageUrl 記事コンテンツ画像URL
     * @param sourceName 配信元名
     * @param createdDate 記事コンテンツ作成日時
     */
    void bind(Context context, int readState, int contentType, String title, String body,
            int imageWidth, int imageHeight, String imageUrl, String sourceName, long createdDate) {
        // Viewの描画完了を検知するためリスナを設定
        mTitleView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        // 画像の読込処理をキャンセル
        if ((mTask != null) && !mTask.isCancelled()) {
            mTask.cancel(true);
        }

        // 記事の未読・既読状態を設定
        if (readState == TrendArticleDb.ArticleState.READ_STATE_ALREADY_READ) {
            // 既読
            mArticleRoot.setBackgroundResource(R.drawable.bg_article_already_read);
            mSourceView.setTextColor(0x3f000000);
            mTitleView.setTextColor(0x8c000000);
            mBodyView.setTextColor(0x66000000);
        } else {
            // 未読
            mArticleRoot.setBackgroundResource(R.drawable.bg_article_unread);
            mSourceView.setTextColor(0xaf000000);
            mTitleView.setTextColor(0xff000000);
            mBodyView.setTextColor(0xcc000000);
        }

        // 記事作成日時と配信元名を設定
        mSourceView.setText(context.getString(
                R.string.txt_article_source_format,
                mDateFormat.format(new Date(createdDate)), sourceName));

        // タイトルと本文を設定
        mTitleView.setText(title);
        mBodyView.setText(body);

        // 記事画像が指定されているかどうか
        boolean hasImage = ((imageWidth > 0) && (imageHeight > 0) && !TextUtils.isEmpty(imageUrl));

        // 記事画像が縦横比1:2以上の横長画像かどうか
        boolean isImageLong = (hasImage && ((imageHeight * 2) <= imageWidth));

        // レコメンドマークの表示と配置
        if (contentType == CONTENT_TYPE_RECOMEND) {
            LayoutParams recomendParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            recomendParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            if (!hasImage || isImageLong) {
                // カード右上に表示する
                recomendParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                recomendParams.setMargins(0, mRecomendMarginT, mDefaultRecomendMarginR, 0);
            } else {
                // 記事画像の左側に表示する
                recomendParams.addRule(RelativeLayout.LEFT_OF, R.id.img_article_default_image);
                recomendParams.setMargins(0, mRecomendMarginT, mWithImgRecomendMarginR, 0);
            }
            mRecomendView.setLayoutParams(recomendParams);
            mRecomendView.setVisibility(View.VISIBLE);
        } else {
            mRecomendView.setVisibility(View.GONE);
        }

        // 記事画像の配置設定と読み込み
        if (isImageLong) {
            // 縦横比1:2以上の横長画像
            mArticleRoot.setPadding(0, 0, 0, mLongImgRootPadB);
            mBodyView.setVisibility(View.GONE);
            mLongImgImageView.setVisibility(View.VISIBLE);
            mDefaultImageView.setVisibility(View.GONE);
            mDefaultImageView.setImageDrawable(null);
            mTitleView.setMaxLines(2);

            // 記事画像の読み込みを開始
            startImageLoading(mLongImgImageView, imageUrl);
        } else {
            mArticleRoot.setPadding(0, 0, 0, mDefaultRootPadB);
            mBodyView.setVisibility(View.VISIBLE);
            mLongImgImageView.setVisibility(View.GONE);
            mLongImgImageView.setImageDrawable(null);
            mTitleView.setMaxLines(2);
            if (hasImage) {
                // 通常の記事画像
                mDefaultImageView.setVisibility(View.VISIBLE);

                // 記事画像の読み込みを開始
                startImageLoading(mDefaultImageView, imageUrl);
            } else {
                // 記事画像無し
                mProgressLayout.setVisibility(View.GONE);
                mDefaultImageView.setVisibility(View.GONE);
                mDefaultImageView.setImageDrawable(null);
                this.mImageUrl = null;
            }
        }
    }

    /**
     * 記事画像の読み込み処理を開始
     * 
     * @param imageView 記事画像を表示するImageView
     * @param imageUrl 記事画像読み込み先となるURL
     */
    private void startImageLoading(ImageView imageView, String imageUrl) {
        if (!imageUrl.equals(this.mImageUrl) || (imageView.getDrawable() == null)) {
            int imageViewId = imageView.getId();
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_LEFT, imageViewId);
            params.addRule(RelativeLayout.ALIGN_RIGHT, imageViewId);
            params.addRule(RelativeLayout.ALIGN_TOP, imageViewId);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, imageViewId);
            mProgressLayout.setLayoutParams(params);
            mProgressLayout.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(null);

            // 画像の読み込み処理を開始
            mTask = new ImageLoadTask(this, imageView, imageUrl);
            mTask.execute((Void) null);
            this.mImageUrl = imageUrl;
        }
    }

    // constructor
    // ----------------------------------------------------------------

    /**
     * コンストラクタ
     * 
     * @param context Contextオブジェクト
     * @param root データをバインドする各Viewのルート
     */
    ArticleItemViewHolder(Context context, View root) {
        Resources res = context.getResources();

        // 記事画像サイズによって使い分けるカード下部のパディング
        mDefaultRootPadB = res.getDimensionPixelSize(
                R.dimen.rltv_article_default_root_pad_b);
        mLongImgRootPadB = res.getDimensionPixelSize(
                R.dimen.rltv_article_long_img_root_pad_b);

        // 記事画像サイズによって使い分けるレコメンドマーク右側のマージン
        mDefaultRecomendMarginR = res.getDimensionPixelSize(
                R.dimen.img_article_default_recomend_margin_r);
        mWithImgRecomendMarginR = res.getDimensionPixelSize(
                R.dimen.img_article_with_img_recomend_margin_r);

        // レコメンドマーク上部のマージン
        mRecomendMarginT = res.getDimensionPixelSize(
                R.dimen.img_article_recomend_margin_t);

        // バインド対象のView
        mArticleRoot = root.findViewById(R.id.rltv_article_root);
        mProgressLayout = root.findViewById(R.id.frm_article_progress);
        mDefaultImageView = (ImageView) root.findViewById(R.id.img_article_default_image);
        mLongImgImageView = (ImageView) root.findViewById(R.id.img_article_long_img_image);
        mRecomendView = (ImageView) root.findViewById(R.id.img_article_recomend);
        mSourceView = (TextView) root.findViewById(R.id.txt_article_source);
        mTitleView = (TextView) root.findViewById(R.id.txt_article_title);
        mBodyView = (TextView) root.findViewById(R.id.txt_article_body);

        // 記事コンテンツ作成日時フォーマット
        mDateFormat = new SimpleDateFormat(
                context.getString(R.string.txt_article_date_format),
                Locale.getDefault());
    }

    // class
    // ----------------------------------------------------------------

    /** 記事一覧およびキーワード検索結果一覧でListView内に表示する項目の画像データをバックグラウンドで読み込むAsyncTask */
    private static class ImageLoadTask extends AsyncTask<Void, Integer, Drawable> {

        // field
        // ----------------------------------------------------------------

        private final String mUrlSpec;
        private final ImageView mImageView;
        private final ArticleItemViewHolder mHolder;

        // method
        // ----------------------------------------------------------------

        /** ワーキングスレッド上で実行される非同期処理 */
        @Override
        protected Drawable doInBackground(Void... params) {
            InputStream stream = null;
            Drawable drawable = null;
            try {
                URL url = new URL(mUrlSpec);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                stream = connection.getInputStream();
                drawable = Drawable.createFromStream(stream, url.getFile());
            } catch (MalformedURLException e) {
                drawable = null;
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (SocketTimeoutException e) {
                drawable = null;
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                drawable = null;
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return drawable;
        }

        /** 非同期処理が実行された後に呼び出されるコールバック */
        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            mHolder.onImageLoaded(mImageView, result);
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param holder ArticleItemViewHolderオブジェクト
         * @param imageView 読み込んだ画像を表示するImageView
         * @param urlSpec 読み込む画像のURL文字列
         */
        ImageLoadTask(ArticleItemViewHolder holder,
                ImageView imageView, String urlSpec) {
            this.mUrlSpec = urlSpec;
            this.mImageView = imageView;
            this.mHolder = holder;
        }
    }

}

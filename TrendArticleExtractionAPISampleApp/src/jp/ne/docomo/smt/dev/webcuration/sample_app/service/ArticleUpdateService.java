/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.service;

import java.util.Locale;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import jp.ne.docomo.smt.dev.webcuration.CurationContents;
import jp.ne.docomo.smt.dev.webcuration.CurationContentsAuthenticated;
import jp.ne.docomo.smt.dev.webcuration.CurationRecommend;
import jp.ne.docomo.smt.dev.webcuration.constants.Lang;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentsResultData;
import jp.ne.docomo.smt.dev.webcuration.param.CurationContentsAuthenticatedRequestParam;
import jp.ne.docomo.smt.dev.webcuration.param.CurationRecommendRequestParam;
import jp.ne.docomo.smt.dev.webcuration.sample_app.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app.provider.TrendArticleDb;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/** 記事コンテンツデータを非同期に更新するIntentService */
public class ArticleUpdateService extends IntentService implements OAuthCallback {

    // field
    // ----------------------------------------------------------------

    private static final String TAG = ArticleUpdateService.class.getSimpleName();

    private static final int ARTICLES_NUMBER = 50;

    private static final String EX_GENRE_ID = "ex_genre_id";
    private static final String EX_FORCE_UPDATE = "ex_force_update";

    private final Handler mHandler;

    private boolean mForceUpdate = false;
    private int mGenreId = 0;

    // method
    // ----------------------------------------------------------------

    /**
     * ArticleUpdateServiceを起動する
     * 
     * @param context Contextオブジェクト
     * @param genreId 更新を行うジャンルID
     * @param forceUpdate trueの場合記事の作成日時を問わず強制的にDBを更新する
     */
    public static void startService(Context context, int genreId, boolean forceUpdate) {
        context.startService(new Intent(context, ArticleUpdateService.class)
                .putExtra(EX_GENRE_ID, genreId).putExtra(EX_FORCE_UPDATE, forceUpdate));
    }

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "OAuth has been Refreshed.");
        }
        startService(this, mGenreId, mForceUpdate);
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

        // 記事の更新に失敗した旨をトーストで表示
        mHandler.post(new Runnable() {

            /** UIスレッドにポストされるトースト表示処理 */
            @Override
            public void run() {
                Toast.makeText(ArticleUpdateService.this,
                        R.string.msg_article_err_update,
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    /** ワーキングスレッド上で実行される処理 */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        // Intentに格納された付加情報を取得
        mGenreId = intent.getIntExtra(EX_GENRE_ID, 0);
        mForceUpdate = intent.getBooleanExtra(EX_FORCE_UPDATE, false);

        // アクセストークンを取得
        String accessToken = AccessTokenManager
                .getInstance(this).getAccessToken();

        // 記事コンテンツを取得
        boolean isAccessTokenExpired = false;
        CurationContentsResultData data = null;
        try {
            if (mGenreId == TrendArticleDb.Article.GENRE_ID_RECOMEND) {
                // 端末の言語設定を使用
                CurationRecommendRequestParam param =
                        new CurationRecommendRequestParam();
                param.setLang(getLanguage());

                // 記事取得件数を設定
                param.setNumber(ARTICLES_NUMBER);

                // レコメンド記事コンテンツを取得
                data = new CurationRecommend().request(param, accessToken);
            } else {
                // 端末の言語設定を使用
                CurationContentsAuthenticatedRequestParam param =
                        new CurationContentsAuthenticatedRequestParam();
                param.setLang(getLanguage());

                // ジャンルIDと記事取得件数を設定
                param.setGenreId(mGenreId);
                param.setNumber(ARTICLES_NUMBER);

                // ジャンルIDを指定して記事コンテンツを取得
                if (TextUtils.isEmpty(accessToken)) {
                    data = new CurationContents().request(param);
                } else {
                    data = new CurationContentsAuthenticated().request(param, accessToken);
                }
            }
        } catch (SdkException e) {
            data = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (ServerException e) {
            data = null;
            isAccessTokenExpired = AccessTokenManager.ERROR_CODE_EXPIRATION
                    .equals(e.getErrorCode());
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

        }

        // DB上の記事コンテンツを更新
        if (isAccessTokenExpired) {
            // アクセストークン有効期限切れの場合は再認証処理を開始
            AccessTokenManager.getInstance(this).startRefresh(this);
        } else if (!TrendArticleDb.Article.updateArticles(this, mGenreId, data, mForceUpdate)) {
            // 記事の更新に失敗した旨をトーストで表示
            mHandler.post(new Runnable() {

                /** UIスレッドにポストされるトースト表示処理 */
                @Override
                public void run() {
                    Toast.makeText(ArticleUpdateService.this,
                            R.string.msg_article_err_update,
                            Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    /**
     * 端末の言語設定からトレンド記事抽出APIに設定する言語設定を取得
     * 
     * @return トレンド記事抽出APIに設定する言語設定
     */
    private Lang getLanguage() {
        if (Locale.JAPAN.equals(Locale.getDefault())) {
            return Lang.JP;
        } else {
            return Lang.EN;
        }
    }

    // constructor
    // ----------------------------------------------------------------

    /** コンストラクタ */
    public ArticleUpdateService() {
        super(TAG);
        mHandler = new Handler();
    }

}

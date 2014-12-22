/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.loader;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.webcuration.CurationSearch;
import jp.ne.docomo.smt.dev.webcuration.CurationSearchAuthenticated;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentsResultData;
import jp.ne.docomo.smt.dev.webcuration.param.CurationSearchAuthenticatedRequestParam;
import jp.ne.docomo.smt.dev.webcuration.sample_app.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app.provider.TrendArticleDb;
import android.content.Context;
import android.text.TextUtils;

/** バックグラウンドでSDKからキーワード検索結果を取得するAsyncTaskLoader */
public class SearchLoader extends BaseLoader<CurationContentsResultData> {

    // field
    // ----------------------------------------------------------------

    private static final int ARTICLES_NUMBER = 50;

    private final int mGenreId;
    private final String mKeyword;

    // method
    // ----------------------------------------------------------------

    /** ワーキングスレッド上で実行されるロード処理 */
    @Override
    public CurationContentsResultData loadInBackground() {
        // キーワードが空の場合はnullを返す
        if (TextUtils.isEmpty(mKeyword)) {
            return null;
        }

        // 端末の言語設定を使用
        CurationSearchAuthenticatedRequestParam param =
                new CurationSearchAuthenticatedRequestParam();
        param.setLang(getLanguage());

        // ジャンルIDと記事取得件数、キーワードを設定
        if (mGenreId != TrendArticleDb.Article.GENRE_ID_SEARCH_ALL) {
            param.setGenreId(mGenreId);
        }
        param.setNumber(ARTICLES_NUMBER);
        param.setSearchKeyword(mKeyword);

        // アクセストークンを取得
        String accessToken = AccessTokenManager
                .getInstance(getContext()).getAccessToken();

        // 検索結果を取得
        CurationContentsResultData data = null;
        try {
            if (TextUtils.isEmpty(accessToken)) {
                data = new CurationSearch().request(param);
            } else {
                data = new CurationSearchAuthenticated().request(param, accessToken);
            }
        } catch (SdkException e) {
            data = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (ServerException e) {
            data = null;
            setAccessTokenExpired(
                    AccessTokenManager.ERROR_CODE_EXPIRATION.equals(e.getErrorCode()));
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return data;
    }

    // constructor
    // ----------------------------------------------------------------

    /**
     * コンストラクタ
     * 
     * @param context Contextオブジェクト
     * @param genreId 検索対象となるジャンルID
     * @param keyword 検索対象となるキーワード文字列
     */
    public SearchLoader(Context context, int genreId, String keyword) {
        super(context);
        this.mGenreId = genreId;
        this.mKeyword = keyword;
    }

}

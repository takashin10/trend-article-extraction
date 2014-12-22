/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.loader;

import java.util.ArrayList;
import java.util.List;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.webcuration.CurationGenre;
import jp.ne.docomo.smt.dev.webcuration.CurationGenreAuthenticated;
import jp.ne.docomo.smt.dev.webcuration.constants.Lang;
import jp.ne.docomo.smt.dev.webcuration.data.CurationGenreData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationGenreResultData;
import jp.ne.docomo.smt.dev.webcuration.param.CurationGenreAuthenticatedRequestParam;
import jp.ne.docomo.smt.dev.webcuration.sample_app.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app.provider.TrendArticleDb;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

/** バックグラウンドでSDKからジャンル情報を取得するAsyncTaskLoader */
public class GenreLoader extends BaseLoader<List<CurationGenreData>> {

    // field
    // ----------------------------------------------------------------

    private static final String SELECTION = ("( "
            + TrendArticleDb.Genre.KEY_PARENT + " == " + TrendArticleDb.Genre.PARENT_ROOT
            + " ) AND ( " + TrendArticleDb.Genre.KEY_LANGUAGE + " == ? )");

    // method
    // ----------------------------------------------------------------

    /** ワーキングスレッド上で実行されるロード処理 */
    @Override
    public List<CurationGenreData> loadInBackground() {
        // 端末の言語設定を使用
        CurationGenreAuthenticatedRequestParam param =
                new CurationGenreAuthenticatedRequestParam();
        Lang language = getLanguage();
        param.setLang(language);

        // アクセストークンを取得
        String accessToken = AccessTokenManager
                .getInstance(getContext()).getAccessToken();

        // ジャンル情報を取得
        ArrayList<CurationGenreData> list = null;
        try {
            // ジャンル情報を取得
            CurationGenreResultData data = null;
            if (TextUtils.isEmpty(accessToken)) {
                data = new CurationGenre().request(param);
            } else {
                data = new CurationGenreAuthenticated().request(param, accessToken);
            }

            // ジャンル情報からジャンル一覧を取得
            if (data != null) {
                list = data.getCurationGenreDataList();
            }
        } catch (SdkException e) {
            list = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (ServerException e) {
            list = null;
            setAccessTokenExpired(
                    AccessTokenManager.ERROR_CODE_EXPIRATION.equals(e.getErrorCode()));
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        if ((list != null) && !list.isEmpty()) {
            // 取得したジャンル一覧でDB上のジャンル情報を更新
            TrendArticleDb.Genre.updateGenres(getContext(), language, list);
        } else {
            // DB上のジャンル情報からジャンル一覧を作成
            String[] selectionArgs = {
                    language.name()
            };
            Cursor cursor = getContext().getContentResolver().query(
                    TrendArticleDb.Genre.CONTENT_URI, null, SELECTION, selectionArgs, null);
            if ((cursor != null) && !cursor.isClosed()) {
                list = new ArrayList<CurationGenreData>();
                try {
                    for (boolean eof = cursor.moveToFirst(); eof; eof = cursor.moveToNext()) {
                        list.add(new StoredGenreData(cursor));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return list;
    }

    // constructor
    // ----------------------------------------------------------------

    /**
     * コンストラクタ
     * 
     * @param context Contextオブジェクト
     */
    public GenreLoader(Context context) {
        super(context);
    }

    // class
    // ----------------------------------------------------------------

    /**
     * ジャンルIDおよびジャンル名を独自に設定可能な拡張ジャンル情報格納クラス
     * SDKからのジャンル情報取得失敗時、アプリ内DB上のジャンル情報を取得した際に
     * SDKから取得した場合と同じCurationGenreDataのListで取り扱うため拡張してある
     */
    private static class StoredGenreData extends CurationGenreData {

        // field
        // ----------------------------------------------------------------

        private final int mGenreId;
        private final String mTitle;

        // method
        // ----------------------------------------------------------------

        /** DBから取得したジャンルIDを返却 */
        @Override
        public int getGenreId() {
            return mGenreId;
        }

        /** DBから取得したジャンル名を返却 */
        @Override
        public String getTitle() {
            return mTitle;
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param cursor DBから取得したジャンル情報が格納されたCursorオブジェクト
         */
        StoredGenreData(Cursor cursor) {
            mGenreId = cursor.getInt(cursor.getColumnIndex(TrendArticleDb.Genre.KEY_GENRE_ID));
            mTitle = cursor.getString(cursor.getColumnIndex(TrendArticleDb.Genre.KEY_TITLE));
        }

    }

}

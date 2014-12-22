/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.service;

import java.util.ArrayList;
import java.util.Calendar;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import jp.ne.docomo.smt.dev.webcuration.CurationSendLog;
import jp.ne.docomo.smt.dev.webcuration.param.CurationLogData;
import jp.ne.docomo.smt.dev.webcuration.param.CurationSendLogRequestParam;
import jp.ne.docomo.smt.dev.webcuration.sample_app.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app.oauth.AccessTokenManager;
import jp.ne.docomo.smt.dev.webcuration.sample_app.provider.TrendArticleDb;
import jp.ne.docomo.smt.dev.webcuration.sample_app.provider.TrendArticleProvider;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

/** 閲覧ログデータの保存処理および保存されたログデータの一括送信処理を非同期に実行するIntentService */
public class LogDataSendingService extends IntentService implements OAuthCallback {

    // field
    // ----------------------------------------------------------------

    private static final String TAG = LogDataSendingService.class.getSimpleName();
    private static final String SELECTION = (TrendArticleDb.LogData._ID + " == ?");

    private static final String EX_LOG_SENDING = "ex_log_sending";

    private static final String EX_CONTENT_ID = "ex_content_id";
    private static final String EX_CONTENT_TYPE = "ex_content_type";
    private static final String EX_GENRE_ID = "ex_genre_id";
    private static final String EX_URL = "ex_url";
    private static final String EX_KEYWORD = "ex_keyword";

    // method
    // ----------------------------------------------------------------

    /**
     * LogDataSendingServiceを起動する
     * 
     * @param context Contextオブジェクト
     */
    public static void startService(Context context) {
        context.startService(new Intent(context, LogDataSendingService.class)
                .putExtra(EX_LOG_SENDING, true));
    }

    /**
     * DBに送信予定ログデータを追加
     * 
     * @param context Contextオブジェクト
     * @param contentId 閲覧した記事コンテンツID
     * @param contentType 閲覧した記事コンテンツ種別
     * @param genreId 閲覧した記事コンテンツのジャンルID
     * @param url 閲覧した記事コンテンツの遷移先URL
     * @param keyword 記事コンテンツ閲覧時のキーワード
     */
    public static void insertLogData(Context context, long contentId,
            int contentType, int genreId, String url, String keyword) {
        context.startService(new Intent(context, LogDataSendingService.class)
                .putExtra(EX_LOG_SENDING, false).putExtra(EX_CONTENT_ID, contentId)
                .putExtra(EX_CONTENT_TYPE, contentType).putExtra(EX_GENRE_ID, genreId)
                .putExtra(EX_URL, url).putExtra(EX_KEYWORD, keyword));
    }

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "OAuth has been Refreshed.");
        }
        startService(this);
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

    /** ワーキングスレッド上で実行される処理 */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getBooleanExtra(EX_LOG_SENDING, true)) {
                // ログ送信
                sendAllLogData();
            } else {
                // ログ収集
                insertLogData(intent);
            }
        }
    }

    /** DB上のログデータを全て送信する */
    private void sendAllLogData() {
        // ログ送信時に使用するアクセストークンの取得
        String accessToken = AccessTokenManager.getInstance(this).getAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        // DBに格納されたログデータを全て取得
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(TrendArticleDb.LogData.CONTENT_URI, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return;
        }

        // DB上のIDとログデータのリストをそれぞれ用意
        ArrayList<Long> rowIdList = new ArrayList<Long>();
        ArrayList<CurationLogData> logDataList = new ArrayList<CurationLogData>();

        try {
            // 各カラムのインデックスを取得しておく
            int idxRowId = cursor.getColumnIndex(TrendArticleDb.LogData._ID);
            int idxContentId = cursor.getColumnIndex(TrendArticleDb.LogData.KEY_CONTENT_ID);
            int idxContentType = cursor.getColumnIndex(TrendArticleDb.LogData.KEY_CONTENT_TYPE);
            int idxDate = cursor.getColumnIndex(TrendArticleDb.LogData.KEY_DATE);
            int idxGenreId = cursor.getColumnIndex(TrendArticleDb.LogData.KEY_GENRE_ID);
            int idxKeyword = cursor.getColumnIndex(TrendArticleDb.LogData.KEY_KEYWORD);
            int idxUrl = cursor.getColumnIndex(TrendArticleDb.LogData.KEY_URL);

            // ログデータ発生日時設定用のCalendarオブジェクトを取得
            Calendar calendar = Calendar.getInstance();

            // CursorからDB上のIDを取得、およびログデータを生成して格納
            for (boolean eof = cursor.moveToFirst(); eof; eof = cursor.moveToNext()) {
                // DB上のIDの取得と格納
                rowIdList.add(cursor.getLong(idxRowId));

                // DB上のジャンルIDを取得
                int genreId = cursor.getInt(idxGenreId);

                // ログデータの発生日時を設定
                calendar.setTimeInMillis(cursor.getLong(idxDate));

                // ログデータの生成と格納
                CurationLogData logData = new CurationLogData();
                logData.setDate(calendar);
                if ((genreId != TrendArticleDb.Article.GENRE_ID_SEARCH_ALL)
                        && (genreId != TrendArticleDb.Article.GENRE_ID_RECOMEND)) {
                    // 「すべて」と「レコメンド」のジャンルIDは送信しない
                    logData.setGenreId(genreId);
                }
                logData.setContentId(cursor.getLong(idxContentId));
                logData.setContentType(cursor.getInt(idxContentType));
                logData.setKeyword(cursor.getString(idxKeyword));
                logData.setUrl(cursor.getString(idxUrl));
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Planned to be sent:"
                            + " Content ID=" + logData.getContentId()
                            + " Content Type=" + logData.getContentType()
                            + " Genre ID=" + logData.getGenreId()
                            + " Keyword=" + logData.getKeyword()
                            + "\nDate=" + logData.getDate().getTime()
                            + " URL=" + logData.getUrl());
                }
                logDataList.add(logData);
            }
        } finally {
            cursor.close();
        }

        // 送信するログデータが存在しない場合は処理を終了
        if (rowIdList.isEmpty() || logDataList.isEmpty()) {
            return;
        }

        // ログデータをサーバに送信
        boolean isAccessTokenExpired = false;
        CurationSendLogRequestParam param = new CurationSendLogRequestParam();
        param.setLogList(logDataList);
        try {
            new CurationSendLog().request(param, accessToken);

            // 送信に成功した場合はDB上のログデータを削除
            ArrayList<ContentProviderOperation> operations =
                    new ArrayList<ContentProviderOperation>();
            String[] selectionArgs = new String[1];
            for (long rowId : rowIdList) {
                selectionArgs[0] = Long.toString(rowId);
                operations.add(ContentProviderOperation
                        .newDelete(TrendArticleDb.LogData.CONTENT_URI)
                        .withSelection(SELECTION, selectionArgs).build());
            }
            ContentProviderResult[] results = resolver.applyBatch(
                    TrendArticleProvider.AUTHORITY, operations);
            if (BuildConfig.DEBUG) {
                int totalCount = 0;
                for (ContentProviderResult result : results) {
                    totalCount += result.count;
                }
                Log.d(TAG, "Sent Log Data:" + totalCount + "/" + results.length);
            }
        } catch (SdkException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (ServerException e) {
            isAccessTokenExpired = AccessTokenManager.ERROR_CODE_EXPIRATION
                    .equals(e.getErrorCode());
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (OperationApplicationException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        if (isAccessTokenExpired) {
            // アクセストークン有効期限切れの場合は再認証処理を開始
            AccessTokenManager.getInstance(this).startRefresh(this);
        }
    }

    /**
     * DBに送信予定ログデータを追加
     * 
     * @param intent 追加するログデータの情報を保持するIntentオブジェクト
     */
    private void insertLogData(Intent intent) {
        TrendArticleDb.LogData.insertLogData(this, intent.getLongExtra(EX_CONTENT_ID, 0),
                intent.getIntExtra(EX_CONTENT_TYPE, 0), intent.getIntExtra(EX_GENRE_ID, 0),
                intent.getStringExtra(EX_URL), intent.getStringExtra(EX_KEYWORD));
    }

    // method
    // ----------------------------------------------------------------

    /** コンストラクタ */
    public LogDataSendingService() {
        super(TAG);
    }

}

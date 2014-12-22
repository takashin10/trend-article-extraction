/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.service;

import jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider.TrendArticleDb;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/** 記事コンテンツ状態を非同期に更新するIntentService 本サンプルアプリでは記事コンテンツの未読・既読状態を更新するのみ */
public class ArticleStateUpdateService extends IntentService {

    // field
    // ----------------------------------------------------------------

    private static final String TAG = ArticleStateUpdateService.class.getSimpleName();

    private static final String EX_CONTENT_ID = "ex_content_id";
    private static final String EX_READ_STATE = "ex_read_state";

    // method
    // ----------------------------------------------------------------

    /**
     * ArticleStateUpdateServiceを起動する
     * 
     * @param context Contextオブジェクト
     * @param contentId 状態更新対象の記事コンテンツID
     * @param isAlreadyRead 未読・既読状態、trueで既読
     */
    public static void startService(Context context, long contentId, boolean isAlreadyRead) {
        context.startService(new Intent(context, ArticleStateUpdateService.class)
                .putExtra(EX_CONTENT_ID, contentId).putExtra(EX_READ_STATE, isAlreadyRead));
    }

    /** ワーキングスレッド上で実行される処理 */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        // 未読・既読状態を更新
        TrendArticleDb.ArticleState.updateReadState(
                this, intent.getLongExtra(EX_CONTENT_ID, 0),
                intent.getBooleanExtra(EX_READ_STATE, false));
    }

    // constructor
    // ----------------------------------------------------------------

    /** コンストラクタ */
    public ArticleStateUpdateService() {
        super(TAG);
    }

}

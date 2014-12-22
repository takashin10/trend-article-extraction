/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.api;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import jp.ne.docomo.smt.dev.calendarview.common.ServerConfig;
import jp.ne.docomo.smt.dev.calendarview.entities.AppInfo;
import jp.ne.docomo.smt.dev.calendarview.entities.TaskResponse;
import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.sentenceunderstanding.SentenceTask;
import jp.ne.docomo.smt.dev.sentenceunderstanding.common.SentenceProjectSpecific;
import jp.ne.docomo.smt.dev.sentenceunderstanding.constants.Lang;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceAppInfoData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceExtractedWordsData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceResultData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.param.SentenceAppInfoParam;
import jp.ne.docomo.smt.dev.sentenceunderstanding.param.SentenceTaskRequestParam;
import jp.ne.docomo.smt.dev.sentenceunderstanding.param.SentenceUserUtteranceParam;
import java.util.ArrayList;
import java.util.List;

/**
 * 発話理解APIリクエストクラス
 * 発話理解 SDKを利用
 */
public class SentenceUnderstanding {
    // コールバック用のインスタンス
    private SentenceUnderstandingCallback mSentenceUnderstandingCallback;

    /**
     * コンストラクタ
     * @param sentenceUnderstandingCallback コールバック用のインスタンス
     */
    public SentenceUnderstanding(SentenceUnderstandingCallback sentenceUnderstandingCallback) {
        this.mSentenceUnderstandingCallback = sentenceUnderstandingCallback;
    }

    /**
     * 非同期通信用インナークラス
     */
    private class SentenceUnderstandingAsyncTask extends
            AsyncTask<SentenceTaskRequestParam, Integer, SentenceResultData> {
        private AlertDialog.Builder mDlg;

        private boolean mIsSdkException = false;
        private String mExceptionMessage = null;

        public SentenceUnderstandingAsyncTask(AlertDialog.Builder dlg) {
            super();
            mDlg = dlg;
        }

        @Override
        protected SentenceResultData doInBackground(SentenceTaskRequestParam... params) {
            SentenceResultData resultData = null;
            SentenceTaskRequestParam reqParam = params[0];

            try {
                // 要求処理クラスを作成
                final SentenceTask su = new SentenceTask();

                // 要求処理クラスにリクエストデータを渡し、レスポンスデータを取得する
                resultData = su.request(reqParam);

            } catch (SdkException ex) {
                mIsSdkException = true;
                mExceptionMessage = "ErrorCode: " + ex.getErrorCode()
                        + "\nMessage: " + ex.getMessage();

            } catch (ServerException ex) {
                mExceptionMessage = "ErrorCode: " + ex.getErrorCode()
                        + "\nMessage: " + ex.getMessage();
            }
            return resultData;
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(SentenceResultData resultData) {
            if (resultData == null) {
                if (mIsSdkException) {
                    mDlg.setTitle("SdkException 発生");

                } else {
                    mDlg.setTitle("ServerException 発生");
                }
                mDlg.setMessage(mExceptionMessage + " ");
                mDlg.show();
            } else {
                // コールバックのパラメータにレスポンスを設定
                TaskResponse arg0 = new TaskResponse();
                // プロジェクトキー
                arg0.projectKey = resultData.getProjectKey();
                // アプリケーション情報
                SentenceAppInfoData appInfo = resultData.getAppInfo();
                if (appInfo != null) {
                    arg0.appInfo = new AppInfo(appInfo.getAppName(), appInfo.getAppKey());
                }
                // クライアント側バージョン情報
                arg0.clientVer = resultData.getClientVer();
                // 対話モード
                arg0.dialogMode = resultData.getDialogMode();
                // 指定言語選択
                arg0.language = resultData.getLanguage();
                // ユーザ識別情報
                arg0.userId = resultData.getUserId();
                // 対話ステータス
                arg0.dialogStatus = resultData.getDialogStatus();
                // 検索結果
                arg0.content = resultData.getContent();
                // アプリ固有情報
                SentenceProjectSpecific specific = resultData.getProjectSpecific();
                if (specific != null && specific instanceof SampleSentenceProjectSpecific) {
                    arg0.projectSpecific = resultData.getProjectSpecific();
                }
                // ユーザ発話内容
                arg0.userUtterance = resultData.getUserUtterance();
                // タスク判定結果
                List<String> idList = resultData.getTaskIdList();
                if (idList != null) {
                    arg0.taskIdList = new ArrayList<String>();
                    for (int i = 0; i < resultData.getTaskIdList().size(); i++) {
                        arg0.taskIdList.add(i, resultData.getTaskIdList().get(i));
                    }
                }
                // 抽出文字列リスト
                List<SentenceExtractedWordsData> wordsList = resultData.getExtractedWordsList();
                if (wordsList != null) {
                    arg0.extractedWords = new ArrayList<SentenceExtractedWordsData>();
                    for (int i = 0; i < resultData.getExtractedWordsList().size(); i++) {
                        arg0.extractedWords.add(resultData.getExtractedWordsList().get(i));
                    }
                }
                // レスポンス送信時のサーバ内時刻
                arg0.serverSendTime = resultData.getServerSendTime();

                // コールバック
                mSentenceUnderstandingCallback.onResponse(arg0);
            }
        }
    }

    /**
     * リクエスト開始メソッド
     * @param context
     * @param utteranceText
     */
    public void startRequest(Context context, String utteranceText) {
        // 意図解釈パラメータクラスを生成して、発話を設定する
        SentenceTaskRequestParam param = new SentenceTaskRequestParam();

        // 言語設定
        param.setLanguage(Lang.JP);

        // アプリケーション情報
        // AppKey は APIKEY を設定
        SentenceAppInfoParam appInfo = new SentenceAppInfoParam();
        appInfo.setAppKey(ServerConfig.APIKEY);
        param.setAppInfo(appInfo);

        // クライアントバージョン
        param.setClientVer("0.1");

        // 発話理解を行う文章を設定
        SentenceUserUtteranceParam userUtterance = new SentenceUserUtteranceParam();
        userUtterance.setUtteranceText(utteranceText);
        param.setUserUtterance(userUtterance);

        // アプリ固有情報
        SampleSentenceProjectSpecific project = new SampleSentenceProjectSpecific();
        project.setSampleId("SAMPLE ID");
        param.setProjectSpecific(project);

        // エラー表示用
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);

        // 実行
        SentenceUnderstandingAsyncTask task = new SentenceUnderstandingAsyncTask(dlg);
        task.execute(param);
    }
}

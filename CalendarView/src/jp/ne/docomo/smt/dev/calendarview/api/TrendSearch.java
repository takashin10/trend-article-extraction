/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.api;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse.ArticleContents;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse.ContentData;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse.ImageSize;
import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.webcuration.CurationSearch;
import jp.ne.docomo.smt.dev.webcuration.constants.Lang;
import jp.ne.docomo.smt.dev.webcuration.data.CurationArticleContentsData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentsResultData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationImageSizeData;
import jp.ne.docomo.smt.dev.webcuration.param.CurationSearchRequestParam;

/**
 * トレンド記事抽出API(キーワード検索)リクエストクラス
 * トレンド記事抽出 SDKを利用
 */
public class TrendSearch {
    // コールバック用のインスタンス
    private TrendSearchCallback mTrendSearchCallback;

    /**
     *  コンストラクタ
     * @param trendCallback コールバック用のインスタンス
     */
    public TrendSearch(TrendSearchCallback trendCallback) {
        this.mTrendSearchCallback = trendCallback;
    }

    /**
     * 非同期通信用インナークラス
     */
    private class TrendSearchAsyncTask extends
            AsyncTask<CurationSearchRequestParam, Integer, CurationContentsResultData> {
        private AlertDialog.Builder mDlg;

        private boolean mIsSdkException = false;
        private String mExceptionMessage = null;

        public TrendSearchAsyncTask(AlertDialog.Builder dlg) {
            super();
            mDlg = dlg;
        }

        @Override
        protected CurationContentsResultData doInBackground(CurationSearchRequestParam... params) {
            CurationContentsResultData resultData = null;
            CurationSearchRequestParam reqParam = params[0];
            try {
                // キーワード検索クラスの生成して、リクエストを実行する
                CurationSearch search = new CurationSearch();
                resultData = search.request(reqParam);

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
        protected void onPostExecute(CurationContentsResultData resultData) {
            if (resultData == null) {
                // エラー時の処理
                if (mIsSdkException) {
                    mDlg.setTitle("SdkException 発生");

                } else {
                    mDlg.setTitle("ServerException 発生");
                }
                mDlg.setMessage(mExceptionMessage + " ");
                mDlg.show();

            } else {
                // コールバックのパラメータにレスポンスを設定
                SearchResponse arg0 = new SearchResponse();
                // コンテンツの全件数
                arg0.totalResults = resultData.getTotalResults();
                // 取得コンテンツの開始番号
                arg0.startIndex = resultData.getStartIndex();
                // 取得コンテンツの返却件数
                arg0.itemsPerPage = resultData.getItemsPerPage();
                // 記事セットの作成時刻
                arg0.issueDate = resultData.getIssueDate();
                // コンテンツの配列
                if (resultData.getCurationArticleContentsDataList() != null) {
                    arg0.articleContents = new ArrayList<ArticleContents>();
                    for (int i = 0; i < resultData.getCurationArticleContentsDataList()
                            .size(); i++) {
                        ArticleContents articleContents = new ArticleContents();
                        CurationArticleContentsData curationArticleContentsData =
                                resultData.getCurationArticleContentsDataList().get(i);
                        // コンテンツID
                        articleContents.contentId = curationArticleContentsData.getContentId();
                        // コンテンツ種別
                        articleContents.contentType = curationArticleContentsData.getContentType();
                        // ジャンルID
                        articleContents.genreId = curationArticleContentsData.getGenreId();
                        // コンテンツデータ
                        CurationContentData curationContentData =
                                curationArticleContentsData.getCurationContentData();
                        if (curationContentData != null) {
                            articleContents.contentData = new ContentData();
                            ContentData contentData = articleContents.contentData;
                            // コンテンツのタイトル
                            contentData.title = curationContentData.getTitle();
                            // コンテンツのスニペット
                            contentData.body = curationContentData.getBody();
                            // コンテンツのURL
                            contentData.linkUrl = curationContentData.getLinkUrl();
                            // コンテンツに含まれる画像のURL
                            contentData.imageUrl = curationContentData.getImageUrl();
                            // コンテンツに含まれる画像のサイズ
                            CurationImageSizeData imageSize =
                                    curationContentData.getCurationImageSizeData();
                            if (imageSize != null) {
                                contentData.imageSize = new ImageSize();
                                // 画像の縦ピクセル数
                                contentData.imageSize.height =
                                        curationContentData.getCurationImageSizeData().getHeight();
                                // 画像の横ピクセル数
                                contentData.imageSize.width =
                                        curationContentData.getCurationImageSizeData().getWidth();
                            }
                            // 記事作成日時
                            contentData.createdDate = curationContentData.getCreatedDate();
                            // 配信元ドメイン
                            contentData.sourceDomain = curationContentData.getSourceDomain();
                            // 配信元名称
                            contentData.sourceName = curationContentData.getSourceName();
                        }
                        // 関連コンテンツ
                        articleContents.relatedContents =
                                curationArticleContentsData.getRelatedContents();

                        arg0.articleContents.add(i, articleContents);
                    }
                }

                // コールバック
                mTrendSearchCallback.onResponse(arg0);
            }
        }
    }

    /**
     * リクエスト開始メソッド
     * @param context
     * @param keyword
     */
    public void startRequest(Context context, String keyword) {
        // パラメータを設定する
        CurationSearchRequestParam requestParam = new CurationSearchRequestParam();
        requestParam.setSearchKeyword(keyword);
        // 英語コンテンツを取得する場合、Lang.EN を設定してください。
        requestParam.setLang(Lang.JP);

        // エラー表示用
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);

        // 実行
        TrendSearchAsyncTask task = new TrendSearchAsyncTask(dlg);
        task.execute(requestParam);
    }
}

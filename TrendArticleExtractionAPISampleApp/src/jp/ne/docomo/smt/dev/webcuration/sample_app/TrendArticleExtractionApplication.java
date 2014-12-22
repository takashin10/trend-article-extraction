/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app;

import android.app.Application;

/** トレンド記事抽出SDKサンプル拡張アプリケーションクラス AndroidアプリにおけるSecureRandomの脆弱性対策コードを実行する */
public class TrendArticleExtractionApplication extends Application {

    /** アプリケーションプロセスが生成される際に呼び出されるコールバック */
    @Override
    public void onCreate() {
        super.onCreate();
        PRNGFixes.apply();
    }

}

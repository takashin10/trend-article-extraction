/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.common;

public final class ServerConfig {
    // API キー(開発者ポータルから取得したAPIキーを設定)
    public static final String APIKEY = 開発者ポータルから取得したAPIキーを設定してください;
    // 認証先のサービス名
    public static final String SNS_PROVIDER = "twitter";
    // 結果の返却先を指定するためのURL
    public static final String SNS_LOCATION = "test-app://auth";

    private ServerConfig() { }
}

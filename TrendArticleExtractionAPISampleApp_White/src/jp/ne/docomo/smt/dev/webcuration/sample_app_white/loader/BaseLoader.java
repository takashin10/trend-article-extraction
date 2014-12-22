/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.loader;

import java.util.Locale;

import jp.ne.docomo.smt.dev.webcuration.constants.Lang;
import android.content.AsyncTaskLoader;
import android.content.Context;

/** バックグラウンドで安全にデータの取得を行う基底AsyncTaskLoader */
public abstract class BaseLoader<D> extends AsyncTaskLoader<D> {

    // field
    // ----------------------------------------------------------------

    private D mData = null;
    private boolean mIsAccessTokenExpired = false;

    // method
    // ----------------------------------------------------------------

    /** ワーキングスレッド上で実行されるロード処理 */
    @Override
    public D loadInBackground() {
        return mData;
    }

    /** UIスレッドへロード結果を渡す */
    @Override
    public void deliverResult(D data) {
        if (!isReset()) {
            this.mData = data;
            super.deliverResult(data);
        }
    }

    /**
     * アクセストークンが有効期限切れがどうかの判定値を取得する
     * 
     * @return アクセストークンが有効期限切れの場合はtrue
     */
    public final boolean isAccessTokenExpired() {
        return mIsAccessTokenExpired;
    }

    /** ロード処理が開始される際に呼び出されるコールバック */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mData != null) {
            deliverResult(mData);
            return;
        }
        if ((mData == null) || takeContentChanged()) {
            forceLoad();
        }
    }

    /** ロード処理が終了される際に呼び出されるコールバック */
    @Override
    protected void onStopLoading() {
        cancelLoad();
        super.onStopLoading();
    }

    /** Loaderがリセットされる際に呼び出されるコールバック */
    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();
        mData = null;
    }

    /**
     * アクセストークンが有効期限切れがどうかの判定値を設定する
     * 
     * @param isAccessTokenExpired アクセストークンが有効期限切れの場合はtrue
     */
    protected final void setAccessTokenExpired(boolean isAccessTokenExpired) {
        this.mIsAccessTokenExpired = isAccessTokenExpired;
    }

    /**
     * 端末の言語設定からトレンド記事抽出APIに設定する言語設定を取得
     * 
     * @return トレンド記事抽出APIに設定する言語設定
     */
    protected final Lang getLanguage() {
        if (Locale.JAPAN.equals(Locale.getDefault())) {
            return Lang.JP;
        } else {
            return Lang.EN;
        }
    }

    // constructor
    // ----------------------------------------------------------------

    /**
     * コンストラクタ
     * 
     * @param context Contextオブジェクト
     */
    public BaseLoader(Context context) {
        super(context);
    }

}

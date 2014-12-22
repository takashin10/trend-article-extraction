/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.activity;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import jp.ne.docomo.smt.dev.webcuration.CurationSnsAuth;
import jp.ne.docomo.smt.dev.webcuration.param.CurationSnsAuthRequestParam;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.R;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.oauth.AccessTokenManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/** 認証設定画面 */
public class AuthenticationActivity extends Activity implements OAuthCallback, OnClickListener {

    // field
    // ----------------------------------------------------------------

    private static final String SNS_PROVIDER = "twitter";
    private static final String SNS_LOCATION = "test-app://auth";

    private static final String ERROR_CODE_INPUT = "001-007-01";
    private static final String ERROR_CODE_AUTH = "001-007-02";
    private static final String ERROR_CODE_INTERNAL = "001-007-03";
    private static final String ERROR_CODE_NETWORK = "001-007-04";
    private static final String ERROR_CODE_SERVER = "001-007-06";
    private static final String ERROR_CODE_RESPONSE = "001-007-07";
    private static final String ERROR_CODE_DENIED = "001-007-20";
    private static final String ERROR_CODE_CANCELLED = "001-007-21";

    private boolean mIsRefresh = false;
    private SnsAuthTask mTask = null;

    // method
    // ----------------------------------------------------------------

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
        runOnUiThread(new Runnable() {

            /** UIスレッドにポストされるトースト表示処理 */
            @Override
            public void run() {
                if (mIsRefresh) {
                    Toast.makeText(AuthenticationActivity.this,
                            R.string.msg_auth_docomo_refreshed,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AuthenticationActivity.this,
                            R.string.msg_auth_docomo_authorized,
                            Toast.LENGTH_SHORT).show();
                }
            }

        });

        // MainActivityに返却する結果を設定
        setResult(RESULT_OK);
    }

    /** docomo IDの認証に失敗した際に呼ばれるコールバック */
    @Override
    public void onError(OAuthError error) {
        if (error == null) {
            // 原因不明のエラー、通常ここを通ることは無い
            MessageDialog.showAllowingStateLoss(getFragmentManager(),
                    getString(R.string.msg_auth_docomo_err_title),
                    getString(R.string.msg_auth_docomo_err_msg_unknown));
            return;
        }

        // エラーコードからエラーメッセージを取得
        final String errorMessage;
        String errorCode = error.getErrorCode();
        if (ERROR_CODE_INPUT.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_input);
        } else if (ERROR_CODE_AUTH.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_auth);
        } else if (ERROR_CODE_INTERNAL.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_internal);
        } else if (ERROR_CODE_NETWORK.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_network);
        } else if (ERROR_CODE_SERVER.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_server);
        } else if (ERROR_CODE_RESPONSE.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_response);
        } else if (ERROR_CODE_DENIED.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_denied);
        } else if (ERROR_CODE_CANCELLED.equals(errorCode)) {
            errorMessage = getString(R.string.msg_auth_docomo_err_msg_cancelled);
        } else {
            errorMessage = getString(
                    R.string.msg_auth_docomo_err_format,
                    errorCode, error.getErrorMessage());
        }

        // エラーメッセージをトーストで表示
        runOnUiThread(new Runnable() {

            /** UIスレッドにポストされるトースト表示処理 */
            @Override
            public void run() {
                Toast.makeText(AuthenticationActivity.this,
                        errorMessage, Toast.LENGTH_SHORT).show();
            }

        });
    }

    /** Viewがクリックされた際に呼び出されるコールバック */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_auth_twitter: {
                // SNS認証URLを取得
                CurationSnsAuthRequestParam param = new CurationSnsAuthRequestParam();
                param.setProvider(SNS_PROVIDER);
                param.setLocation(SNS_LOCATION);
                mTask = new SnsAuthTask(this);
                mTask.execute(param);
                break;
            }
            case R.id.btn_auth_docomo: {
                // OAuth認証を開始
                AccessTokenManager manager = AccessTokenManager.getInstance(this);
                if (manager.hasAccessToken()) {
                    // リフレッシュトークンを使用した再認証処理を開始
                    mIsRefresh = true;
                    manager.startRefresh(this);
                } else {
                    // 初回認証処理を開始
                    mIsRefresh = false;
                    manager.startAuthentication(this, this);
                }
                break;
            }
            default:
                break;
        }
    }

    /** Activityが生成される際に呼び出されるコールバック */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // ActionBarの初期設定
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.title_main);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        ((View) findViewById(android.R.id.home).getParent()).setVisibility(View.GONE);

        // 各種リスナを設定
        findViewById(R.id.btn_auth_twitter).setOnClickListener(this);
        findViewById(R.id.btn_auth_docomo).setOnClickListener(this);
    }

    /** Activityが破棄される際に呼び出されるコールバック */
    @Override
    protected void onDestroy() {
        // 各種リスナを解放
        findViewById(R.id.btn_auth_twitter).setOnClickListener(null);
        findViewById(R.id.btn_auth_docomo).setOnClickListener(null);
        super.onDestroy();
    }

    /** Activityが非表示になる際に呼び出されるコールバック */
    @Override
    protected void onPause() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
        super.onPause();
    }

    // class
    // ----------------------------------------------------------------

    /** SDKを通してTwitter認証用のURLをバックグラウンドで取得するAsyncTask */
    private static class SnsAuthTask extends
            AsyncTask<CurationSnsAuthRequestParam, Integer, String> {

        // field
        // ----------------------------------------------------------------

        private final AuthenticationActivity mActivity;

        // method
        // ----------------------------------------------------------------

        /** ワーキングスレッド上で実行される非同期処理 */
        @Override
        protected String doInBackground(CurationSnsAuthRequestParam... params) {
            String result = null;
            try {
                result = new CurationSnsAuth().request(params[0]);
            } catch (SdkException e) {
                result = null;
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (ServerException e) {
                result = null;
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        /** 非同期処理が実行された後に呼び出されるコールバック */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (TextUtils.isEmpty(result)) {
                // 認証URLが空の場合はダイアログで通知
                MessageDialog.showAllowingStateLoss(mActivity.getFragmentManager(),
                        mActivity.getString(R.string.msg_auth_twitter_err_title),
                        mActivity.getString(R.string.msg_auth_twitter_err_url_empty));
            } else {
                // ブラウザアプリへIntentで認証URLを伝送する
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
            }
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param activity AuthenticationActivity
         */
        SnsAuthTask(AuthenticationActivity activity) {
            this.mActivity = activity;
        }

    }

}

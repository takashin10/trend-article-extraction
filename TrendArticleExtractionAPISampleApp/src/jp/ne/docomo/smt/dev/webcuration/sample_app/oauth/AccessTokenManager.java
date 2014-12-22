/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.oauth;

import jp.ne.docomo.smt.dev.oauth.OAuth;
import jp.ne.docomo.smt.dev.oauth.OAuthCallback;
import jp.ne.docomo.smt.dev.oauth.OAuthError;
import jp.ne.docomo.smt.dev.oauth.OAuthToken;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * docomo ID OAuth認証におけるアクセストークンおよび再認証用リフレッシュトークンの管理クラス
 * 認証処理と再認証処理の開始機能、暗号化されたトークンの保存および復号されたトークンの返却機能を提供する
 */
public class AccessTokenManager implements OAuthCallback {

    // field
    // ----------------------------------------------------------------

    public static final String ERROR_CODE_EXPIRATION = "001-001-08";

    private static final String OAUTH_CLIENT_ID = ドコモクラウドAPI 開発者サイトにて発行したID;
    private static final String OAUTH_CLIENT_SECRET = ドコモクラウドAPI 開発者サイトにて発行したID;
    private static final String OAUTH_SCOPE = スコープについては、「 API 共通リファレンス」 （https://dev.smt.docomo.ne.jp/?p=docs.common.index） の「（付録 1）scope 一覧」を参照してください。;
    private static final String OAUTH_REDIRECT_URI = ドコモクラウドAPI 開発者サイトにて発行したURI;

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static AccessTokenManager sInstance;

    private final Context mContext;
    private final Encryptor mEncryptor;

    private OAuthCallback mCallback = null;

    // method
    // ----------------------------------------------------------------

    /**
     * AccessTokenManagerオブジェクトを取得
     * 
     * @param context Contextオブジェクト
     * @return AccessTokenManagerオブジェクト
     */
    public static AccessTokenManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccessTokenManager(context);
        }
        return sInstance;
    }

    /** docomo IDの認証に成功した際に呼ばれるコールバック */
    @Override
    public void onComplete(OAuthToken token) {
        // アクセストークンとリフレッシュトークンを暗号化して保存
        String accessToken = mEncryptor.encrypt(mContext, token.getAccessToken());
        String refreshToken = mEncryptor.encrypt(mContext, token.getRefreshToken());
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .commit();

        // コールバック
        if (mCallback != null) {
            mCallback.onComplete(token);
            mCallback = null;
        }
    }

    /** docomo IDの認証に失敗した際に呼ばれるコールバック */
    @Override
    public void onError(OAuthError error) {
        if (mCallback != null) {
            mCallback.onError(error);
            mCallback = null;
        }
    }

    /**
     * docomo IDの認証画面を開き認証処理を開始する
     * 
     * @param activity Activityオブジェクト
     * @param callback OAuthCallbackオブジェクト
     */
    public void startAuthentication(Activity activity, OAuthCallback callback) {
        this.mCallback = callback;
        getOAuth().startAuth(activity, this);
    }

    /**
     * docomo IDの再認証処理を開始する
     * 
     * @param callback OAuthCallbackオブジェクト
     */
    public void startRefresh(OAuthCallback callback) {
        // 暗号化されたリフレッシュトークンを取得
        String refreshToken = PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(KEY_REFRESH_TOKEN, null);
        if (TextUtils.isEmpty(refreshToken)) {
            return;
        }
        this.mCallback = callback;

        // リフレッシュトークンを復号化して再認証処理を開始
        refreshToken = mEncryptor.decrypt(mContext, refreshToken);
        getOAuth().refreshAuth(mContext, refreshToken, this);
    }

    /**
     * アクセストークンが設定されているかどうか
     * 
     * @return アクセストークンが設定されている場合はtrue
     */
    public boolean hasAccessToken() {
        return PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .contains(KEY_ACCESS_TOKEN);
    }

    /**
     * アクセストークンを取得
     * 
     * @return アクセストークン
     */
    public String getAccessToken() {
        // 暗号化されたアクセストークンを取得
        String accessToken = PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(KEY_ACCESS_TOKEN, null);
        if (TextUtils.isEmpty(accessToken)) {
            return null;
        }

        // リフレッシュトークンを復号化
        accessToken = mEncryptor.decrypt(mContext, accessToken);
        return accessToken;
    }

    /**
     * OAuthオブジェクトを新規に生成して取得
     * 
     * @return 新規に生成されたOAuthオブジェクト
     */
    private OAuth getOAuth() {
        OAuth oAuth = new OAuth();
        oAuth.setClientID(OAUTH_CLIENT_ID);
        oAuth.setSecret(OAUTH_CLIENT_SECRET);
        oAuth.setScope(OAUTH_SCOPE);
        oAuth.setRedirectUri(OAUTH_REDIRECT_URI);
        return oAuth;
    }

    // constructor
    // ----------------------------------------------------------------

    /**
     * コンストラクタ
     * 
     * @param context Contextオブジェクト
     */
    private AccessTokenManager(Context context) {
        this.mContext = context.getApplicationContext();
        mEncryptor = new Encryptor();
    }

}

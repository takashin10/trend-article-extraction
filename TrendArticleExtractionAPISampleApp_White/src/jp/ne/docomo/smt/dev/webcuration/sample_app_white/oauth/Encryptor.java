/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.oauth;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Base64;

/** 文字列およびバイト配列の暗号化・復号化を行うクラス */
class Encryptor {

    // field
    // ----------------------------------------------------------------

    /** 初期化ベクトル */
    private static final byte[] IV = {
        //初期化ベクトルの値は変更してご利用ください(16バイト)
            XX, XX, XX, XX, XX, XX, XX, XX, XX, XX, XX, XX, XX, XX, XX, XX
    };

    /** 共通鍵生成用ソルト */
    private static final byte[] SALT = {
        //共通鍵生成用ソルトは変更してご利用ください(任意のバイト数)
            XX, XX, XX, XX, XX, XX, XX, XX, XX, XX,
            XX, XX, XX, XX, XX, XX, XX, XX, XX, XX
    };

    /** 暗号化アルゴリズム */
    private static final String KEY_ALGORITHM = "AES";

    /** 暗号化アルゴリズム / ブロック暗号モード / パディングルール */
    private static final String TRANSFORMATION = KEY_ALGORITHM + "/CBC/PKCS7Padding";

    // method
    // ----------------------------------------------------------------

    /**
     * 文字列をバイト配列にデコード
     * 
     * @param string デコードする文字列
     * @return デコードされたバイト配列
     */
    static byte[] decode(String string) {
        return Base64.decode(string, Base64.URL_SAFE);
    }

    /**
     * バイト配列を文字列にエンコード
     * 
     * @param bytes エンコードするバイト配列
     * @return エンコードされた文字列
     */
    static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, (Base64.URL_SAFE | Base64.NO_WRAP));
    }

    /**
     * 復号化
     * 
     * @param context Contextオブジェクト
     * @param encrypted 復号化するバイト配列
     * @return 復号化されたバイト配列
     */
    byte[] decrypt(Context context, byte[] encrypted) {
        byte[] plain = null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(context), new IvParameterSpec(IV));
            plain = cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException e) {
            plain = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (NoSuchPaddingException e) {
            plain = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (InvalidKeyException e) {
            plain = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (InvalidAlgorithmParameterException e) {
            plain = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (IllegalBlockSizeException e) {
            plain = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (BadPaddingException e) {
            plain = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return plain;
    }

    /**
     * 暗号化
     * 
     * @param context Contextオブジェクト
     * @param plain 暗号化するバイト配列
     * @return 暗号化されたバイト配列
     */
    byte[] encrypt(Context context, byte[] plain) {
        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(context), new IvParameterSpec(IV));
            encrypted = cipher.doFinal(plain);
        } catch (NoSuchAlgorithmException e) {
            encrypted = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (NoSuchPaddingException e) {
            encrypted = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (InvalidKeyException e) {
            encrypted = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (InvalidAlgorithmParameterException e) {
            encrypted = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (IllegalBlockSizeException e) {
            encrypted = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (BadPaddingException e) {
            encrypted = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return encrypted;
    }

    /**
     * 復号化
     * 
     * @param context Contextオブジェクト
     * @param encrypted 復号化する文字列
     * @return 復号化された文字列
     */
    String decrypt(Context context, String encrypted) {
        return encode(decrypt(context, decode(encrypted)));
    }

    /**
     * 暗号化
     * 
     * @param context Contextオブジェクト
     * @param plain 暗号化する文字列
     * @return 暗号化された文字列
     */
    String encrypt(Context context, String plain) {
        return encode(encrypt(context, decode(plain)));
    }

    /**
     * バイト配列のキー値からSecretKeyオブジェクトを生成する
     * 
     * @param context Contextオブジェクト
     * @return SecretKeyオブジェクト
     */
    private SecretKey generateKey(Context context) {
        SecretKey key = null;
        try {
            // アプリのパッケージ名を取得
            String packageName = context.getApplicationInfo().packageName;

            // アプリのインストール日時を取得
            String installTime = String.valueOf(context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_META_DATA).firstInstallTime);

            // パッケージ名とインストール日時から共通鍵を生成
            char[] password = (installTime + packageName).toCharArray();
            key = SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC")
                    .generateSecret(new PBEKeySpec(password, SALT, 1024, 256));
        } catch (NumberFormatException e) {
            key = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (NameNotFoundException e) {
            key = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (InvalidKeySpecException e) {
            key = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            key = null;
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return key;
    }

}

/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

/** タイトル、メッセージ、およびOKボタンを表示する汎用DialogFragment */
public class MessageDialog extends DialogFragment {

    // field
    // ----------------------------------------------------------------

    private static final String TAG = MessageDialog.class.getSimpleName();

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";

    // method
    // ----------------------------------------------------------------

    /**
     * MessageDialogを開く
     * 
     * @param fm FragmentManagerオブジェクト
     * @param title ダイアログのタイトル文字列
     * @param message ダイアログの本文文字列
     */
    public static void show(FragmentManager fm, String title, String message) {
        show(fm, title, message, false);
    }

    /**
     * MessageDialogを開く
     * 
     * @param fm FragmentManagerオブジェクト
     * @param title ダイアログのタイトル文字列
     * @param message ダイアログの本文文字列
     */
    public static void showAllowingStateLoss(FragmentManager fm, String title, String message) {
        show(fm, title, message, true);
    }

    /**
     * MessageDialogを開く
     * 
     * @param fm FragmentManagerオブジェクト
     * @param title ダイアログのタイトル文字列
     * @param message ダイアログの本文文字列
     * @param allowStateLoss trueならActivityの状態保存後にコミットを行う
     */
    private static void show(FragmentManager fm,
            String title, String message, boolean allowStateLoss) {
        // 既に開いているMessageDialogが存在する場合は先に閉じておく
        MessageDialog dialog = (MessageDialog) fm.findFragmentByTag(TAG);
        if (dialog != null) {
            dialog.onDismiss(dialog.getDialog());
        }

        // 新規にMessageDialogを生成して開く
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        dialog = new MessageDialog();
        dialog.setArguments(args);
        if (allowStateLoss) {
            fm.beginTransaction().add(dialog, TAG).commitAllowingStateLoss();
        } else {
            dialog.show(fm, TAG);
        }
    }

    /** DialogFragmentのDialogが生成される際に呼び出される */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(ARG_TITLE))
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok, null).create();
    }

}

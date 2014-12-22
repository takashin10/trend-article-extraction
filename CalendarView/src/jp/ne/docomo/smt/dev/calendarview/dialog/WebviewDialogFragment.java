/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.dialog;

import jp.ne.docomo.smt.dev.calendarview.R;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebviewDialogFragment
        extends DialogFragment
        implements OnClickListener, OnKeyListener {

    private View mView;
    private ProgressBar mWebviewProgress;
    private String mTitle;
    private String mUrl;

    public WebviewDialogFragment(String title, String url) {
        this.mTitle = title;
        this.mUrl = url;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.webview_dialog, container, false);
        mWebviewProgress = (ProgressBar) mView.findViewById(R.id.webview_progress);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        setupWebview();
        initData();
        initAction();
        getDialog().setOnKeyListener(this);
    }

    private void initAction() {
        mView.findViewById(R.id.webview_close_btn).setOnClickListener(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebview() {
        WebView webView = (WebView) mView.findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mWebviewProgress.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mWebviewProgress.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mWebviewProgress.setProgress(newProgress);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev>"
                + "(KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>");
    }

    private void initData() {
        ((TextView) mView.findViewById(R.id.webview_title)).setText(mTitle);
        ((WebView) mView.findViewById(R.id.webview)).loadUrl(mUrl);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.webview_close_btn) {
            dismiss();
        }
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (((WebView) mView.findViewById(R.id.webview)).canGoBack()) {
            ((WebView) mView.findViewById(R.id.webview)).goBack();
            return true;
        }
        return false;
    }

}

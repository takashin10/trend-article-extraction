/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import jp.ne.docomo.smt.dev.calendarview.adapter.CalendarAdapter;
import jp.ne.docomo.smt.dev.calendarview.adapter.InformationAdapter;
import jp.ne.docomo.smt.dev.calendarview.api.SentenceUnderstanding;
import jp.ne.docomo.smt.dev.calendarview.api.SentenceUnderstandingCallback;
import jp.ne.docomo.smt.dev.calendarview.api.TrendSearchCallback;
import jp.ne.docomo.smt.dev.calendarview.api.TrendSearch;
import jp.ne.docomo.smt.dev.calendarview.common.CalApplication;
import jp.ne.docomo.smt.dev.calendarview.common.ServerConfig;
import jp.ne.docomo.smt.dev.calendarview.database.TBTodoEvent;
import jp.ne.docomo.smt.dev.calendarview.dialog.TodoInputDialog;
import jp.ne.docomo.smt.dev.calendarview.dialog.WebviewDialogFragment;
import jp.ne.docomo.smt.dev.calendarview.dialog.TodoInputDialog.TodoInputSuccess;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchComparator;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse;
import jp.ne.docomo.smt.dev.calendarview.entities.TaskResponse;
import jp.ne.docomo.smt.dev.calendarview.entities.TodoEvent;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse.ArticleContents;
import jp.ne.docomo.smt.dev.calendarview.fragment.BaseFragment;
import jp.ne.docomo.smt.dev.calendarview.fragment.CalendarViewFragment;
import jp.ne.docomo.smt.dev.calendarview.utils.Utility;
import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import jp.ne.docomo.smt.dev.webcuration.CurationSnsAuth;
import jp.ne.docomo.smt.dev.webcuration.param.CurationSnsAuthRequestParam;

public class CalendarActivity
        extends FragmentActivity
        implements OnItemClickListener, TodoInputSuccess {

    public GregorianCalendar month; // calendar instances.

    public Handler handler; // for grabbing some event values for showing the dot
                            // marker.
    public ArrayList<String> items; // container to store calendar items which
                                    // needs showing the event marker
    ArrayList<TodoEvent> event;

    private CalendarViewFragment mFragment;

    private InformationAdapter mAdapter;

    private String mSelectedGridDate;

    private ArrayList<String> mDaysNeedGetTask = new ArrayList<String>();

    private SimpleDateFormat mDf;
    private boolean mIsItemClick = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        Locale.setDefault(Locale.US);

        month = (GregorianCalendar) GregorianCalendar.getInstance();

        items = new ArrayList<String>();

        mFragment = new CalendarViewFragment(month);
        mFragment.setOnItemClick(this);
        mDf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        mSelectedGridDate = mDf.format(month.getTime());
        mFragment.setSelectedGridDate(mSelectedGridDate);
        BaseFragment.setFragment(this, mFragment);

        handler = new Handler();
        refreshSelectedEvent();

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(android.text.format.DateFormat.format("yyyy年MM月", month));

        View previous = findViewById(R.id.previous);

        previous.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setPreviousMonth();
                refreshCalendar();
            }
        });

        View next = findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setNextMonth();
                refreshCalendar();
            }
        });

        ListView listInformation = (ListView) findViewById(R.id.information_list);
        mAdapter = new InformationAdapter(this);
        mAdapter.setOnItemClickListener(this);
        listInformation.setAdapter(mAdapter);

        // API キーの登録
        AuthApiKey.initializeAuth(ServerConfig.APIKEY);
    }

    // オプションメニューの設定
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    // メニューアイテムが選択された時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_twitter:
            // SNS認証URLを取得
            CurationSnsAuthRequestParam param = new CurationSnsAuthRequestParam();
            param.setProvider(ServerConfig.SNS_PROVIDER);
            param.setLocation(ServerConfig.SNS_LOCATION);
            SnsAuthTask mTask = new SnsAuthTask(this);
            mTask.execute(param);
            return true;
        default:
        }
        return false;
    }

    protected void setNextMonth() {
        mIsItemClick = false;
        if (month.get(GregorianCalendar.MONTH) == month.getActualMaximum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) + 1),
                    month.getActualMinimum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) + 1);
        }

    }

    protected void setPreviousMonth() {
        mIsItemClick = false;
        if (month.get(GregorianCalendar.MONTH) == month.getActualMinimum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) - 1),
                    month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) - 1);
        }

    }

    protected void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();

    }

    public void refreshCalendar() {
        TextView title = (TextView) findViewById(R.id.title);

        mFragment = new CalendarViewFragment(month);
        mFragment.setOnItemClick(this);
        mFragment.setSelectedGridDate(mSelectedGridDate);
        mFragment.setSelectedItems(items);
        BaseFragment.setFragment(this, mFragment);
        getNext3Days(mSelectedGridDate);

        title.setText(android.text.format.DateFormat.format("yyyy年MM月", month));
    }

    public void refreshSelectedEvent() {
        handler.postDelayed(calendarUpdater, 500);
    }

    public Runnable calendarUpdater = new Runnable() {

        @Override
        public void run() {
            items.clear();

            event = Utility.readCalendarEventFromDB(CalendarActivity.this);
            for (int i = 0; i < event.size(); i++) {
                items.add(event.get(i).createAt);
            }
            mFragment.setSelectedItems(items);
            getNext3Days(mSelectedGridDate);
        }
    };

    private boolean mLock = false;

    private int mTotalRequest;

    private int mRequestCount;

    private ArrayList<TodoEvent> mAllEventList;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mTotalRequest > 0) {
            return;
        }
        mIsItemClick = true;
        if (mLock) {
            return;
        }
        mLock = true;
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mLock = false;
            }
        }, 1000);
        if (parent == null) {
            ArticleContents item = mAdapter.getItem(position);
            WebviewDialogFragment dialog =
                    new WebviewDialogFragment(item.contentData.title, item.contentData.linkUrl);
            dialog.show(getSupportFragmentManager(), "");
        } else {
            boolean isInputEvent = false;
            if (mSelectedGridDate.equals(CalendarAdapter.sDayString.get(position))) {
                isInputEvent = true;
            }
            mSelectedGridDate = CalendarAdapter.sDayString.get(position);
            ((CalendarAdapter) parent.getAdapter()).setCurentDateString(mSelectedGridDate);
            ((CalendarAdapter) parent.getAdapter()).notifyDataSetChanged();
            String[] separatedTime = mSelectedGridDate.split("-");
            String gridvalueString = separatedTime[2].replaceFirst("^0*", "");
            int gridvalue = Integer.parseInt(gridvalueString);
            // navigate to next or previous month on clicking offdays.
            if ((gridvalue > 7) && (position < 8)) {
                setPreviousMonth();
                refreshCalendar();
            } else if ((gridvalue < 7 * 2) && (position > 28)) {
                setNextMonth();
                refreshCalendar();
            } else {
                if (isInputEvent) {
                    TodoInputDialog dialog = new TodoInputDialog(mSelectedGridDate);
                    TBTodoEvent eventTb = new TBTodoEvent(CalApplication.getInstant().getSqlDb());
                    dialog.setEvent(eventTb.getAllEventByDay(mSelectedGridDate));
                    dialog.setOnTodoInputSuccess(this);
                    dialog.show(getSupportFragmentManager(), "");
                } else {
                    getNext3Days(mSelectedGridDate);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onTodoInputSuccess(String day, ArrayList<String> events) {
        TBTodoEvent eventTB = new TBTodoEvent(CalApplication.getInstant().getSqlDb());
        eventTB.deleteEventByCreateAt(day);
        if (events != null) {
            for (int i = 0; i < events.size(); i++) {
                eventTB.addEvent(day, events.get(i));
            }
        }
        refreshSelectedEvent();
    }

    private void getNext3Days(String day) {
        if (!mIsItemClick) {
            return;
        }
        mDaysNeedGetTask.clear();
        String[] seperate = day.split("-");
        GregorianCalendar temp = (GregorianCalendar) month.clone();
        temp.set(Integer.valueOf(seperate[0]), Integer.valueOf(seperate[1]) - 1,
                Integer.valueOf(seperate[2]));
        for (int i = 0; i < 3; i++) {
            mDaysNeedGetTask.add(mDf.format(temp.getTime()));
            temp.add(GregorianCalendar.DATE, 1);
        }
        temp = null;

        mAllEventList = new ArrayList<TodoEvent>();
        TBTodoEvent eventTb = new TBTodoEvent(CalApplication.getInstant().getSqlDb());
        for (int i = 0; i < mDaysNeedGetTask.size(); i++) {
            mAllEventList.addAll(eventTb.getAllEventByDay(mDaysNeedGetTask.get(i)));
        }
        searchRespondList.clear();
        mTotalRequest = 0;
        ((ListView) findViewById(R.id.information_list)).setVisibility(View.INVISIBLE);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.empty_view_for_todo).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);
        if (mAllEventList == null || mAllEventList.size() == 0) {
            ((ListView) findViewById(R.id.information_list)).setVisibility(View.INVISIBLE);
            findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
            findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
            findViewById(R.id.empty_view_for_todo).setVisibility(View.VISIBLE);
        }

        mTotalRequest += mAllEventList.size();
        mRequestCount = 0;
        requestForEachEvent();
    }

    void requestForEachEvent() {
        if (mRequestCount < mAllEventList.size()) {
            // 発話理解
            SentenceUnderstanding request =
                    new SentenceUnderstanding(
                            new GetTaskListener(mAllEventList.get(mRequestCount).createAt,
                                    mRequestCount));
            request.startRequest(CalendarActivity.this, mAllEventList.get(mRequestCount).event);
            mRequestCount++;
        }
    }

    ArrayList<ArticleContents> searchRespondList = new ArrayList<ArticleContents>();

    private class GetTaskListener implements SentenceUnderstandingCallback {
        private Date mDate;

        public GetTaskListener(String day, int position) {
            try {
                mDate = mDf.parse(day);
                Calendar cal = (GregorianCalendar) GregorianCalendar.getInstance().clone();
                cal.setTime(mDate);
                cal.add(Calendar.MINUTE, position);
                mDate = cal.getTime();
                cal = null;
            } catch (ParseException e) {
                mDate = ((GregorianCalendar) GregorianCalendar.getInstance().clone()).getTime();
            }
        }

        @Override
        public void onResponse(TaskResponse arg0) {
            mTotalRequest--;
            requestForEachEvent();
            for (int i = 0; i < arg0.userUtterance.getUtteranceWordList().size(); i++) {
                if (!arg0.userUtterance.getUtteranceWordList().get(i)
                        .matches("^[\\u3040-\\u309F]+$")) {
                    // トレンド記事抽出API キーワード検索
                    TrendSearch request = new TrendSearch(
                            new TrendSearchCallback() {
                                /*
                                 * トレンド記事抽出SDK(キーワード検索)からのコールバック
                                 * @param arg0 キーワード検索のレスポンス
                                 */
                                @Override
                                public void onResponse(SearchResponse arg0) {
                                    mTotalRequest--;
                                    for (int i = 0; i < arg0.articleContents.size();
                                            i++) {
                                        arg0.articleContents.get(i).date = mDate;
                                        searchRespondList.add(
                                                arg0.articleContents.get(i));
                                    }
                                    Collections.sort(searchRespondList,
                                            new SearchComparator());
                                    mAdapter.setData(searchRespondList);

                                    if (searchRespondList.size() == 0) {
                                        findViewById(R.id.empty_view)
                                                .setVisibility(View.VISIBLE);
                                        findViewById(R.id.progress_bar)
                                                .setVisibility(View.INVISIBLE);
                                        findViewById(R.id.information_list)
                                                .setVisibility(View.INVISIBLE);
                                        return;
                                    }

                                    ((ListView) findViewById(R.id.information_list))
                                            .setVisibility(View.VISIBLE);
                                    findViewById(R.id.progress_bar)
                                            .setVisibility(View.GONE);
                                    findViewById(R.id.empty_view)
                                            .setVisibility(View.GONE);
                                }
                            }
                            );
                    // キーワード検索開始
                    request.startRequest(CalendarActivity.this,
                            arg0.userUtterance.getUtteranceWordList().get(i));
                    mTotalRequest++;
                }
            }
        }
    }

    /** SDKを通してTwitter認証用のURLをバックグラウンドで取得するAsyncTask */
    private static class SnsAuthTask extends
            AsyncTask<CurationSnsAuthRequestParam, Integer, String> {

        private AlertDialog.Builder mDlg;
        private Context mContext;

        /** ワーキングスレッド上で実行される非同期処理 */
        @Override
        protected String doInBackground(CurationSnsAuthRequestParam... params) {
            String result = null;
            try {
                result = new CurationSnsAuth().request(params[0]);
            } catch (SdkException e) {
                result = null;
                if (BuildConfig.DEBUG) {
                    // TODO エラー処理を書く
                }
            } catch (ServerException e) {
                result = null;
                if (BuildConfig.DEBUG) {
                    // TODO エラー処理を書く
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
                mDlg.setTitle(mContext.getString(R.string.msg_auth_twitter_err_title));
                mDlg.setMessage(mContext.getString(R.string.msg_auth_twitter_err_url_empty));
                mDlg.show();
            } else {
                // ブラウザアプリへIntentで認証URLを伝送する
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
            }
        }

        /**
         * コンストラクタ
         * @param context
         */
        SnsAuthTask(Context context) {
            mContext = context;
            // エラー表示用
            mDlg = new AlertDialog.Builder(context);
        }
    }
}

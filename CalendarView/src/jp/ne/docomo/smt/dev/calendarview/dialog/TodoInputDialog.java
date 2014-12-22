/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.dialog;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import jp.ne.docomo.smt.dev.calendarview.R;
import jp.ne.docomo.smt.dev.calendarview.entities.TodoEvent;
import jp.ne.docomo.smt.dev.calendarview.views.EventInputView;

public class TodoInputDialog extends DialogFragment implements OnClickListener {

    private View mView;
    private String mTitle;
    ArrayList<TodoEvent> event;
    private boolean mIsEditMode = false;

    public interface TodoInputSuccess {
        void onTodoInputSuccess(String day, ArrayList<String> events);
    }

    TodoInputSuccess onTodoInputSuccess;

    public TodoInputDialog(String title) {
        this.mTitle = title;
    }

    public void setEvent(ArrayList<TodoEvent> event) {
        this.event = event;
    }

    public void setOnTodoInputSuccess(TodoInputSuccess onTodoInputSuccess) {
        this.onTodoInputSuccess = onTodoInputSuccess;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dialog_input_event, container, false);
        findViews();
        return mView;
    }

    private void findViews() {

    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        initAction();
        initData();
    }

    private void initData() {
        ((TextView) mView.findViewById(R.id.dialog_title)).setText(mTitle);
        ((LinearLayout) mView.findViewById(R.id.event_container)).removeAllViews();
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                EventInputView eventRow = new EventInputView(getActivity());
                eventRow.getEventName().setText(event.get(i).event);
                eventRow.setOnRemoveclickListener(this);
                ((LinearLayout) mView.findViewById(R.id.event_container)).addView(eventRow);
            }
        }
    }

    private void initAction() {
        mView.findViewById(R.id.cancel_btn).setOnClickListener(this);
        mView.findViewById(R.id.ok_btn).setOnClickListener(this);
        mView.findViewById(R.id.edit_btn).setOnClickListener(this);
        mView.findViewById(R.id.add_btn_id).setOnClickListener(this);
    }

    private void changeEditMode(boolean editable) {
        LinearLayout viewItems = ((LinearLayout) mView.findViewById(R.id.event_container));
        int visibleMode;
        if (editable) {
            mView.findViewById(R.id.add_btn_id).setEnabled(false);
            visibleMode = View.VISIBLE;
        } else {
            mView.findViewById(R.id.add_btn_id).setEnabled(true);
            visibleMode = View.GONE;
        }
        for (int i = 0; i < viewItems.getChildCount(); i++) {
            EventInputView item = (EventInputView) viewItems.getChildAt(i);
            item.getRemoveBtn().setVisibility(visibleMode);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.cancel_btn:
            dismiss();
            break;
        case R.id.ok_btn:
            if (onTodoInputSuccess != null) {
                ArrayList<String> events = new ArrayList<String>();
                LinearLayout viewItems = ((LinearLayout) mView.findViewById(R.id.event_container));
                for (int i = 0; i < viewItems.getChildCount(); i++) {
                    EventInputView item = (EventInputView) viewItems.getChildAt(i);
                    if (!TextUtils.isEmpty(item.getEventName().getEditableText().toString())) {
                        events.add(item.getEventName().getEditableText().toString());
                    }
                }
                onTodoInputSuccess.onTodoInputSuccess(mTitle, events);
            }
            dismiss();
            break;
        case R.id.edit_btn:
            mIsEditMode = !mIsEditMode;
            changeEditMode(mIsEditMode);
            if (mIsEditMode) {
                ((TextView) mView.findViewById(R.id.edit_btn)).setText("完了");
            } else {
                ((TextView) mView.findViewById(R.id.edit_btn)).setText("編集");
            }
            break;
        case R.id.remove_btn_id:
            ((LinearLayout) mView.findViewById(R.id.event_container))
                    .removeView((View) v.getParent().getParent());
            break;
        case R.id.add_btn_id:
            if (!mIsEditMode) {
                EventInputView eventRow = new EventInputView(getActivity());
                eventRow.setOnRemoveclickListener(this);
                ((LinearLayout) mView.findViewById(R.id.event_container)).addView(eventRow);
            }
            break;
        default:
            break;
        }
    }
}

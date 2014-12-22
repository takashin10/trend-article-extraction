/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.fragment;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;


import jp.ne.docomo.smt.dev.calendarview.R;
import jp.ne.docomo.smt.dev.calendarview.adapter.CalendarAdapter;

public class CalendarViewFragment extends BaseFragment implements OnItemClickListener {
    private View mContent;
    public CalendarAdapter adapter;
    private GregorianCalendar mMonth;
    OnItemClickListener onItemClick;
    private String mSelectedGridDate;
    private ArrayList<String> mSelectedItems;

    public CalendarViewFragment(GregorianCalendar month) {
        this.mMonth = month;
    }

    public CalendarViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        return mContent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new CalendarAdapter(getActivity(), mMonth);
        adapter.setCurentDateString(mSelectedGridDate);
        if (mSelectedItems != null) {
            adapter.setItems(mSelectedItems);
        }
        GridView gridview = (GridView) mContent.findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (onItemClick != null) {
            onItemClick.onItemClick(parent, view, position, id);
        }
    }

    public void setOnItemClick(OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setSelectedGridDate(String selectedGridDate) {
        this.mSelectedGridDate = selectedGridDate;
    }

    public void setSelectedItems(ArrayList<String> items) {
        mSelectedItems = items;
        if (adapter == null) {
            return;
        }
        adapter.setItems(items);
        adapter.notifyDataSetChanged();
    }
}

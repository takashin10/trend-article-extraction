/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jp.ne.docomo.smt.dev.calendarview.R;

public class CalendarAdapter extends BaseAdapter {
    private Context mContext;

    private java.util.Calendar mMonth;
    public GregorianCalendar pmonth; // calendar instance for previous month
    /**
     * calendar instance for previous month for getting complete view
     */
    public GregorianCalendar pmonthmaxset;
    int firstDay;
    int maxWeeknumber;
    int maxP;
    int calMaxP;
    int lastWeekDay;
    int leftDays;
    int mnthlength;
    String itemvalue, curentDateString;
    DateFormat df;

    private ArrayList<String> mItems;
    public static List<String> sDayString;

    public CalendarAdapter(Context c, GregorianCalendar monthCalendar) {
        CalendarAdapter.sDayString = new ArrayList<String>();
        Locale.setDefault(Locale.US);
        mMonth = monthCalendar;
        mContext = c;
        mMonth.set(GregorianCalendar.DAY_OF_MONTH, 1);
        this.mItems = new ArrayList<String>();
        df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        refreshDays();
    }

    public void setCurentDateString(String curentDateString) {
        this.curentDateString = curentDateString;
    }

    public void setItems(ArrayList<String> items) {
        for (int i = 0; i != items.size(); i++) {
            if (items.get(i).length() == 1) {
                items.set(i, "0" + items.get(i));
            }
        }
        this.mItems = items;
    }

    public int getCount() {
        return sDayString.size();
    }

    public Object getItem(int position) {
        return sDayString.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater vi =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.calendar_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.content = (FrameLayout) convertView.findViewById(R.id.calendar_content_id);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.date_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // separates daystring into parts.
        String[] separatedTime = sDayString.get(position).split("-");
        // taking last part of date. ie; 2 from 2012-12-02
        String gridvalue = separatedTime[2].replaceFirst("^0*", "");
        // checking whether the day is in current month or not.
        if ((Integer.parseInt(gridvalue) > 1) && (position < firstDay)) {
            // setting offdays to white color.
            viewHolder.date.setTextColor(Color.LTGRAY);
            viewHolder.date.setClickable(false);
            viewHolder.date.setFocusable(false);
        } else if ((Integer.parseInt(gridvalue) < 14) && (position > 28)) {
            viewHolder.date.setTextColor(Color.LTGRAY);
            viewHolder.date.setClickable(false);
            viewHolder.date.setFocusable(false);
        } else {
            // setting curent month's days in blue color.
            viewHolder.date.setTextColor(Color.GRAY);
        }

        // create date string for comparison
        String date = sDayString.get(position);

        if (date.length() == 1) {
            date = "0" + date;
        }
        String monthStr = "" + (mMonth.get(GregorianCalendar.MONTH) + 1);
        if (monthStr.length() == 1) {
            monthStr = "0" + monthStr;
        }

        viewHolder.content.setBackgroundColor(
                mContext.getResources().getColor(R.color.calendar_cell));
        // show icon if date is not empty and it exists in the items array
        if (date.length() > 0 && mItems != null && mItems.contains(date)) {
            viewHolder.content.setBackgroundColor(
                    mContext.getResources().getColor(R.color.calendar_cel_check_select));
            viewHolder.icon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.INVISIBLE);
        }
        if (sDayString.get(position).equals(curentDateString)) {
            viewHolder.content.setBackgroundColor(
                    mContext.getResources().getColor(R.color.calendar_cel_selectl));
        }
        viewHolder.date.setText(gridvalue);

        return convertView;
    }

    private class ViewHolder {
        public TextView date;
        public ImageView icon;
        public FrameLayout content;
    }

    public void refreshDays() {
        // clear items
        mItems.clear();
        sDayString.clear();
        Locale.setDefault(Locale.US);
        pmonth = (GregorianCalendar) mMonth.clone();
        // month start day. ie; sun, mon, etc
        firstDay = mMonth.get(GregorianCalendar.DAY_OF_WEEK);
        // finding number of weeks in current month.
        // maxWeeknumber =
        // month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
        maxWeeknumber = 6;
        // allocating maximum row number for the gridview.
        mnthlength = maxWeeknumber * 7;
        maxP = getMaxP(); // previous month maximum day 31,30....
        calMaxP = maxP - (firstDay - 1);    // calendar offday starting 24,25 ...
        /**
         * Calendar instance for getting a complete gridview including the three month's
         * (previous,current,next) dates.
         */
        pmonthmaxset = (GregorianCalendar) pmonth.clone();
        /**
         * setting the start date as previous month's required date.
         */
        pmonthmaxset.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);

        /**
         * filling calendar gridview.
         */
        for (int n = 0; n < mnthlength; n++) {

            itemvalue = df.format(pmonthmaxset.getTime());
            pmonthmaxset.add(GregorianCalendar.DATE, 1);
            sDayString.add(itemvalue);

        }
    }

    private int getMaxP() {
        int maxP;
        if (mMonth.get(GregorianCalendar.MONTH)
                == mMonth.getActualMinimum(GregorianCalendar.MONTH)) {
            pmonth.set((mMonth.get(GregorianCalendar.YEAR) - 1),
                    mMonth.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            pmonth.set(GregorianCalendar.MONTH, mMonth.get(GregorianCalendar.MONTH) - 1);
        }
        maxP = pmonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

        return maxP;
    }

}

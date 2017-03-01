package com.point.eslee.health_free;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eslee on 2017-02-12.
 */

public class ListViewPointAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewPointItem> listViewItemList = new ArrayList<ListViewPointItem>();

    // ListViewAdapter의 생성자
    public ListViewPointAdapter() {

    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }


    @Override
    public Object getItem(int i) {
        return listViewItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_point_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_point_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView titleView = (TextView) view.findViewById(R.id.listitem_title);
        TextView datetimeView = (TextView) view.findViewById(R.id.listitem_datetime);
        TextView pointView = (TextView) view.findViewById(R.id.listitem_point);
        TextView pointDetailView = (TextView) view.findViewById(R.id.listitem_pointdetail);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewPointItem listViewItem = listViewItemList.get(pos);

        // 뷰 태그에 아이템 저장
        view.setTag(listViewItem);

        // 아이템 내 각 위젯에 데이터 반영
        titleView.setText(listViewItem.getTitle());
        datetimeView.setText(listViewItem.getDateTime());
        pointView.setText(Common.get_pointString(listViewItem.getPoint()));
        if (listViewItem.getPoint() < 0) {
            pointView.setTextColor(view.getResources().getColor(R.color.colorPoint2));
        } else {
            pointView.setTextColor(view.getResources().getColor(R.color.colorPoint1));
        }
        pointDetailView.setText(listViewItem.getPointStr());

       // setListViewHeight((ListView) view); // 높이 조절
        return view;
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if(listAdapter == null)return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(int id, String title, String datetime, int point, String point_detail) {
        ListViewPointItem item = new ListViewPointItem(id,title,datetime,point,point_detail);
        listViewItemList.add(item);
    }

    public void clearItem() {
        listViewItemList.clear();
    }
}

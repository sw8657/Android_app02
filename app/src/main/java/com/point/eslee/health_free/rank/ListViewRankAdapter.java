package com.point.eslee.health_free.rank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.point.eslee.health_free.R;
import com.point.eslee.health_free.VO.RankVO;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Administrator on 2017-04-14.
 */

public class ListViewRankAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<RankVO> listViewItemList = new ArrayList<RankVO>();

    public ListViewRankAdapter(){

    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listViewItemList.get(position).get_ID();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // listview_rank_item Layout을 inflate하여 converView 참조 획득
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_rank_item,parent,false);
        }

        TextView numView = (TextView) view.findViewById(R.id.rankitem_num);
        ImageView imgView = (ImageView) view.findViewById(R.id.rankitem_img);
        TextView usernameView = (TextView) view.findViewById(R.id.rankitem_username);
        TextView valueView = (TextView) view.findViewById(R.id.rankitem_value);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        RankVO rankVO = listViewItemList.get(pos);
        // 뷰 태그에 아이템 저장
        view.setTag(rankVO);

        // 아이템 내 각 위젯에 데이터 반영
        numView.setText(String.valueOf(rankVO.getNum()));
        usernameView.setText(rankVO.getTitle());
        valueView.setText(rankVO.getValue());
        Glide.with(view.getContext()).load(rankVO.getImgUrl())
                .bitmapTransform(new CropCircleTransformation(view.getContext()))
                .placeholder(R.drawable.blank_profile)
                .error(R.drawable.blank_profile)
                .into(imgView);

        return view;
    }

    public void addItem(int _ID, int user_id, int num, String imgUrl, String Title, String Value){
        RankVO item = new RankVO(_ID, user_id, num,imgUrl,Title,Value);
        listViewItemList.add(item);
    }

    public void addItemList(ArrayList<RankVO> items){
        listViewItemList.addAll(items);
    }

    public void replaceItemList(ArrayList<RankVO> items){
        listViewItemList.clear();
        listViewItemList.addAll(items);
    }

    public void clearItem(){
        listViewItemList.clear();
    }
}

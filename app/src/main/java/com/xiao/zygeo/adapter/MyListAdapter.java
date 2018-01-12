package com.xiao.zygeo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xiao.zygeo.R;

import java.util.List;
import java.util.Map;

/**
 * Created by xiao on 2017/9/8.
 */

public class MyListAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> list;

    public  MyListAdapter(Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.record_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.recorditem_name);
            holder.date = (TextView) view.findViewById(R.id.recorditem_date);
            holder.img = (ImageView) view.findViewById(R.id.recorditem_img);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.img.setScaleType(ImageView.ScaleType.FIT_XY);
        Picasso.with(context)
                .load(String.valueOf(list.get(position).get("img")))
                .placeholder(R.drawable.pic_load)
                .error(R.drawable.picfail)
                .into(holder.img);
        holder.title.setText(String.valueOf(list.get(position).get("title")));
        holder.date.setText(String.valueOf(list.get(position).get("date")));
        return view;
    }

    class ViewHolder {
        private ImageView img;
        private TextView title, date;
    }
}

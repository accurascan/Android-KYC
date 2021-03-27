package com.accurascan.accurasdk.sample.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.accurascan.accurasdk.sample.R;
import com.accurascan.ocr.mrz.model.BarcodeFormat;

import java.util.List;

public class BarCodeFormatListAdapter extends BaseAdapter {

    private Context mContext;
    private List<BarcodeFormat> menuItemList;

    public BarCodeFormatListAdapter(Context context, List<BarcodeFormat> navItems) {
        mContext = context;
        menuItemList = navItems;
    }

    @Override
    public int getCount() {
        return menuItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_barcode, null);
            holder = new ViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_barcode_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(menuItemList.get(position).barcodeTitle);
        if (menuItemList.get(position).isSelected) {
            holder.tvTitle.setTextColor(Color.RED);
        } else {
            holder.tvTitle.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView tvTitle;
    }
}
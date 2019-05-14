package com.github.aamnony.smartdoorbell;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MsgAdapter extends BaseAdapter {
    private final Context mContext;
    LayoutInflater mInflater;
    private final TableRowModel[] mTableRows;
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    MsgAdapter(Context context, TableRowModel[] tableRows) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mTableRows = tableRows;
    }

    @Override
    public int getCount() {
        return mTableRows.length;
    }

    @Override
    public TableRowModel getItem(int position) {
        return mTableRows[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.log_item, parent, false);
            holder = new Holder();
            holder.personName = convertView.findViewById(R.id.txtPerson);
            holder.actionType = convertView.findViewById(R.id.txtActionType);
            holder.timestamp = convertView.findViewById(R.id.txtTime);
            holder.snapshot = convertView.findViewById(R.id.imgSnapshot);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        TableRowModel item = getItem(position);
        holder.personName.setText(item.getPersonName());
        holder.actionType.setText(item.getActionType());
        //holder.timestamp.setText(String.valueOf(item.getTimeStamp()));

//        int date1 =  item.getTimeStamp();
        Date dt = new Date((long)(item.getTimeStamp()*1000));
        holder.timestamp.setText(FORMAT.format(dt));

        if (item.getSnapshotTempUrl() != null) {
            Picasso.get().load(item.getSnapshotTempUrl()).into(holder.snapshot);
        }

        return convertView;
    }

    private static class Holder {
        TextView timestamp;
        TextView actionType;
        TextView personName;
        ImageView snapshot;
    }
}
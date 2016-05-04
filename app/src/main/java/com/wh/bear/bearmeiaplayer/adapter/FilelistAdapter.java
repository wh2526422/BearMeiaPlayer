package com.wh.bear.bearmeiaplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.wh.bear.bearmeiaplayer.R;
import com.wh.bear.bearmeiaplayer.utils.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2015-10-08.
 */
public class FilelistAdapter extends BaseAdapter {

    Context context;
    List<File> data;

    public FilelistAdapter(Context context, List<File> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.media_item, parent, false);
            ImageView icon = (ImageView) convertView.findViewById(R.id.mIcon);
            TextView title = (TextView) convertView.findViewById(R.id.mTitle);
            TextView display_name= (TextView) convertView.findViewById(R.id.display_name);
            holder = new Holder(icon, title,display_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        File file = data.get(position);
        if (file.isDirectory()){
            holder.icon.setImageResource(R.drawable.file);
        }else{
            String suffix = StringUtils.getSuffix(file.getName());
            if (("mp4").equals(suffix)||("3gp").equals(suffix)) {
                holder.icon.setImageResource(R.drawable.media);
            }else {
                holder.icon.setImageResource(android.R.drawable.ic_menu_help);
            }

        }
        holder.title.setText(file.getName());
        holder.title.setTextColor(Color.BLACK);
        holder.display_name.setText(StringUtils.getTime(file.lastModified()));
        holder.display_name.setTextColor(Color.BLACK);
        return convertView;
    }

    class Holder {
        ImageView icon;
        TextView title;
        TextView display_name;

        public Holder(ImageView icon, TextView title,TextView display_name) {
            this.icon = icon;
            this.title = title;
            this.display_name=display_name;
        }
    }
}

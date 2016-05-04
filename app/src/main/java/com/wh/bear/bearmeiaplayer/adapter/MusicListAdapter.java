package com.wh.bear.bearmeiaplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wh.bear.bearmeiaplayer.R;
import com.wh.bear.bearmeiaplayer.bean.Music;
import com.wh.bear.bearmeiaplayer.utils.StringUtils;

import java.net.PortUnreachableException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Administrator on 15-10-12.
 */
public class MusicListAdapter extends BaseAdapter{
    ArrayList<Music> data;
    Context context;

    public MusicListAdapter(ArrayList<Music> data, Context context) {
        this.data = data;
        this.context = context;
    }
    public void setData(ArrayList<Music> data){
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
        if (convertView==null){
            convertView=View.inflate(context, R.layout.media_item,null);
            ImageView icon = (ImageView) convertView.findViewById(R.id.mIcon);
            TextView title = (TextView) convertView.findViewById(R.id.mTitle);
            TextView display_name = (TextView) convertView.findViewById(R.id.display_name);
            TextView duration = (TextView) convertView.findViewById(R.id.mduration);
            TextView singFlag = (TextView) convertView.findViewById(R.id.music_play_flag);
            holder = new Holder(icon, title, display_name, duration,singFlag);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Music music = data.get(position);
        holder.icon.setBackgroundResource(R.drawable.musicicon);
        holder.title.setText(music.getTilte());
        holder.display_name.setText(music.getArtist());
        try {
            holder.duration.setText(StringUtils.getMusicDuration(music.getDuration()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (music.singing) {
            holder.singFlag.setVisibility(View.VISIBLE);
            holder.singFlag.setText(context.getResources().getString(R.string.isplaying));
        } else {
            holder.singFlag.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    class Holder {
        ImageView icon;
        TextView title;
        TextView display_name;
        TextView duration;
        TextView singFlag;

        public Holder(ImageView icon, TextView title, TextView display_name, TextView duration, TextView singFlag) {
            this.icon = icon;
            this.title = title;
            this.display_name = display_name;
            this.duration = duration;
            this.singFlag = singFlag;
        }
    }


}

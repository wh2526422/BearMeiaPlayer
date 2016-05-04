package com.wh.bear.bearmeiaplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wh.bear.bearmeiaplayer.R;
import com.wh.bear.bearmeiaplayer.bean.Video;
import com.wh.bear.bearmeiaplayer.utils.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Administrator on 15-9-30.
 */
public class PlayerListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Video> data;
    int index;
    public PlayerListAdapter(Context context, ArrayList<Video> data,int index) {
        this.context = context;
        this.data = data;
        this.index=index;
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

            convertView=View.inflate(context, R.layout.vedio_item,null);
            ImageView play_flag= (ImageView) convertView.findViewById(R.id.play_flag);
            TextView title= (TextView) convertView.findViewById(R.id.video_title);
            TextView duration= (TextView) convertView.findViewById(R.id.video_duration);

            holder=new Holder(play_flag, title, duration);
            convertView.setTag(holder);
        }else {
            holder= (Holder) convertView.getTag();
        }

        holder.title.setText(data.get(position).getTitle());
        try {
            holder.duration.setText(StringUtils.getVideoDuration(data.get(position).getDuration()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (position==index){
            holder.play_flag.setVisibility(View.VISIBLE);
        }else {
            holder.play_flag.setVisibility(View.GONE);
        }
        return convertView;
    }

    class Holder{
        ImageView play_flag;
        TextView title;
        TextView duration;

        public Holder(ImageView play_flag, TextView title, TextView duration) {
            this.play_flag = play_flag;
            this.title = title;
            this.duration = duration;
        }
    }
}

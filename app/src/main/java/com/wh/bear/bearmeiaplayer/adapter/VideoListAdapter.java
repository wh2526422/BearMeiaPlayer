package com.wh.bear.bearmeiaplayer.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
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
public class VideoListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Video> data;

    public VideoListAdapter(Context context, ArrayList<Video> data) {
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.media_item, parent, false);
            ImageView icon = (ImageView) convertView.findViewById(R.id.mIcon);
            TextView title = (TextView) convertView.findViewById(R.id.mTitle);
            TextView display_name = (TextView) convertView.findViewById(R.id.display_name);
            TextView duration = (TextView) convertView.findViewById(R.id.mduration);

            holder = new Holder(icon, title, display_name, duration);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Video video = data.get(position);
        Bitmap source = ThumbnailUtils.createVideoThumbnail(video.getUrl(), MediaStore.Video.Thumbnails.MICRO_KIND);
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(source, 50, 50);
        holder.icon.setImageBitmap(bitmap);
        holder.title.setText(video.getTitle());
        holder.display_name.setText(video.getDisplay_name());
        try {
            holder.duration.setText(StringUtils.getVideoDuration(video.getDuration()));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    class Holder {
        ImageView icon;
        TextView title;
        TextView display_name;
        TextView duration;

        public Holder(ImageView icon, TextView title, TextView display_name, TextView duration) {
            this.icon = icon;
            this.title = title;
            this.display_name = display_name;
            this.duration = duration;
        }
    }

}

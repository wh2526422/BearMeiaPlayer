package com.wh.bear.mediaplayer.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.wh.bear.mediaplayer.R;
import com.wh.bear.mediaplayer.bean.Video;
import com.wh.bear.mediaplayer.utils.LoadVideoThumbnail;
import com.wh.bear.mediaplayer.utils.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Administrator on 15-9-30.
 */
public class VideoListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Video> data;
    LoadVideoThumbnail loader;
    ListView mListView;

    public VideoListAdapter(Context context, ArrayList<Video> data, ListView listView) {
        this.context = context;
        this.data = data;
        this.mListView = listView;
        loader = LoadVideoThumbnail.getInstance();
        loader.cancelAllTasksAndCleanData();
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
        holder.icon.setTag(video.getUrl());
        Bitmap localeImage = loader.getLocaleImage(video.getUrl());
        if (localeImage == null) {
            holder.icon.setImageResource(R.drawable.media);
            loader.displayImage(video.getUrl(), new LoadVideoThumbnail.OnVideoThumbnailLoadCallback() {
                @Override
                public void loadVideoThumbnailCompleted(Bitmap image, String imageUrl) {
                    ImageView view = (ImageView) mListView.findViewWithTag(imageUrl);
                    if (view != null && image != null) {
                        view.setImageBitmap(image);
                    }
                }
            });
        } else {
            holder.icon.setImageBitmap(localeImage);
        }

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

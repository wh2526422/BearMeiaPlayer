package com.wh.bear.bearmeiaplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 15-9-28.
 */
public class Video implements Parcelable{

    private String title;
    private String display_name;
    private long duration;
    private String url;
    private int currentProgress;


    public Video(String title, String display_name, long duration, String url) {
        this.title = title;
        this.display_name = display_name;
        this.duration = duration;
        this.url = url;
    }

    public Video(String title, String display_name, long duration, String url, int currentProgress) {
        this.title = title;
        this.display_name = display_name;
        this.duration = duration;
        this.url = url;
        this.currentProgress = currentProgress;
    }

    public Video() {
    }

    protected Video(Parcel in) {
        title = in.readString();
        display_name = in.readString();
        duration = in.readLong();
        url = in.readString();
        currentProgress = in.readInt();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(display_name);
        dest.writeLong(duration);
        dest.writeString(url);
        dest.writeInt(currentProgress);
    }
}

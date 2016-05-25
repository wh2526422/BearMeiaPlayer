package com.wh.bear.mediaplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 15-10-12.
 */
public class Music implements Parcelable {
    private String tilte;
    private String album;
    private String artist;
    private String url;
    private int duration;
    private long size;
    public boolean singing;

    public Music(String tilte, String album, String artist, String url, int duration, long size) {
        this.tilte = tilte;
        this.album = album;
        this.artist = artist;
        this.url = url;
        this.duration = duration;
        this.size = size;
    }

    public Music() {
    }

    protected Music(Parcel in) {
        tilte = in.readString();
        album = in.readString();
        artist = in.readString();
        url = in.readString();
        duration = in.readInt();
        size = in.readLong();
    }

    public String getTilte() {
        return tilte;
    }

    public void setTilte(String tilte) {
        this.tilte = tilte;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tilte);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeInt(duration);
        dest.writeLong(size);
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
}

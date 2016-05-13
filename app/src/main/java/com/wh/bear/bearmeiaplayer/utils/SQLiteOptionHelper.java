package com.wh.bear.bearmeiaplayer.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.wh.bear.bearmeiaplayer.bean.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 15-10-12.
 */
public class SQLiteOptionHelper extends SQLiteOpenHelper{
    private static final int WRITE = 0;
    private static final int READ = 0;
    SQLiteDatabase db;
    public SQLiteOptionHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    private SQLiteDatabase getInstance(int flag){
        if (flag==WRITE){
            db=getWritableDatabase();
        }else {
            db=getReadableDatabase();
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table video(" +
                "id integer primary key," +
                "title varchar(20)," +
                "display_name varchar(20)," +
                "duration big int," +
                "url varchar(100)," +
                "currentProgress integer" +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 插入数据库
     * @param vs
     * @return
     */
    public void setVideos(List<Video> vs){
        db=getInstance(WRITE);
        for (Video v : vs) {
            ContentValues values=new ContentValues();
            values.put("title",v.getTitle());
            values.put("display_name",v.getDisplay_name());
            values.put("duration",v.getDuration());
            values.put("url",v.getUrl());
            values.put("currentProgress",v.getCurrentProgress());
            db.insert("video", null, values);
        }
        db.close();
    }

    /**
     * 从数据库中读取
     * @return
     */
    public ArrayList<Video> getVideos(){
        db=getInstance(READ);
        ArrayList<Video> videos=new ArrayList<>();
        Cursor cursor = db.query("video", null, null, null, null, null, null);
        while (cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String display_name = cursor.getString(cursor.getColumnIndex("display_name"));
            int duration = cursor.getInt(cursor.getColumnIndex("duration"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            int currentProgress = cursor.getInt(cursor.getColumnIndex("currentProgress"));
            Video video=new Video(title, display_name, duration, url, currentProgress);
            videos.add(video);
        }
        cursor.close();
        db.close();
        return videos;

    }

    /**
     * 项数据库插入单条video
     * @param v
     * @return
     */
    public long setVideo(Video v){
        db=getInstance(WRITE);
        ContentValues values = new ContentValues();
        values.put("title",v.getTitle());
        values.put("display_name",v.getDisplay_name());
        values.put("duration",v.getDuration());
        values.put("url",v.getUrl());
        values.put("currentProgress", v.getCurrentProgress());
        long l = db.insert("video", null, values);
        db.close();
        return l;

    }

    /**
     * 从数据库中获取单个video对象
     * @param title
     * @return
     */
    public Video getVideo(String title){
        db=getInstance(READ);
        Video video = null;
        Cursor cursor = db.query("video", null, "title=?", new String[]{title}, null, null, null);
        while (cursor.moveToFirst()){
            String display_name = cursor.getString(cursor.getColumnIndex("display_name"));
            int duration = cursor.getInt(cursor.getColumnIndex("duration"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            int currentProgress = cursor.getInt(cursor.getColumnIndex("currentProgress"));
            video=new Video(title, display_name, duration, url, currentProgress);
        }
        cursor.close();
        db.close();
        return video;

    }

    public int clearData(){
        db=getInstance(WRITE);
        return db.delete("video",null,null);
    }

    /**
     * 更新视频进度
     * @param title
     * @param progress
     * @return
     */
    public int updateVideoProgress(String title,int progress){
        db=getInstance(WRITE);

        ContentValues values=new ContentValues();

        values.put("currentProgress", progress);

        int i = db.update("video", values, "title=?", new String[]{title});
        db.close();
        return i;

    }
}

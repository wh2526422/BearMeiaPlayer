package com.wh.bear.bearmeiaplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015-10-13.
 */
public class MediaKeeper {
    /**
     * 保存主题id
     * @param context
     * @param themeid
     */
    public static void writeTheme(Context context,int themeid){

        SharedPreferences theme = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("themeId",themeid);
        edit.apply();
    }

    /**
     * 读取主题id
     * @param context
     * @return
     */
    public static int readTheme(Context context){
        SharedPreferences theme = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        return theme.getInt("themeId", 0);
    }

    /**
     * 保存播放模式
     * @param context
     * @param model
     */
    public static void writePlaymodel(Context context,int model){
        SharedPreferences theme = context.getSharedPreferences("model", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("model",model);
        edit.apply();
    }

    /**
     * 读取播放模式
     * @param context
     * @return
     */
    public static int readPlaymodel(Context context){
        SharedPreferences theme = context.getSharedPreferences("model", Context.MODE_PRIVATE);
        return theme.getInt("model", 0);
    }

    /**
     * 保存当前音量
     * @param context
     * @param streamVolume
     */
    public static void writeCurrentStreamVolume(Context context,int streamVolume) {
        SharedPreferences theme = context.getSharedPreferences("streamVolume", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("streamVolume",streamVolume);
        edit.apply();
    }

    /**
     * 读取播放模式
     * @param context
     * @return
     */
    public static int readCurrentStreamVolume(Context context){
        SharedPreferences streamVolume = context.getSharedPreferences("streamVolume", Context.MODE_PRIVATE);
        return streamVolume.getInt("streamVolume", 0);
    }
}

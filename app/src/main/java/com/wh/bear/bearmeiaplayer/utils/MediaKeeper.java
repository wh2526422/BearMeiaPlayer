package com.wh.bear.bearmeiaplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015-10-13.
 */
public class MediaKeeper {
    /**
     * 保存主题id
     * @param context 上下文
     * @param themeId 主题id
     */
    public static void writeTheme(Context context,int themeId){

        SharedPreferences theme = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("themeId",themeId);
        edit.apply();
    }

    /**
     * 读取主题id
     * @param context 上下文
     * @return
     */
    public static int readTheme(Context context){
        SharedPreferences theme = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        return theme.getInt("themeId", 0);
    }

    /**
     * 保存播放模式
     * @param context 上下文
     * @param model 播放模式
     */
    public static void writePlaymodel(Context context,int model){
        SharedPreferences theme = context.getSharedPreferences("model", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("model",model);
        edit.apply();
    }

    /**
     * 读取播放模式
     * @param context 上下文
     * @return
     */
    public static int readPlaymodel(Context context){
        SharedPreferences theme = context.getSharedPreferences("model", Context.MODE_PRIVATE);
        return theme.getInt("model", 0);
    }

    /**
     * 保存当前音量
     * @param context 上下文
     * @param streamVolume 当前音量
     */
    public static void writeCurrentStreamVolume(Context context,int streamVolume) {
        SharedPreferences theme = context.getSharedPreferences("streamVolume", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("streamVolume",streamVolume);
        edit.apply();
    }

    /**
     * 读取播放模式
     * @param context 上下文
     * @return
     */
    public static int readCurrentStreamVolume(Context context){
        SharedPreferences streamVolume = context.getSharedPreferences("streamVolume", Context.MODE_PRIVATE);
        return streamVolume.getInt("streamVolume", 0);
    }

    /**
     * 储存视频播放进度
     * @param context 上下文
     * @param progress 当前进度
     * @param name 视频名字
     */
    public static void writeVideoHistotyProgress(Context context,int progress,String name) {
        SharedPreferences history_progress = context.getSharedPreferences("history_progress", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = history_progress.edit();
        edit.putInt(name,progress);
        edit.apply();
    }

    /**
     * 读取视频播放进度
     * @param context 上下文
     * @param name 视频名字
     * @return
     */
    public static int readVideoHistotyProgress(Context context,String name){
        SharedPreferences streamVolume = context.getSharedPreferences("history_progress", Context.MODE_PRIVATE);
        return streamVolume.getInt(name, 0);
    }

}

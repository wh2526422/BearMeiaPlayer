package com.wh.bear.bearmeiaplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015-10-13.
 */
public class MediaThemeKeeper {
    /**
     * 保存主题id
     * @param context
     * @param themeid
     */
    public static void writeTheme(Context context,int themeid){

        SharedPreferences theme = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("themeId",themeid);
        edit.commit();
    }

    /**
     * 读取主题id
     * @param context
     * @return
     */
    public static int readTheme(Context context){
        SharedPreferences theme = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        int themeId = theme.getInt("themeId", 0);
        return themeId;
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
        edit.commit();
    }

    /**
     * 读取播放模式
     * @param context
     * @return
     */
    public static int readPlaymodel(Context context){
        SharedPreferences theme = context.getSharedPreferences("model", Context.MODE_PRIVATE);
        int model = theme.getInt("model", 0);
        return model;
    }
}

package com.wh.bear.mediaplayer.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 15-9-30.
 */
public class StringUtils {

    /**
     * 格式化时长
     * @param time
     * @return
     * @throws ParseException
     */
    public static String getVideoDuration(long time) throws ParseException {
        // 00:00
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String model= "0001-01-01 00:00:00";
        Date parse = format.parse(model);

        String s = format.format(parse.getTime()+time);

        return s.substring(10);
    }

    /**
     * 格式化时长
     * @param time
     * @return
     * @throws ParseException
     */
    public static String getMusicDuration(long time) throws ParseException {
        // 00:00:00
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String model= "0001-01-01 00:00:00";
        Date parse = format.parse(model);

        String s = format.format(parse.getTime()+time);
        return s.substring(14);
    }

    /**
     * 格式化日期
     * @param time
     * @return
     */
    public static String getTime(long time){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.format(time);
    }

    public static String getSuffix(String title){
        File f =new File(title);
        String fileName=f.getName();
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }
}

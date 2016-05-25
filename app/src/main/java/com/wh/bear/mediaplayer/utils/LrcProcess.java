package com.wh.bear.mediaplayer.utils;

import com.wh.bear.mediaplayer.bean.LrcContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author
 *  处理歌词的类
 */
public class LrcProcess {
    private List<LrcContent> lrcList; //List集合存放歌词内容对象
    private LrcContent mLrcContent;     //声明一个歌词内容对象
    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcess() {
        mLrcContent = new LrcContent();
        lrcList = new ArrayList<>();
    }

    /**
     * 读取歌词
     * @param path
     * @return
     */
    public String readLRC(String path) {
        //定义一个StringBuilder对象，用来存放歌词内容
        StringBuilder stringBuilder = new StringBuilder();
        File f = new File(path.replace(".mp3", ".lrc"));
        if (!f.exists()){
            stringBuilder.append("木有歌词文件，赶紧去下载！...");
            return stringBuilder.toString();
        }
        try {
            //创建一个文件输入流对象
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "@");

                //分离“@”字符
                String splitLrcData[] = s.split("@");
                if(splitLrcData.length > 1) {
                    mLrcContent.setLrcStr(splitLrcData[1]);

                    //处理歌词取得歌曲的时间
                    int lrcTime = time2Str(splitLrcData[0]);
                    if (lrcTime == -1) {
                        return stringBuilder.append("木有歌词文件，赶紧去下载！...").toString();
                    }
                    mLrcContent.setLrcTime(lrcTime);

                    //添加进列表数组
                    lrcList.add(mLrcContent);

                    //新创建歌词内容对象
                    mLrcContent = new LrcContent();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append("木有读取到歌词哦！");
        }
        return stringBuilder.toString();
    }
    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     * @param timeStr
     * @return
     */
    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        int minute = 0;
        int second = 0;
        int millisecond = 0;
        try {
            minute = Integer.parseInt(timeData[0]);
            second = Integer.parseInt(timeData[1]);
            millisecond = Integer.parseInt(timeData[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }

        //计算上一行与下一行的时间转换为毫秒数
        return (minute * 60 + second) * 1000 + millisecond * 10;
    }
    public List<LrcContent> getLrcList() {
        return lrcList;
    }
}

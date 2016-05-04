package com.wh.bear.bearmeiaplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.wh.bear.bearmeiaplayer.bean.LrcContent;
import com.wh.bear.bearmeiaplayer.bean.Music;
import com.wh.bear.bearmeiaplayer.utils.LrcProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 15-10-12.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{

    private MediaPlayer player;
    private static int model=0;
    private static ArrayList<Music> data_music;
    private static int currentPosition;
    private String url;
    private List<LrcContent> lrcList = new ArrayList<>(); //存放歌词列表对象
    private int index = 0;          //歌词检索值
    Timer timer=new Timer();
    private boolean startTask;
    private Handler handler=new Handler();
    private int currentTime;
    private int duration;
    TimerTask task=new TimerTask() {
        @Override
        public void run() {
            int currentProgress = player.getCurrentPosition();
            Intent receiver=new Intent("com.iotek.bearmediaplayer.MusicBroadcastReiceiver");

            receiver.putExtra("currentProgress",currentProgress);
            sendBroadcast(receiver);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (player==null) {
            player=new MediaPlayer();
        }

        player.setOnCompletionListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent != null ? intent.getStringExtra("url") : null;
        if (url!=null) {
            if (url.equals(this.url)){
                initLrc(url);
                return super.onStartCommand(intent, flags, startId);
            }
            initLrc(url);
            start(url);
            this.url=url;
        }
        String data = intent != null ? intent.getStringExtra("data") : null;
        if (data!=null) {
            switch (data) {
                case "pause":
                    pause();
                    break;
                case "play":
                    play();
                    break;
                case "changeProgress":
                    int progress = intent.getIntExtra("progress", 0);
                    player.seekTo(progress);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        player.release();
        return super.stopService(name);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (model){
            case 0:
                //顺序播放
                model=0;
                currentPosition++;
                if (currentPosition==data_music.size()){
                    pause();
                    return;
                }
                break;
            case 1:
                //随机播放
                model=1;
                Random random=new Random();
                currentPosition = random.nextInt(data_music.size());
                break;
            case 2:
                //整体循环
                model=2;
                currentPosition++;
                if (currentPosition==data_music.size()){
                    currentPosition=0;
                }
                break;
            case 3:
                //单曲循环
                model=3;
                break;
        }
        setPositionPlay(currentPosition);
        //每次播放结束开始下一首歌时发送广播更新界面
        Intent receiver=new Intent("com.iotek.bearmediaplayer.MusicBroadcastReiceiver");
        receiver.putExtra("next",currentPosition);
        sendBroadcast(receiver);
    }

    /**
     * 播放前准备
     * @param url 音乐文件url
     */
    private void start(String url) {
        try {
            player.reset();
            player.setDataSource(url);
            player.prepare();
            player.setOnPreparedListener(this);
            play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始播放
     */
    private void play() {
        if (player!=null){
            player.start();
        }
        //计时器开始工作，更新进度
        if (!startTask) {
            timer.schedule(task, 1000, 1000);
            startTask=true;
        }
    }

    /**
     * 暂停
     */
    private void pause() {
        if (player!=null){
            player.pause();
        }
    }

    /**
     * 播放下一个位置的音乐
     * @param currentPosition
     */
    private void setPositionPlay(int currentPosition) {
        Music music = data_music.get(currentPosition);
        url=music.getUrl();
        start(url);
        initLrc(url);
    }

    /**
     * 初始化歌词
     * @param url
     */
    public void initLrc(String url){
        LrcProcess mLrcProcess = new LrcProcess();
        //读取歌词文件
        mLrcProcess.readLRC(url);

        //传回处理后的歌词文件
        lrcList = mLrcProcess.getLrcList();
        MusicPlayerActivity.lrcView.setmLrcList(lrcList);
        //切换带动画显示歌词
        MusicPlayerActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_z));
        handler.post(mRunnable);
    }
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            MusicPlayerActivity.lrcView.setIndex(lrcIndex());
            MusicPlayerActivity.lrcView.invalidate();
            handler.postDelayed(mRunnable, 100);
        }
    };

    /**
     * 根据时间获取歌词显示的索引值
     * @return
     */
    public int lrcIndex() {
        if(player.isPlaying()) {
            currentTime = player.getCurrentPosition();
            duration = player.getDuration();
        }
        if(currentTime < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrcList.get(i).getLrcTime()
                            && currentTime < lrcList.get(i + 1).getLrcTime()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1
                        && currentTime > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Intent intent = new Intent("com.wh.changesingflag");
        intent.putExtra("position",currentPosition);
        sendBroadcast(intent);
    }

    public static class MusicServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //将音乐文件传入
            ArrayList<Music> data = intent.getParcelableArrayListExtra("data_music");
            int firstPosition = intent.getIntExtra("firstPosition", -1);
            if (data!=null){
                data_music=data;
            }
            if (firstPosition!=-1){
                currentPosition=firstPosition;
            }
            //改变播放模式
            int music_model = intent.getIntExtra("music_model", -1);
            if (music_model!=-1) {
                switch (music_model){
                    case 0:
                        //顺序播放
                        model=0;
                        break;
                    case 1:
                        //随机播放
                        model=1;
                        break;
                    case 2:
                        //整体循环
                        model=2;
                        break;
                    case 3:
                        //单曲循环
                        model=3;
                        break;
                }
            }

        }
    }
}

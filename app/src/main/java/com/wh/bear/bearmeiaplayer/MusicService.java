package com.wh.bear.bearmeiaplayer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

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
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private static final String TAG = "MusicService";
    private MediaPlayer player;
    private static int model = 0;                           //  当前播放模式
    private static ArrayList<Music> data_music;             //  歌曲资源
    private static int currentPosition;                     //  当前歌曲位置
    private String url;                                     //  当前歌曲url
    private List<LrcContent> lrcList = new ArrayList<>();   //  存放歌词列表对象
    private int index = 0;                                  //  歌词检索值
    Timer timer = new Timer();
    private boolean startTask;                              //  标志是否启动timer
    private Handler handler = new Handler();
    private int currentTime;                                //  当前播放时间
    private int duration;                                   //  当前音乐时长
    private boolean pause;                                  //  标志是否暂停
    NotificationManager manager;
    Notification build;
    RemoteViews remoteViews;

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (!pause) {
                int currentProgress = player.getCurrentPosition();
                Intent receiver = new Intent("com.iotek.bearmediaplayer.MusicBroadcastReiceiver");
                receiver.putExtra("currentProgress", currentProgress);
                sendBroadcast(receiver);
            }
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
        if (player == null) {
            player = new MediaPlayer();
        }
        player.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (notificationControl(intent)) return super.onStartCommand(intent, flags, startId);
        if (switchSongControl(intent)) return super.onStartCommand(intent, flags, startId);
        playOrpauseControl(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 响应界面播放暂停及改变进度相关事件处理
     * @param intent
     */
    private void playOrpauseControl(Intent intent) {
        String data = intent != null ? intent.getStringExtra("data") : null;
        if (data != null) {
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
            initNotification();
        }
    }

    /**
     * 判断从音乐列表传过来的音乐是否和当前播放的是用一首如果是则继续播放
     * @param intent
     * @return
     */
    private boolean switchSongControl(Intent intent) {
        String url = intent != null ? intent.getStringExtra("url") : null;
        if (url != null) {
            if (url.equals(this.url) && player != null && player.isPlaying()) {
                initLrc(url);
                return true;
            }
            initLrc(url);
            start(url);
            this.url = url;
        }
        return false;
    }

    /**
     * 相应通知相关点击事件的处理
     * @param intent
     * @return
     */
    private boolean notificationControl(Intent intent) {
        String notification = intent != null ? intent.getStringExtra("notification") : null;
        if (notification != null) {
            if (data_music == null || data_music.size() == 0) {
                return true;
            }
            String action = intent.getStringExtra("action");
            Log.i(TAG, "action\t" + action);
            switch (action) {
                case "prev":
                    currentPosition--;
                    if (currentPosition == -1) {
                        currentPosition = 0;
                    }
                    break;
                case "next":
                    currentPosition++;
                    if (currentPosition == data_music.size()) {
                        currentPosition = data_music.size() - 1;
                    }
                    break;
            }
            updateUiWhenNext(currentPosition);
            return true;
        }
        return false;
    }

    @Override
    public boolean stopService(Intent name) {
        Log.i(TAG, "stopService");
        player.release();
        return super.stopService(name);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "onCompletion");
        if (data_music == null || data_music.size() == 0) {
            return;
        }
        switch (model) {
            case 0:
                //顺序播放
                model = 0;
                currentPosition++;
                if (currentPosition == data_music.size()) {
                    pause();
                }
                break;
            case 1:
                //随机播放
                model = 1;
                Random random = new Random();
                currentPosition = random.nextInt(data_music.size());
                break;
            case 2:
                //整体循环
                model = 2;
                currentPosition++;
                if (currentPosition == data_music.size()) {
                    currentPosition = 0;
                }
                break;
            case 3:
                //单曲循环
                model = 3;
                break;
        }
        updateUiWhenNext(currentPosition);
    }

    /**
     * 当切换下首歌时更新ui
     */
    private void updateUiWhenNext(int position) {
        if (position != data_music.size()) {
            setPositionPlay(position);
        }
        //每次播放结束开始下一首歌时发送广播更新界面
        Intent receiver = new Intent("com.iotek.bearmediaplayer.MusicBroadcastReiceiver");
        receiver.putExtra("next", position == data_music.size() ? currentPosition-- : position);
        sendBroadcast(receiver);
    }

    /**
     * 播放前准备
     *
     * @param url 音乐文件url
     */
    private void start(String url) {
        try {
            if (player != null) {
                player.reset();
                player.setDataSource(url);
                player.prepare();
                player.setOnPreparedListener(this);
                play();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    private void play() {
        if (player != null) {
            player.start();
        }
        //计时器开始工作，更新进度
        if (!startTask) {
            timer.schedule(task, 1000, 1000);
            startTask = true;
        }
        pause = false;
    }

    /**
     * 暂停
     */
    private void pause() {
        if (player != null) {
            player.pause();
            sendChangeSingFlagReceiver(player.isPlaying(), currentPosition);
        }
        pause = true;

    }

    /**
     * 播放下一个位置的音乐
     *
     * @param currentPosition
     */
    private void setPositionPlay(int currentPosition) {
        Music music = data_music.get(currentPosition);
        url = music.getUrl();
        start(url);
        initLrc(url);
    }

    /**
     * 初始化歌词
     *
     * @param url
     */
    public void initLrc(String url) {
        LrcProcess mLrcProcess = new LrcProcess();
        //读取歌词文件
        mLrcProcess.readLRC(url);

        //传回处理后的歌词文件
        lrcList = mLrcProcess.getLrcList();
        MusicPlayerActivity.lrcView.setmLrcList(lrcList);
        if (lrcList == null || lrcList.size() == 0) {
            MusicPlayerActivity.lrcView.invalidate();
            return;
        }
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
     *
     * @return
     */
    public int lrcIndex() {
        if (player.isPlaying()) {
            currentTime = player.getCurrentPosition();
            duration = player.getDuration();
        }
        if (currentTime < duration) {
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

    /**
     * 发送改变歌曲标志的广播，用于界面显示歌曲是否在播放
     * @param play
     * @param position
     */
    private void sendChangeSingFlagReceiver(boolean play, int position) {
        Intent intent = new Intent("com.wh.changesingflag");
        intent.putExtra("position", position);
        intent.putExtra("play", play);
        sendBroadcast(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        initNotification();
        sendChangeSingFlagReceiver(mp.isPlaying(), currentPosition);
    }

    public static class MusicServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //将音乐文件传入
            ArrayList<Music> data = intent.getParcelableArrayListExtra("data_music");
            int firstPosition = intent.getIntExtra("firstPosition", -1);
            if (data != null) {
                data_music = data;
            }
            if (firstPosition != -1) {
                currentPosition = firstPosition;
            }
            //改变播放模式
            int music_model = intent.getIntExtra("music_model", -1);
            if (music_model != -1) {
                switch (music_model) {
                    case 0:
                        //顺序播放
                        model = 0;
                        break;
                    case 1:
                        //随机播放
                        model = 1;
                        break;
                    case 2:
                        //整体循环
                        model = 2;
                        break;
                    case 3:
                        //单曲循环
                        model = 3;
                        break;
                }
            }

        }
    }

    /**
     * 显示通知，并且处理相关通知控制音乐
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initNotification() {
        if (data_music == null || data_music.size() == 0 || player == null) {
            return;
        }
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Music music = data_music.get(currentPosition);
        builder.setContentIntent(PendingIntent.getActivity(this, 0x001, new Intent(this, this.getClass()), PendingIntent.FLAG_UPDATE_CURRENT));

        //  设置客制的notification的相关显示资源
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        remoteViews.setImageViewResource(R.id.notification_music_icon, R.drawable.media);
        remoteViews.setTextViewText(R.id.notification_music_title, music.getTilte());
        remoteViews.setTextColor(R.id.notification_music_title, Color.BLACK);
        remoteViews.setTextViewText(R.id.notification_music_artist, music.getArtist());
        remoteViews.setTextColor(R.id.notification_music_artist, Color.BLACK);
        remoteViews.setImageViewResource(R.id.notification_music_preview, android.R.drawable.ic_media_rew);
        remoteViews.setImageViewResource(R.id.notification_music_next, android.R.drawable.ic_media_ff);
        //   设置notification上各view的点击事件
        Intent play_service = new Intent(this, MusicService.class);
        if (player.isPlaying()) {
            Log.i(TAG, "isPlaying");
            play_service.putExtra("data", "pause");
            remoteViews.setImageViewResource(R.id.notification_music_play, android.R.drawable.ic_media_pause);
        } else {
            Log.i(TAG, "isPlaying\telse");
            play_service.putExtra("data", "play");
            remoteViews.setImageViewResource(R.id.notification_music_play, android.R.drawable.ic_media_play);
        }
        remoteViews.setOnClickPendingIntent(R.id.notification_music_play, PendingIntent.getService(this, 0x0010, play_service, PendingIntent.FLAG_UPDATE_CURRENT));
        Intent prev_service = new Intent(this, MusicService.class);
        prev_service.putExtra("notification", "notification");
        prev_service.putExtra("action", "prev");
        remoteViews.setOnClickPendingIntent(R.id.notification_music_preview, PendingIntent.getService(this, 0x0011, prev_service, PendingIntent.FLAG_UPDATE_CURRENT));
        Intent next_service = new Intent(this, MusicService.class);
        next_service.putExtra("notification", "notification");
        next_service.putExtra("action", "next");
        remoteViews.setOnClickPendingIntent(R.id.notification_music_next, PendingIntent.getService(this, 0x0012, next_service, PendingIntent.FLAG_UPDATE_CURRENT));

        builder.setContent(remoteViews)
                .setSmallIcon(R.drawable.media)                    //  通知出现时显示何图标
                .setTicker(music.getTilte() + "\t正在播放...")      //  通知出现时显示何提示
                .setContentIntent(
                        PendingIntent.getActivity(
                                this,
                                0x0012,
                                new Intent(this, MainActivity.class)
                                        .putExtra("currentPosition", currentPosition),
                                PendingIntent.FLAG_UPDATE_CURRENT));

        build = builder.build();
        build.flags |= Notification.FLAG_INSISTENT;

        manager.notify(build.flags, build);
        new EditText(this).setLayoutParams(new LinearLayout.LayoutParams(240, 60));
    }

}

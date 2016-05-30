package com.wh.bear.mediaplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wh.bear.mediaplayer.bean.LrcView;
import com.wh.bear.mediaplayer.bean.Music;
import com.wh.bear.mediaplayer.utils.MediaKeeper;
import com.wh.bear.mediaplayer.utils.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Administrator on 15-10-12.
 */
public class MusicPlayerActivity extends Activity {

    private static final String TAG = "MusicPlayerActivity";
    private static final int PROGRESS_UPDATE = 0x101;
    private static final int DURATION_UPDATE = 0x102;
    private static final int PLAY_OVER = 0x103;
    private static final int MUSIC_ON_PAUSE = 0x104;

    LinearLayout music_layout;
    public static LrcView lrcView;
    ImageButton back, play_model, music_preview, music_next;
    static ImageButton music_play;
    static TextView title;
    static TextView music_currentTime, music_endTime;
    static SeekBar music_progress;
    static ArrayList<Music> data_music;
    int firstPosition;                                          //第一次开始播放的位置
    static int duration;
    static int currentPosition;                                 //当前播放音乐的位置
    private static int currentProgress;                         //当前进度
    int music_model = 0;
    PauseListener listener;
    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_UPDATE:
                    music_progress.setProgress(currentProgress);
                    try {
                        music_currentTime.setText(StringUtils.getMusicDuration(currentProgress));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case DURATION_UPDATE:
                    Music music = data_music.get(currentPosition);
                    initStartView(music.getDuration(), music.getTilte());
                    break;
                case PLAY_OVER:
                    music_play.setImageResource(android.R.drawable.ic_media_play);
                    music_progress.setProgress(0);
                    lrcView.setmLrcList(null);
                    lrcView.invalidate();
                    currentPosition = data_music.size() -1;
                    break;
                case MUSIC_ON_PAUSE:
                    if (music_play != null) {
                        boolean play = (boolean) msg.obj;
                        if (play) {
                            music_play.setImageResource(android.R.drawable.ic_media_pause);
                        } else {
                            music_play.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_layout);

        music_layout = (LinearLayout) findViewById(R.id.music_layout);
        back = (ImageButton) findViewById(R.id.back);
        play_model = (ImageButton) findViewById(R.id.play_model);
        music_preview = (ImageButton) findViewById(R.id.music_preview);
        music_next = (ImageButton) findViewById(R.id.music_next);
        music_play = (ImageButton) findViewById(R.id.music_play);
        title = (TextView) findViewById(R.id.music_title);
        music_currentTime = (TextView) findViewById(R.id.music_currentTime);
        music_endTime = (TextView) findViewById(R.id.music_endTime);
        music_progress = (SeekBar) findViewById(R.id.music_progress);
        lrcView = (LrcView) findViewById(R.id.lrcShowView);

        //读取主题
        int themeId = MediaKeeper.readTheme(this);
        changeTheme(music_layout, themeId);
        //读取播放模式
        int model = MediaKeeper.readPlaymodel(this);
        initModel(model);
        //设置电话监听
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // 获取系统服务
        telManager.listen(new MobliePhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);

        Intent intent = getIntent();
        data_music = intent.getParcelableArrayListExtra("data_music");
        firstPosition = intent.getIntExtra("firstPosition", 0);
        currentPosition = firstPosition;

        Music music = data_music.get(firstPosition);
        duration = music.getDuration();
        //初始化播放界面
        initStartView(duration, music.getTilte());

        //启动service
        Intent service = new Intent(MusicPlayerActivity.this, MusicService.class);
        service.putExtra("url", music.getUrl());
        startService(service);
        //发送首次广播传递数据
        Intent receiver = new Intent("com.wh.bear.mediaplayer.MusicServiceReceiver");
        receiver.putParcelableArrayListExtra("data_music", data_music);
        receiver.putExtra("firstPosition", firstPosition);

        receiver.putExtra("music_model", music_model);
        sendBroadcast(receiver);

        //播放按钮事件
        music_play.setOnClickListener(new View.OnClickListener() {
            int count = 0;

            @Override
            public void onClick(View v) {

                if (count % 2 == 0) {
                    music_play.setImageResource(android.R.drawable.ic_media_play);
                    count++;
                    playOnpause();
                } else {
                    music_play.setImageResource(android.R.drawable.ic_media_pause);
                    count++;
                    playOnstart();
                }
            }
        });
        /**
         * 上一首
         */
        music_preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currentPosition >= 1) {
                    currentPosition--;
                }
                Music m = data_music.get(currentPosition);
                duration = m.getDuration();
                initStartView(duration, m.getTilte());
                Intent service = new Intent(MusicPlayerActivity.this, MusicService.class);
                service.putExtra("url", m.getUrl());
                startService(service);

                Intent receiver = new Intent("com.iotek.bearmediaplayer.MusicServiceReceiver");
                receiver.putExtra("firstPosition", currentPosition);

                sendBroadcast(receiver);
            }
        });
        /**
         * 下一首
         */
        music_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPosition < data_music.size() - 1) {
                    currentPosition++;
                }
                Music m = data_music.get(currentPosition);
                duration = m.getDuration();
                initStartView(duration, m.getTilte());
                Intent service = new Intent(MusicPlayerActivity.this, MusicService.class);
                service.putExtra("url", m.getUrl());
                startService(service);

                Intent receiver = new Intent("com.iotek.bearmediaplayer.MusicServiceReceiver");
                receiver.putExtra("firstPosition", currentPosition);

                sendBroadcast(receiver);
            }
        });

        /**
         * 进度条更新
         */
        music_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                Intent intent = new Intent(MusicPlayerActivity.this, MusicService.class);
                intent.putExtra("data", "changeProgress");
                intent.putExtra("progress", progress);
                startService(intent);
            }
        });

        play_model.setOnClickListener(new View.OnClickListener() {
            int count = 0;
            @Override
            public void onClick(View v) {
                if (count % 4 == 0) {
                    //随机播放
                    play_model.setImageResource(R.drawable.toggle);
                    count++;
                    music_model = 1;
                    setCurrentModel(music_model);
                } else if (count % 4 == 1) {
                    //整体循环
                    play_model.setImageResource(R.drawable.repeat);
                    count++;
                    music_model = 2;
                    setCurrentModel(music_model);
                } else if (count % 4 == 2) {
                    //单曲循环
                    play_model.setImageResource(R.drawable.repeatone);
                    count++;
                    music_model = 3;
                    setCurrentModel(music_model);
                } else if (count % 4 == 3) {
                    //顺序播放
                    play_model.setImageResource(R.drawable.straigthplay);
                    count++;
                    music_model = 0;
                    setCurrentModel(music_model);
                }
            }
        });
        /**
         * 返回键
         */
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MusicPlayerActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        listener = new PauseListener();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("com.wh.changesingflag");
        registerReceiver(listener, filter1);
    }

    /**
     * 初始化播放界面
     *
     * @param duration
     * @param text
     */
    private static void initStartView(int duration, String text) {
        title.setText(text);
        music_progress.setMax(duration);
        music_play.setImageResource(android.R.drawable.ic_media_pause);
        try {
            music_endTime.setText(StringUtils.getMusicDuration(duration));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置当前音乐播放模式
     *
     * @param currentModel
     */
    public void setCurrentModel(int currentModel) {
        Intent receiver = new Intent("com.iotek.bearmediaplayer.MusicServiceReceiver");
        receiver.putExtra("music_model", currentModel);
        sendBroadcast(receiver);
    }

    /**
     * 每次启动activity时初始化播放模式及图标
     *
     * @param model
     */
    public void initModel(int model) {
        switch (model) {
            case 0:
                play_model.setImageResource(R.drawable.straigthplay);
                break;
            case 1:
                play_model.setImageResource(R.drawable.toggle);
                break;
            case 2:
                play_model.setImageResource(R.drawable.repeat);
                break;
            case 3:
                play_model.setImageResource(R.drawable.repeatone);
                break;
        }
        music_model = model;
        setCurrentModel(model);
    }

    public static class MusicBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //从service发送过来，时时更新进度
            currentProgress = intent.getIntExtra("currentProgress", 0);
            Log.i(TAG,"currentProgress\t" + currentProgress);
            handler.sendEmptyMessage(PROGRESS_UPDATE);
            //当service自动播放时，用于更新界面的广播
            int next = intent.getIntExtra("next", -1);
            if (next != -1) {
                currentPosition = next;
                if (data_music != null && currentPosition == data_music.size()){
                    handler.sendEmptyMessage(PLAY_OVER);
                } else {
                    handler.sendEmptyMessage(DURATION_UPDATE);
                }
            }
        }
    }

    /**
     * 发送广播开始音乐
     */
    public void playOnstart() {
        Intent intent = new Intent(MusicPlayerActivity.this, MusicService.class);
        intent.putExtra("data", "play");
        startService(intent);
    }

    /**
     * 发送广播暂停音乐
     */
    public void playOnpause() {
        Intent intent = new Intent(MusicPlayerActivity.this, MusicService.class);
        intent.putExtra("data", "pause");
        startService(intent);
    }

    /**
     * 改变主题
     *
     * @param main_layout
     * @param themeId
     */
    private void changeTheme(LinearLayout main_layout, int themeId) {
        switch (themeId) {
            case 1:
                main_layout.setBackgroundResource(R.drawable.p1);
                break;
            case 2:
                main_layout.setBackgroundResource(R.drawable.p2);
                break;
            case 3:
                main_layout.setBackgroundResource(R.drawable.p3);
                break;
            case 4:
                main_layout.setBackgroundResource(R.drawable.p4);
                break;
            case 5:
                main_layout.setBackgroundResource(R.drawable.p5);
                break;
            case 6:
                main_layout.setBackgroundResource(R.drawable.p6);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaKeeper.writePlaymodel(this, music_model);
        if (listener != null) {
            unregisterReceiver(listener);
        }
    }

    /**
     * @author 电话监听器类
     */
    private class MobliePhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
                    playOnstart();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:    //通话状态
                case TelephonyManager.CALL_STATE_RINGING:    //响铃状态
                    playOnpause();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 监听系统音量变化而改变音量进度的广播
     */
    private class PauseListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //当音乐暂停时改变播放按钮样式
            if (intent.getAction().equals("com.wh.changesingflag")) {
                boolean play = intent.getBooleanExtra("play",false);
                Log.i(TAG,"play\t" + play);
                Message msg = handler.obtainMessage(MUSIC_ON_PAUSE);
                msg.obj = play;
                handler.sendMessage(msg);
            }
        }
    }
}

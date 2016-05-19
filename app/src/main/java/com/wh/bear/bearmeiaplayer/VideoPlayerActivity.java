package com.wh.bear.bearmeiaplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wh.bear.bearmeiaplayer.adapter.OnSeekBarChangeListenerAdapter;
import com.wh.bear.bearmeiaplayer.adapter.PlayerListAdapter;
import com.wh.bear.bearmeiaplayer.bean.Video;
import com.wh.bear.bearmeiaplayer.utils.MediaKeeper;
import com.wh.bear.bearmeiaplayer.utils.SQLiteOptionHelper;
import com.wh.bear.bearmeiaplayer.utils.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;


/**
 * Created by Administrator on 15-9-28.
 */
public class VideoPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, View.OnTouchListener, MediaPlayer.OnCompletionListener,
        View.OnClickListener, SurfaceHolder.Callback, AdapterView.OnItemClickListener {
    private static final String TAG = "VideoPlayerActivity";

    private static final int HIDE_STATUS_BAR = 0x001;
    private static final int UPDATE_PROGRESS = 0x002;

    SurfaceView player_screen;
    LinearLayout ctr_layout,video_play_header_view;
    SeekBar media_progress, sound_progress;
    ImageButton btn_sound, btn_rew, btn_previous, btn_play, btn_next, btn_ff;
    ImageButton full_screen;                        //  全屏按钮,视频铺满屏幕
    Button btn_float_play,btn_back;
    TextView current_time, end_time;
    View videoList;
    MediaPlayer player;
    Display currDisplay;
    AudioManager mAudioManager;
    private int vWidth;                                             //  视频宽
    private int vHeight;                                            //  视频高
    int currentProgress;                                            //  播放视频的当前进度
    long duration;                                                  //  当前视频的时长
    ListView video_list;                                            //  播放列表
    Button btn_ctr_list;                                            //  控制列表隐藏显示
    ArrayList<Video> data;                                          //  列表数据源
    int currentPosition;                                            //  当前播放视频的位置
    PlayerListAdapter adapter;
    boolean videoListOpen = false;                                  //  视频列表是否在显示
    boolean isFullScreen;
    VolumeReceiver mVolumeReceiver;                                 //  音量监听

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //控制栏自动隐藏
                case HIDE_STATUS_BAR:
                    ctr_layout.setVisibility(View.GONE);
                    btn_ctr_list.setVisibility(View.GONE);
                    video_play_header_view.setVisibility(View.INVISIBLE);
                    break;
                //进度条自动更新
                case UPDATE_PROGRESS:
                    try {
                        currentProgress = player.getCurrentPosition();
                        Log.i(TAG, "currentProgress\t" + currentProgress);
                        media_progress.setProgress(currentProgress);
                        current_time.setText(StringUtils.getVideoDuration(currentProgress));
                        if (currentProgress != duration) {
                            sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);
        initUi();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (savedInstanceState != null) {
            currentProgress = savedInstanceState.getInt("currentProgress");
        }

        Bundle bundle = getIntent().getExtras();
        data = bundle.getParcelableArrayList("data_video");
        currentPosition = bundle.getInt("firstPosition");

        Video video = data.get(currentPosition);
        if (video.getUrl() != null) {
            video.on = true;
            duration = video.getDuration();                             //获得视频时长
            initStartView(duration);
            //列表视图设置
            setVideoList();
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setOnSeekCompleteListener(this);
            player.setOnErrorListener(this);
            player.setOnCompletionListener(this);
            SurfaceHolder holder = player_screen.getHolder();
            holder.addCallback(this);
        }
        /**
         * 屏幕点击事件
         */
        player_screen.setOnClickListener(this);
        player_screen.setOnTouchListener(this);
        /**
         * 声音控制，总控制，静音
         */
        btn_sound.setOnClickListener(this);
        /**
         * 快退按钮
         */
        btn_rew.setOnClickListener(this);
        /**
         * 上一个视频
         */
        btn_previous.setOnClickListener(this);
        /**
         * 下一首
         */
        btn_next.setOnClickListener(this);
        /**
         * 快进按钮
         */
        btn_ff.setOnClickListener(this);
        /**
         * 播放按钮点击
         */
        btn_play.setOnClickListener(this);

        btn_back.setOnClickListener(this);
        /**
         * 悬浮播放
         */
        btn_float_play.setOnClickListener(this);
        /**
         * 进度条控制
         */
        media_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    current_time.setText(StringUtils.getVideoDuration(progress));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (handler.hasMessages(UPDATE_PROGRESS)) {
                    handler.removeMessages(UPDATE_PROGRESS);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                player.seekTo(progress);
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        });
        /**
         * 音量控制，局部
         */
        sound_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });

        registerVolumeReceiver();
    }

    private void initUi() {
        player_screen = (SurfaceView) findViewById(R.id.player_screen);
        ctr_layout = (LinearLayout) findViewById(R.id.ctr_layout);

        media_progress = (SeekBar) findViewById(R.id.media_progress);
        sound_progress = (SeekBar) findViewById(R.id.sound_progress);

        btn_sound = (ImageButton) findViewById(R.id.btn_sound);
        btn_rew = (ImageButton) findViewById(R.id.btn_rew);
        btn_previous = (ImageButton) findViewById(R.id.btn_previous);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_ff = (ImageButton) findViewById(R.id.btn_ff);

        current_time = (TextView) findViewById(R.id.current_time);
        end_time = (TextView) findViewById(R.id.end_time);

        video_list = (ListView) findViewById(R.id.video_list);
        btn_ctr_list = (Button) findViewById(R.id.btn_ctr_list);

        full_screen = (ImageButton) findViewById(R.id.full_screen);

        videoList = findViewById(R.id.list_layout);

        video_play_header_view = (LinearLayout) findViewById(R.id.video_play_header_view);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_float_play = (Button) findViewById(R.id.btn_float_play);

        currDisplay = getWindowManager().getDefaultDisplay();
        // fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 列表初始化及控制设置
     */
    private void setVideoList() {
        if (data != null) {
            adapter = new PlayerListAdapter(this, data, currentPosition);
            video_list.setAdapter(adapter);

            video_list.setOnItemClickListener(this);
            /**
             * 控制播放列表显示和隐藏
             */
            btn_ctr_list.setOnClickListener(this);
            /**
             * 控制视频尺寸是否铺满屏幕
             */
            full_screen.setOnClickListener(this);

        }
    }

    /**
     * 当按上一首或者下一首时更新列表
     *
     * @param position 切换的歌曲位置
     * @param adapter 含有资源的适配器
     */
    private void changeVideoAndUpdateList(int position, PlayerListAdapter adapter) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).on = i == position;
        }
        adapter.setData(data);
        adapter.notifyDataSetChanged();
        //  播放当前点中项
        try {
            player.reset();
            startVideo(data.get(position).getUrl());
            duration = data.get(position).getDuration();
            initStartView(duration);
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     *
     * @param url 文件路径
     * @throws IOException
     */
    private void startVideo(String url) throws IOException {
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(url);
        player.prepareAsync();
    }

    /**
     * 播放准备，设置视频大小
     *
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        vWidth = mp.getVideoWidth();
        vHeight = mp.getVideoHeight();
        if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {

            float wRatio = (float) vWidth / (float) currDisplay.getWidth();
            float hRatio = (float) vHeight / (float) currDisplay.getHeight();

            float ratio = Math.max(hRatio, wRatio);

            vWidth = (int) Math.ceil((float) vWidth / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);
            player_screen.setLayoutParams(new FrameLayout.LayoutParams(vWidth, vHeight, Gravity.CENTER));
            mp.start();
            //视频每次播放时跳转到历史位置
            currentProgress = data.get(currentPosition).getCurrentProgress();
            Log.i(TAG, "currentProgress\t" + currentProgress);
            mp.seekTo(currentProgress);

            updateMediaProgress(mp);
        } else {
            mp.start();
            //视频每次播放时跳转到历史位置
            currentProgress = data.get(currentPosition).getCurrentProgress();
            Log.i(TAG, "currentProgress\t" + currentProgress);
            mp.seekTo(currentProgress);
            updateMediaProgress(mp);
        }
    }


    /**
     * 拖动进度条后
     *
     * @param mp
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // TODO: 15-10-9

    }

    /**
     * 视频播放结束之后
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play.setImageResource(android.R.drawable.ic_media_play);
        finish();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }

    public void updateMediaProgress(MediaPlayer mp) {
        if (mp.isPlaying()) {
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    /**
     * 初始化播放界面
     */
    public void initStartView(long duration) {
        if (player != null && !player.isPlaying()) {
            btn_play.setImageResource(android.R.drawable.ic_media_pause);
        }

        //  设置进度条最大值
        Log.i(TAG, "initStartView\tduration" + ((int) duration));
        media_progress.setMax((int) duration);
        media_progress.setProgress(0);
        //  设置声音进度条最大值
        sound_progress.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        //  设置进度条当前值
        sound_progress.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        try {
            end_time.setText(StringUtils.getVideoDuration(duration));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //屏幕被覆盖之后播放停止，进度条停止更新
        try {
            player.pause();
            if (handler.hasMessages(UPDATE_PROGRESS)) {
                handler.removeMessages(UPDATE_PROGRESS);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            player.start();
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消静音
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        if (handler.hasMessages(UPDATE_PROGRESS)) {
            handler.removeMessages(UPDATE_PROGRESS);
        }
        //将数据储存进入数据库
        SQLiteOptionHelper helper = new SQLiteOptionHelper(this, "videos", 1);          //  数据库操作助手
        Video video = data.get(currentPosition);
        int l = helper.updateVideoProgress(video.getTitle(), currentProgress);
        if (l <= 0) {
            video.setCurrentProgress(currentProgress);
            helper.setVideo(video);
        }
        if (mVolumeReceiver != null) {
            unregisterReceiver(mVolumeReceiver);
        }
    }

    float downX;
    float downY;
    int currentSound;//当前音量

    /**
     * 播放屏幕触屏事件
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float moveX;
        float moveY;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                currentSound = sound_progress.getProgress();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                int diffX = (int) (moveX - downX);
                int diffY = (int) (moveY - downY);
                //手势改变视频进度
                if (diffX > 10 || diffX < -10) {
                    currentProgress += diffX * 2;
                    media_progress.setProgress(currentProgress);
                    player.seekTo(currentProgress);

                }
                //手势改变视频声音和亮度
                if (diffY > 5 || diffY < -5) {
                    if (downX > dm.widthPixels / 2) {
                        sound_progress.setProgress(currentSound - diffY / 50);
                    } else {
                        setBrightness(-diffY);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:

                break;

        }
        return false;
    }

    /**
     * 设置屏幕亮度
     *
     * @param brightness 亮度参数
     */
    public void setBrightness(float brightness) {

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_screen:
                ctr_layout.setVisibility(View.VISIBLE);
                btn_ctr_list.setVisibility(View.VISIBLE);
                video_play_header_view.setVisibility(View.VISIBLE);
                if (handler.hasMessages(HIDE_STATUS_BAR)) {
                    handler.removeMessages(HIDE_STATUS_BAR);
                }
                handler.sendEmptyMessageDelayed(HIDE_STATUS_BAR, 5000);
                break;
            case R.id.btn_rew:
                if (player != null && currentProgress >= 5000) {
                    if (handler.hasMessages(UPDATE_PROGRESS)) {
                        handler.removeMessages(UPDATE_PROGRESS);
                    }
                    currentProgress -= 5000;
                    player.seekTo(currentProgress);
                    handler.sendEmptyMessage(UPDATE_PROGRESS);
                }
                break;
            case R.id.btn_previous:
                if (player != null && currentPosition >= 1) {
                    btn_play.setImageResource(android.R.drawable.ic_media_pause);
                    currentPosition -= 1;
                    changeVideoAndUpdateList(currentPosition, adapter);
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "已经是第一个", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_next:
                if (player != null && currentPosition < data.size() - 1) {
                    btn_play.setImageResource(android.R.drawable.ic_media_pause);
                    currentPosition += 1;
                    changeVideoAndUpdateList(currentPosition, adapter);
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "已经是最后一个", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_ff:
                if (player != null && currentProgress <= duration - 5000) {
                    if (handler.hasMessages(UPDATE_PROGRESS)) {
                        handler.removeMessages(UPDATE_PROGRESS);
                    }
                    currentProgress += 5000;
                    player.seekTo(currentProgress);
                    handler.sendEmptyMessage(UPDATE_PROGRESS);
                }
                break;
            case R.id.btn_play:
                if (player != null) {
                    if (player.isPlaying()) {
                        ((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
                        player.pause();
                    } else {
                        ((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
                        player.start();
                    }
                }

                break;
            case R.id.btn_sound:
                int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (streamVolume == 0) {
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    sound_progress.setProgress(MediaKeeper.readCurrentStreamVolume(VideoPlayerActivity.this));
                } else {
                    MediaKeeper.writeCurrentStreamVolume(VideoPlayerActivity.this, streamVolume);
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_lock_silent_mode);
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    sound_progress.setProgress(0);
                }
                break;
            case R.id.btn_ctr_list:

                if (!videoListOpen) {
                    videoList.setVisibility(View.VISIBLE);
                    videoListOpen = true;
                    btn_ctr_list.setText(R.string.hide);
                } else {
                    videoList.setVisibility(View.GONE);
                    videoListOpen = false;
                    btn_ctr_list.setText(R.string.show);
                }
                break;
            case R.id.full_screen:
                if (isFullScreen) {
                    vWidth = player.getVideoWidth();
                    vHeight = player.getVideoHeight();
                    if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {

                        float wRatio = (float) vWidth / (float) currDisplay.getWidth();
                        float hRatio = (float) vHeight / (float) currDisplay.getHeight();

                        float ratio = Math.max(hRatio, wRatio);

                        vWidth = (int) Math.ceil((float) vWidth / ratio);
                        vHeight = (int) Math.ceil((float) vHeight / ratio);
                        player_screen.setLayoutParams(new FrameLayout.LayoutParams(vWidth, vHeight, Gravity.CENTER));
                    }
                    isFullScreen = false;
                } else {
                    player_screen.setLayoutParams(new FrameLayout.LayoutParams(currDisplay.getWidth(), currDisplay.getHeight(), Gravity.CENTER));
                    isFullScreen = true;
                }
                break;
            case R.id.btn_float_play:
                startActivity(new Intent(this,FloatVideoPlayService.class).putExtra("video",data.get(currentPosition)));
            case R.id.btn_back:
                finish();
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        player.setDisplay(holder);
        player.reset();
        try {
            startVideo(data.get(currentPosition).getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.release();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        changeVideoAndUpdateList(position, adapter);
    }

    /**
     * 注册音量监听
     */
    private void registerVolumeReceiver() {
        mVolumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 监听系统音量变化而改变音量进度的广播
     */
    private class VolumeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                sound_progress.setProgress(currVolume);
            }
        }
    }


}

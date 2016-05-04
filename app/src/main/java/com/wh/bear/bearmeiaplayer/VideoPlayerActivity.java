package com.wh.bear.bearmeiaplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wh.bear.bearmeiaplayer.adapter.PlayerListAdapter;
import com.wh.bear.bearmeiaplayer.bean.Video;
import com.wh.bear.bearmeiaplayer.utils.SQLiteOptionHelper;
import com.wh.bear.bearmeiaplayer.utils.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;


/**
 * Created by Administrator on 15-9-28.
 */
public class VideoPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, View.OnTouchListener,MediaPlayer.OnCompletionListener{
    SurfaceView player_screen;
    LinearLayout ctr_layout;
    SeekBar media_progress, sound_progress;
    ImageButton btn_sound, btn_rew, btn_previous, btn_play, btn_next, btn_ff;
    ImageButton full_screen;//全屏按钮,视频铺满屏幕
    TextView current_time, end_time;
    MediaPlayer player;
    Display currDisplay;
    AudioManager manager;
    private int vWidh;//视频宽
    private int vHeight;//视频高
    int currentProgress;//播放视频的当前进度
    long duration;//当前视频的时长
    ListView video_list;//播放列表
    Button btn_ctr_list;//控制列表隐藏显示
    ArrayList<Video> data;//列表数据源
    int firstPosition;//第一次播放视频的位置
    int currentPosition;//当前播放视频的位置
    SQLiteOptionHelper helper;//数据库操作助手
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //控制栏自动隐藏
            if (msg.what == 0x001) {
                ctr_layout.setVisibility(View.GONE);
                btn_ctr_list.setVisibility(View.GONE);
            }
            //进度条自动更新
            if (msg.what == 0x002) {
                currentProgress = player.getCurrentPosition();
                media_progress.setProgress(currentProgress);
                try {
                    current_time.setText(StringUtils.getVideoDuration(currentProgress));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (currentProgress != duration) {
                    sendEmptyMessageDelayed(0x002, 1000);
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);
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

        full_screen= (ImageButton) findViewById(R.id.full_screen);

        currDisplay = getWindowManager().getDefaultDisplay();
        // fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        manager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (savedInstanceState != null) {
            currentProgress = savedInstanceState.getInt("currentProgress");
        }
        helper=new SQLiteOptionHelper(this,"vedios",1);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        data = bundle.getParcelableArrayList("data_video");

        firstPosition = bundle.getInt("firstPosition");
        currentPosition = firstPosition;
        final String url = data.get(firstPosition).getUrl();
        duration = data.get(firstPosition).getDuration();//获得视频时长
        initStartView(duration);
        //列表视图设置
        setVideoList();

        if (url != null) {
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setOnSeekCompleteListener(this);
            player.setOnErrorListener(this);
            player.setOnCompletionListener(this);
            SurfaceHolder holder = player_screen.getHolder();

            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    player.setDisplay(holder);
                    try {
                        startVideo(url);
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
            });


        }
        /**
         * 屏幕点击事件
         */
        player_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctr_layout.setVisibility(View.VISIBLE);
                btn_ctr_list.setVisibility(View.VISIBLE);
                if (handler.hasMessages(0x001)) {
                    handler.removeMessages(0x001);
                }
                handler.sendEmptyMessageDelayed(0x001, 5000);
            }
        });
        player_screen.setOnTouchListener(this);
        /**
         * 声音控制，总控制，静音
         */
        btn_sound.setOnClickListener(new View.OnClickListener() {
            int count = 0;

            @Override
            public void onClick(View v) {
                if (count % 2 == 0) {
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_lock_silent_mode);
                    count++;
                    manager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    sound_progress.setProgress(0);
                } else {
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    count++;
                    manager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    sound_progress.setProgress(manager.getStreamVolume(AudioManager.STREAM_MUSIC));
                }
            }
        });
        /**
         * 快退按钮
         */
        btn_rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (player != null && currentProgress >= 5000) {
                    if (handler.hasMessages(0x002)) {
                        handler.removeMessages(0x002);
                    }
                    currentProgress -= 5000;
                    player.seekTo(currentProgress);
                    handler.sendEmptyMessage(0x002);
                }
            }
        });
        /**
         * 上一个视频
         */
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null && currentPosition >= 1) {
                    try {
                        currentPosition -= 1;
                        String url = data.get(currentPosition).getUrl();
                        player.reset();
                        startVideo(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /**
         * 下一首
         */
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null && currentPosition < data.size() - 1) {
                    try {
                        currentPosition += 1;
                        String url = data.get(currentPosition).getUrl();
                        player.reset();
                        startVideo(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /**
         * 快进按钮
         */
        btn_ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null && currentProgress <= duration - 5000) {
                    if (handler.hasMessages(0x002)) {
                        handler.removeMessages(0x002);
                    }
                    currentProgress += 5000;
                    player.seekTo(currentProgress);
                    handler.sendEmptyMessage(0x002);
                }
            }
        });
        /**
         * 播放按钮点击
         */
        btn_play.setOnClickListener(new View.OnClickListener() {
            int count = 0;

            @Override
            public void onClick(View v) {
                if (count % 2 == 0) {
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
                    count++;
                    if (player != null) {
                        player.pause();
                    }

                } else {
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
                    count++;
                    if (player != null) {
                        player.start();
                    }
                }
            }
        });
        /**
         * 进度条控制
         */
        media_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                if (handler.hasMessages(0x002)) {
                    handler.removeMessages(0x002);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                player.seekTo(progress);

                handler.sendEmptyMessage(0x002);
            }
        });
        /**
         * 音量控制，局部
         */
        sound_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                manager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    /**
     * 列表初始化及控制设置
     */
    private void setVideoList() {
        if (data != null) {
            PlayerListAdapter adapter = new PlayerListAdapter(this, data, firstPosition);
            video_list.setAdapter(adapter);

            video_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                View currentView;

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //第一次点击如果点击位置和当前播放位置不同
                    if (position != firstPosition && firstPosition != -1) {
                        video_list.getChildAt(firstPosition).findViewById(R.id.play_flag).setVisibility(View.GONE);
                        firstPosition = -1;
                    }
                    view.findViewById(R.id.play_flag).setVisibility(View.VISIBLE);
                    if (currentView != null && view != currentView) {
                        currentView.findViewById(R.id.play_flag).setVisibility(View.GONE);
                    }
                    currentView = view;
                    //播放当前点中项
                    try {
                        player.reset();
                        startVideo(data.get(position).getUrl());
                        duration = data.get(position).getDuration();
                        initStartView(duration);
                        handler.sendEmptyMessage(0x002);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            /**
             * 控制播放列表显示和隐藏
             */
            btn_ctr_list.setOnClickListener(new View.OnClickListener() {
                boolean open = false;

                @Override
                public void onClick(View v) {
                    if (!open) {
                        findViewById(R.id.list_layout).setVisibility(View.VISIBLE);
                        open = true;
                        btn_ctr_list.setText(R.string.hide);
                    } else {
                        findViewById(R.id.list_layout).setVisibility(View.GONE);
                        open = false;
                        btn_ctr_list.setText(R.string.show);
                    }
                }
            });
            /**
             * 控制视频尺寸是否铺满屏幕
             */
            full_screen.setOnClickListener(new View.OnClickListener() {
                int count=0;
                @Override
                public void onClick(View v) {
                    if (count%2==0){
                        player_screen.setLayoutParams(new FrameLayout.LayoutParams(currDisplay.getWidth(), currDisplay.getHeight(), Gravity.CENTER));
                        count++;
                    }else {
                        vWidh = player.getVideoWidth();
                        vHeight = player.getVideoHeight();
                        if (vWidh > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {

                            float wRatio = (float) vWidh / (float) currDisplay.getWidth();
                            float hRatio = (float) vHeight / (float) currDisplay.getHeight();

                            float ratio = Math.max(hRatio, wRatio);

                            vWidh = (int) Math.ceil((float) vWidh / ratio);
                            vHeight = (int) Math.ceil((float) vHeight / ratio);
                            player_screen.setLayoutParams(new FrameLayout.LayoutParams(vWidh, vHeight, Gravity.CENTER));
                        }
                        count++;
                    }
                }
            });

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
        vWidh = mp.getVideoWidth();
        vHeight = mp.getVideoHeight();
        if (vWidh > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {

            float wRatio = (float) vWidh / (float) currDisplay.getWidth();
            float hRatio = (float) vHeight / (float) currDisplay.getHeight();

            float ratio = Math.max(hRatio, wRatio);

            vWidh = (int) Math.ceil((float) vWidh / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);
            player_screen.setLayoutParams(new FrameLayout.LayoutParams(vWidh, vHeight, Gravity.CENTER));
            mp.start();
            //视频每次播放时跳转到历史位置
            currentProgress=data.get(currentPosition).getCurrentProgress();
            mp.seekTo(currentProgress);

            updateMediaprogress(mp);
        } else {
            mp.start();
            //视频每次播放时跳转到历史位置
            currentProgress=data.get(currentPosition).getCurrentProgress();
            mp.seekTo(currentProgress);
            updateMediaprogress(mp);
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
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {

        btn_play.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }

    public void updateMediaprogress(MediaPlayer mp) {
        if (mp.isPlaying()) {
            handler.sendEmptyMessage(0x002);
        }
    }

    /**
     * 初始化播放界面
     */
    public void initStartView(long duration) {
        if (player != null && !player.isPlaying()) {
            btn_play.setImageResource(android.R.drawable.ic_media_pause);
        }

        //设置进度条最大值
        media_progress.setMax((int) duration);
        media_progress.setProgress(0);
        //设置声音进度条最大值
        sound_progress.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        //设置进度条当前值
        sound_progress.setProgress(manager.getStreamVolume(AudioManager.STREAM_MUSIC));

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
        player.pause();
        if (handler.hasMessages(0x002)) {
            handler.removeMessages(0x002);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.start();
        handler.sendEmptyMessage(0x002);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消静音
        manager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        if (handler.hasMessages(0x002)) {
            handler.removeMessages(0x002);
        }
        //将数据储存进入数据库
        Video vedio = data.get(currentPosition);
        int l = helper.updateVedioProgress(vedio.getTitle(), currentProgress);
        if (l<=0){
            vedio.setCurrentProgress(currentProgress);
            helper.setVedio(vedio);
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


}

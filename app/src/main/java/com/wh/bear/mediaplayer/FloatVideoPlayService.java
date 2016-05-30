package com.wh.bear.mediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wh.bear.mediaplayer.adapter.OnSeekBarChangeListenerAdapter;
import com.wh.bear.mediaplayer.bean.Video;
import com.wh.bear.mediaplayer.utils.MediaKeeper;
import com.wh.bear.mediaplayer.utils.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class FloatVideoPlayService extends Activity implements MediaPlayer.OnPreparedListener, OnErrorListener,
        MediaPlayer.OnCompletionListener, View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = "FloatVideoPlayService";
    private static final int HIDE_STATUS_BAR = 0x001011;
    private static final int UPDATE_PROGRESS = 0x001012;

    private MediaPlayer mPlayer;
    private FrameLayout float_play_layout;
    private SurfaceView float_player_screen;
    private TextView float_current_time, float_end_time;
    private SeekBar float_media_progress;
    private LinearLayout float_header_view, float_foot_view;
    private long duration;
    private WindowManager mWindowManager;
    private int vWidth;
    private int vHeight;
    private Video video;
    private int currentProgress;

    private Display currDisplay;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //控制栏自动隐藏
                case HIDE_STATUS_BAR:
                    float_header_view.setVisibility(View.GONE);
                    float_foot_view.setVisibility(View.GONE);
                    break;
                //进度条自动更新
                case UPDATE_PROGRESS:
                    try {
                        if (mPlayer == null) return;
                        currentProgress = mPlayer.getCurrentPosition();
                        Log.i(TAG, "currentProgress\t" + currentProgress);
                        float_media_progress.setProgress(currentProgress);
                        float_current_time.setText(StringUtils.getVideoDuration(currentProgress));
                        if (currentProgress != duration) {
                            sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                        }
                    } catch (ParseException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        initFloatView();
        onBind(getIntent());
    }

    public void onBind(Intent intent) {
        Log.i(TAG, "onBind");

        video = intent.getParcelableExtra("video");
        currentProgress = intent.getIntExtra("currentProgress", 0);
        prepareToPlayVideo(video,currentProgress);
        createFloatView();
    }

    private void prepareToPlayVideo(Video video, int currentProgress) {
        if (video == null) return;
        currDisplay = getWindowManager().getDefaultDisplay();
        duration = video.getDuration();                             //获得视频时长
        initStartView(duration);
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);
        SurfaceHolder holder = float_player_screen.getHolder();
        holder.addCallback(this);

    }

    private void initStartView(long duration) {
        //  设置进度条最大值
        Log.i(TAG, "initStartView\tduration" + ((int) duration));
        float_media_progress.setMax((int) duration);
        float_media_progress.setProgress(0);

        try {
            float_end_time.setText(StringUtils.getVideoDuration(duration));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    WindowManager.LayoutParams wmParams;
    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        Log.i(TAG, "mWindowManager--->" + mWindowManager);
        Log.i(TAG, "FloatView --->  Create start");
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
//        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        float_player_screen.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //      getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - float_play_layout.getMeasuredWidth() / 2;
                Log.i(TAG, "RawX" + event.getRawX());
                Log.i(TAG, "X" + event.getX());
                //      减25为状态栏的高度
                wmParams.y = (int) event.getRawY() - float_play_layout.getMeasuredHeight() / 2 - 25;
                Log.i(TAG, "RawY" + event.getRawY());
                Log.i(TAG, "Y" + event.getY());
                //刷新
                mWindowManager.updateViewLayout(float_play_layout, wmParams);
                return false;  //       此处必须返回false，否则OnClickListener获取不到监听
            }
        });
        //获取浮动窗口视图所在布局
        //添加mFloatLayout
        mWindowManager.addView(float_play_layout, wmParams);

        float_play_layout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        Log.i(TAG, "FloatView --->  Create end");
        this.finish();
    }

    private void initFloatView() {
        LayoutInflater inflater = getLayoutInflater();
        float_play_layout = (FrameLayout) inflater.inflate(R.layout.float_video_play_layout, null);
        float_header_view = (LinearLayout) float_play_layout.findViewById(R.id.float_header_view);
        float_foot_view = (LinearLayout) float_play_layout.findViewById(R.id.float_foot_view);
        float_player_screen = (SurfaceView) float_play_layout.findViewById(R.id.float_player_screen);
        float_current_time = (TextView) float_play_layout.findViewById(R.id.float_current_time);
        float_end_time = (TextView) float_play_layout.findViewById(R.id.float_end_time);
        float_media_progress = (SeekBar) float_play_layout.findViewById(R.id.float_media_progress);

        float_player_screen.setOnClickListener(this);
        ((ImageButton) float_play_layout.findViewById(R.id.btn_close)).setOnClickListener(this);
        ((ImageButton) float_play_layout.findViewById(R.id.float_btn_play)).setOnClickListener(this);
        ((ImageButton) float_play_layout.findViewById(R.id.btn_fullscreen)).setOnClickListener(this);

        /**
         * 进度条控制
         */
        float_media_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    float_current_time.setText(StringUtils.getVideoDuration(progress));
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
                mPlayer.seekTo(progress);
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        mp.release();
        return false;
    }

    public void updateMediaProgress(MediaPlayer mp) {
        if (mp.isPlaying()) {
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPrepared");
        vWidth = mp.getVideoWidth();
        vHeight = mp.getVideoHeight();
        if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {

            float wRatio = (float) vWidth / (float) currDisplay.getWidth();
            float hRatio = (float) vHeight / (float) currDisplay.getHeight();

            float ratio = Math.max(hRatio, wRatio);

            vWidth = (int) Math.ceil((float) vWidth / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);
            float_player_screen.setLayoutParams(new FrameLayout.LayoutParams(vWidth, vHeight, Gravity.CENTER));
            mp.start();
            mp.seekTo(currentProgress);
            updateMediaProgress(mp);
        } else {
            mp.start();
            mp.seekTo(currentProgress);
            updateMediaProgress(mp);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayer != null && mWindowManager != null && float_play_layout != null) {
            mWindowManager.removeViewImmediate(float_play_layout);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        try {
            mPlayer.setDisplay(holder);
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(video.getUrl());
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mPlayer.release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_player_screen:
                float_header_view.setVisibility(View.VISIBLE);
                float_foot_view.setVisibility(View.VISIBLE);
                if (handler.hasMessages(HIDE_STATUS_BAR)) {
                    handler.removeMessages(HIDE_STATUS_BAR);
                }
                handler.sendEmptyMessageDelayed(HIDE_STATUS_BAR, 5000);
                break;
            case R.id.float_btn_play:
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        ((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
                        mPlayer.pause();
                    } else {
                        ((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
                        mPlayer.start();
                    }
                }
                break;
            case R.id.btn_fullscreen:
                Intent intent = new Intent(this,VideoPlayerActivity.class);
                ArrayList<Video> videos = new ArrayList<>();
                videos.add(video);
                intent.putParcelableArrayListExtra("data_video",videos);
                intent.putExtra("firstPosition",0);
                startActivity(intent);
                MediaKeeper.writeVideoHistotyProgress(this,currentProgress,video.getTitle());
            case R.id.btn_close:
                mWindowManager.removeViewImmediate(float_play_layout);
                handler.removeMessages(UPDATE_PROGRESS);
                handler.removeMessages(HIDE_STATUS_BAR);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        vWidth = mPlayer.getVideoWidth();
        vHeight = mPlayer.getVideoHeight();
        if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {

            float wRatio = (float) vWidth / (float) currDisplay.getWidth();
            float hRatio = (float) vHeight / (float) currDisplay.getHeight();

            float ratio = Math.max(hRatio, wRatio);

            vWidth = (int) Math.ceil((float) vWidth / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);
            float_player_screen.setLayoutParams(new FrameLayout.LayoutParams(vWidth, vHeight, Gravity.CENTER));
        }
    }
}

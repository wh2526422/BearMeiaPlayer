package com.wh.bear.bearmeiaplayer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;

import com.wh.bear.bearmeiaplayer.adapter.MusicListAdapter;
import com.wh.bear.bearmeiaplayer.adapter.VideoListAdapter;
import com.wh.bear.bearmeiaplayer.bean.Music;
import com.wh.bear.bearmeiaplayer.bean.Video;
import com.wh.bear.bearmeiaplayer.utils.MediaThemeKeeper;
import com.wh.bear.bearmeiaplayer.utils.SQLiteOptionHelper;

import java.util.ArrayList;

public class
MainActivity extends AppCompatActivity {
    LinearLayout main_layout;
    ListView media_list;
    ImageButton btn_add;
    Switch scanner_switch;
    Button changeTheme;
    ArrayList<Video> data_video = new ArrayList<>();
    ArrayList<Music> data_music = new ArrayList<>();
    boolean ifVedioScanned = false;//标识视频是否已经被扫描过
    boolean ifMusicScanned = false;//标识音乐是否被扫描过
    ChangeMusicSingFlagReceiver receiver;
    MusicListAdapter mAdapter;
    VideoListAdapter vAdapter;
    int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPosition = getIntent().getIntExtra("currentPosition", -1);

        main_layout = (LinearLayout) findViewById(R.id.main_layout);
        media_list = (ListView) findViewById(R.id.media_list);
        btn_add = (ImageButton) findViewById(R.id.btn_add);
        scanner_switch = (Switch) findViewById(R.id.scanner_switch);
        changeTheme = (Button) findViewById(R.id.change_theme);

        int themeId = MediaThemeKeeper.readTheme(this);
        changeTheme(main_layout, themeId);
        receiver = new ChangeMusicSingFlagReceiver();
        registerReceiver(receiver, new IntentFilter("com.wh.changesingflag"));

        //判断开关是在视频端还是音乐端
        if (scanner_switch.isChecked()) {
            scannerVedio();
        } else {
            scannerMusic();
        }
        /**
         * 监听开关的转换
         */
        scanner_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    scannerVedio();
                } else {
                    scannerMusic();
                }
            }
        });
        /**
         * 启动多级目录
         */
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileManager.class);
                startActivity(intent);
            }
        });
        /**
         * 分别启动视频播放页面和音乐播放页面
         */
        media_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                refreshMusicList(position, true);
                //点击视频listview
                if (scanner_switch.isChecked()) {
                    Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("data_video", data_video);
                    bundle.putInt("firstPosition", position);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("data_music", data_music);
                    Log.i("wanghuan", "FirstVisiblePosition\t" + media_list.getFirstVisiblePosition());
                    Log.i("wanghuan", "Position\t" + position);
                    bundle.putInt("firstPosition", position);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

        /**
         * 启动更换主题界面
         */
        changeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ThemeActivity.class);
                startActivityForResult(intent, 0x010);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x010) {
            int theme = MediaThemeKeeper.readTheme(this);
            changeTheme(main_layout, theme);
        }
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

    /**
     * 扫描音乐
     */
    private void scannerMusic() {
        if (!ifMusicScanned) {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            while (cursor != null && cursor.moveToNext()) {
                //歌曲的名称：MediaStore.Audio.Media.TITLE
                String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                //歌曲的歌手名：MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                if (duration < 30000) {
                    continue;
                }
                //歌曲文件的大小：MediaStore.Audio.Media.SIZE
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                Music music = new Music(tilte, album, artist, url, duration, size);

                data_music.add(music);
            }
            cursor.close();
            ifMusicScanned = true;
        }
        if (data_music.size() > 0 && currentPosition != -1) data_music.get(currentPosition).singing = true;
        mAdapter = new MusicListAdapter(data_music, this);
        media_list.setAdapter(mAdapter);
    }

    /**
     * 扫描视频
     */
    private void scannerVedio() {
        if (!ifVedioScanned) {
            SQLiteOptionHelper helper = new SQLiteOptionHelper(this, "vedios", 1);
            data_video = helper.getVedios();
            if (data_video.size() == 0 && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                ContentResolver resolver = getContentResolver();
                String[] projection = {MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATA};
                Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

                while (cursor.moveToNext()) {

                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

                    Video vedio = new Video(title, display_name, duration, url);

                    data_video.add(vedio);
                }
                cursor.close();
                helper.setVedios(data_video);
                VideoListAdapter vAdapter = new VideoListAdapter(this, data_video);
                media_list.setAdapter(vAdapter);
            }

            ifVedioScanned = true;
        }

        vAdapter = new VideoListAdapter(this, data_video);
        media_list.setAdapter(vAdapter);
    }

    /**
     * 改变歌曲是否在唱的状态
     */
    class ChangeMusicSingFlagReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.wh.changesingflag")) {
                int position = intent.getIntExtra("position", 0);
                boolean play = intent.getBooleanExtra("play", false);
                refreshMusicList(position, play);
            }
        }
    }

    /**
     * 刷新音乐列表
     *
     * @param position
     * @param play
     */
    private void refreshMusicList(int position, boolean play) {
        currentPosition = position;
        for (int i = 0; i < data_music.size(); i++) {
            data_music.get(i).singing = play && i == position;
        }
        if (mAdapter != null) {
            mAdapter.setData(data_music);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter = new MusicListAdapter(data_music, MainActivity.this);
            media_list.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}

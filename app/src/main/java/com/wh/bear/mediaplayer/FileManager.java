package com.wh.bear.mediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import com.wh.bear.mediaplayer.adapter.FileListAdapter;
import com.wh.bear.mediaplayer.bean.Video;
import com.wh.bear.mediaplayer.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015-10-08.
 */
public class FileManager extends Activity {
    ListView file_list;
    List<File> data_source;
    FileListAdapter adapter;
    File currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.file_list);
        file_list = (ListView) findViewById(R.id.file_list);
        currentFile = Environment.getExternalStorageDirectory();
        data_source = getFileList(currentFile);

        adapter = new FileListAdapter(this, data_source);
        file_list.setAdapter(adapter);

        file_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentFile = data_source.get(position);
                //如果为文件夹则进入下一层
                if (currentFile.isDirectory()) {
                    data_source = getFileList(currentFile);
                    adapter = new FileListAdapter(FileManager.this, data_source);
                    file_list.setAdapter(adapter);
                } else {
                    //如果为视频文件则进行播放
                    String suffix = StringUtils.getSuffix(currentFile.getName());
                    if (("mp4").equals(suffix) || ("3gp").equals(suffix)) {
                        Intent intent = new Intent(FileManager.this, VideoPlayerActivity.class);
                        Bundle bundle = new Bundle();
                        MediaPlayer player = MediaPlayer.create(FileManager.this, Uri.parse(currentFile.getAbsolutePath()));
                        ArrayList<Video> data = new ArrayList<>();
                        Video video = new Video(currentFile.getName(), null, player.getDuration(), currentFile.getAbsolutePath());
                        data.add(video);
                        bundle.putParcelableArrayList("data_video", data);
                        bundle.putInt("firstPosition", 0);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            }
        });

    }

    /**
     * 遍历文件夹
     *
     * @param file 要扫描的文件夹
     * @return 文件夹内文件集合
     */
    private List<File> getFileList(File file) {
        List<File> list = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null)
            Collections.addAll(list, files);
        return list;
    }

    /**
     * 设置back键事件，当按下back键则返回上一层
     *
     * @param keyCode 发生事件的code
     * @param event   发生的事件
     * @return boolean值表示当前事件发生时是否只监听该动作
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            //如果到根目录则销毁当前activity
            if (currentFile.equals(Environment.getExternalStorageDirectory())) {
                finish();
                return true;
            }
            //返回上一层
            currentFile = currentFile.getParentFile();

            data_source = getFileList(currentFile);
            adapter = new FileListAdapter(FileManager.this, data_source);
            file_list.setAdapter(adapter);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

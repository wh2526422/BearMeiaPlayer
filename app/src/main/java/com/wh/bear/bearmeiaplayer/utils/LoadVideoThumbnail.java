package com.wh.bear.bearmeiaplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/24.
 */
public class LoadVideoThumbnail {
    private static final String TAG = "LoadVideoThumbnail";
    List<String> urls;                                  //  储存加载过得图片的url
    List<LoadVideoThumbnailTask> tasks;                 //  储存开启过得线程
    static LoadVideoThumbnail loader;

    private LoadVideoThumbnail() {
        Log.i(TAG, "LoadVideoThumbnail");
        urls = new ArrayList<>();
        tasks = new ArrayList<>();
    }

    public static LoadVideoThumbnail getInstance() {
        if (loader == null) {
            loader = new LoadVideoThumbnail();
        }
        return loader;
    }

    public void displayImage(final String url, OnVideoThumbnailLoadCallback callback) {
        if (!urls.contains(url)) {
            Log.i(TAG, "displayImage");
            LoadVideoThumbnailTask loadVideoThumbnailTask = new LoadVideoThumbnailTask(callback);
            loadVideoThumbnailTask.execute(url);
            urls.add(url);
            tasks.add(loadVideoThumbnailTask);
        }

    }

    class LoadVideoThumbnailTask extends AsyncTask<String, Void, List<Object>> {

        OnVideoThumbnailLoadCallback callback;

        public LoadVideoThumbnailTask(OnVideoThumbnailLoadCallback callback) {
            this.callback = callback;
        }

        @Override
        protected List<Object> doInBackground(String... params) {
            List<Object> list = new ArrayList<>();
            String url = params[0];

            Bitmap source = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Video.Thumbnails.MICRO_KIND);
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(source, 50, 50);
            try {
                boolean saveImage = saveImage(bitmap, url);
                Log.i(TAG, "Save image \t" + saveImage);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Save image \t" + false);
            }

            list.add(url);
            list.add(bitmap);
            return list;
        }

        @Override
        protected void onPostExecute(List<Object> list) {
            String url = (String) list.get(0);
            Bitmap bitmap = (Bitmap) list.get(1);
            Log.i(TAG, "onPostExecute\turl\t" + url + "\tbitmap\t" + bitmap);
            if (callback != null) {
                callback.loadVideoThumbnailCompleted(bitmap, url);
            }

        }
    }

    /**
     * 取消所有的正在运行的任务，并且清空数据
     */
    public void cancelAllTasksAndCleanData() {
        if (urls != null) {
            urls.clear();
        }
        if (tasks != null) {
            for (LoadVideoThumbnailTask task : tasks) {
                if (!task.isCancelled()) {
                    task.cancel(true);
                }
            }
            tasks.clear();
        }
    }

    /**
     * 视频截图加载结束回调接口
     */
    public interface OnVideoThumbnailLoadCallback {
        void loadVideoThumbnailCompleted(Bitmap image, String imageUrl);
    }

    public Bitmap getLocaleImage(String imageUrl) {
        imageUrl = imageUrl.substring(0, imageUrl.lastIndexOf(".")) + ".jpg";
        File file = new File(imageUrl);
        Bitmap bitmap = null;
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(imageUrl);
        }

        return bitmap;
    }

    /**
     * 将bitmap本地保存
     *
     * @param bitmap   要保存的bitmap
     * @param imageUrl 要保存的url
     * @return 是否保存成功
     * @throws IOException
     */
    public boolean saveImage(Bitmap bitmap, String imageUrl) throws IOException {
        imageUrl = imageUrl.substring(0, imageUrl.lastIndexOf(".")) + ".jpg";
        Log.i(TAG, "url\t" + imageUrl);
        File file = new File(imageUrl);

        if (file.exists()) {
            return false;
        }
        boolean newFile = file.createNewFile();
        if (!newFile) {
            return false;
        }
        FileOutputStream fos = new FileOutputStream(file);
        boolean compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return compress;
    }

}

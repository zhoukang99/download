package com.clay.downloadlibrary.download;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 作者 : Clay
 * 日期 : 2019-01-07  17:29
 * 说明 : 资源下载
 */

public class DownloadManager {

    private final static int BIG_CORE_POOL_SIZE = 2;
    private final static int BIG_MAX_NUM_POOL_SIZE = 2;
    private final static int SMALL_CORE_POOL_SIZE = 2;
    private final static int SMALL_MAX_NUM_POOL_SIZE = 2;

    private static DownloadManager INSTANCE;
    private List<DownloadFuture> mFutures;
    private DownloadInterface mDownloadInterface;
    private PriorityThreadPoolExecutor mBigTaskPool;
    private PriorityThreadPoolExecutor mSmallTaskPool;

    private DownloadManager() {
        mFutures = Collections.synchronizedList(new ArrayList<DownloadFuture>());

        mBigTaskPool = new PriorityThreadPoolExecutor(BIG_CORE_POOL_SIZE, BIG_MAX_NUM_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS);
        mSmallTaskPool = new PriorityThreadPoolExecutor(SMALL_CORE_POOL_SIZE, SMALL_MAX_NUM_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS);
        initDownloadClient();
    }

    private void initDownloadClient() {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl("http://localhost")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            mDownloadInterface = retrofit.create(DownloadInterface.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DownloadManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DownloadManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DownloadManager();
                }
            }
        }
        return INSTANCE;
    }

    DownloadInterface getDownloadInterface() {
        return mDownloadInterface;
    }

    public DownloadFuture download(String url, String path, DownloadListener listener) {
        DownloadTask task = new DownloadTask.Builder()
                .setUrl(url)
                .setLocalPath(path)
                .setDonwloadListener(listener)
                .build();
        return download(task);
    }

    public DownloadFuture download(@NonNull DownloadTask task) {
        DownloadFuture<?> future = submit(task);
        mFutures.add(future);
        return future;
    }

    public void pauseAll() {
        for (DownloadFuture future : mFutures) {
            future.pause();
        }
    }

    public void resumeAll() {
        for (DownloadFuture future : mFutures) {
            future.resume();
        }
    }

    public void cancelAll(boolean mayInterruptIfRunning) {
        for (DownloadFuture future : mFutures) {
            future.cancel(mayInterruptIfRunning);
        }
    }

    public void shutdown() {
        if (!mBigTaskPool.isShutdown()) {
            mBigTaskPool.shutdown();
        }
        if (!mSmallTaskPool.isShutdown()) {
            mSmallTaskPool.shutdown();
        }
    }

    void removeFuture(DownloadFuture future) {
        mFutures.remove(future);
    }

    private DownloadFuture<?> submit(DownloadTask task) {
        DownloadRunnable operator = new DownloadRunnable(task);
        if (task.getTotalSize() > 0 && task.getTotalSize() < 10 * 1024 * 1024) {
            return mSmallTaskPool.submit(operator);
        } else {
            return mBigTaskPool.submit(operator);
        }
    }

    private boolean addDownloadTask(DownloadTask task) {
        if (TextUtils.isEmpty(task.getUrl())) {
            throw new IllegalArgumentException("task's url cannot be empty");
        }
        // do nothing
        return true;
    }
}

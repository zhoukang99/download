package com.clay.downloadlibrary.download;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 作者 : Clay
 * 日期 : 2019-01-11  09:45
 * 说明 :
 */

public class DownloadFuture<T> extends FutureTask<T> implements Comparable<DownloadFuture> {

    private DownloadRunnable mOperate;

    DownloadFuture(@NonNull Callable<T> callable) {
        super(callable);
    }

    DownloadFuture(@NonNull DownloadRunnable runnable, T result) {
        super(runnable, result);
        mOperate = runnable;
    }

    /**
     * 任务在队列中
     */
    public boolean isPendding() {
        return mOperate.isPendding();
    }

    /**
     * 任务是否暂停
     */
    public boolean isPaused() {
        return mOperate.isPaused();
    }

    /**
     * 是否暂停
     */
    public void pause() {
        if (mOperate != null) {
            mOperate.pauseDownload();
        }
    }

    public void resume() {
        if (mOperate != null) {
            mOperate.resumeDownload();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        mOperate.cancelDownload();
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    protected void done() {
        Log.i("download", "done");
        super.done();
        DownloadManager.getInstance().removeFuture(this);
    }

    @Override
    public int compareTo(@NonNull DownloadFuture o) {
        if (mOperate instanceof Comparable) {
            return ((Comparable) mOperate).compareTo(o.mOperate);
        }
        return -1;
    }
}

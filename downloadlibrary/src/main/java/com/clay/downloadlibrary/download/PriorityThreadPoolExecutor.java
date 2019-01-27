package com.clay.downloadlibrary.download;


import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作者 : Clay
 * 日期 : 2019-01-11  16:08
 * 说明 : 按优先级来执行任务的线程池
 */

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<Runnable>());
    }

    @NonNull
    @Override
    public <T> DownloadFuture<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        DownloadFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    @NonNull
    @Override
    public DownloadFuture<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        DownloadFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    @NonNull
    @Override
    public <T> DownloadFuture<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        DownloadFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    @Override
    protected <T> DownloadFuture<T> newTaskFor(Callable<T> callable) {
        return new DownloadFuture<>(callable);
    }

    @Override
    protected <T> DownloadFuture<T> newTaskFor(Runnable runnable, T value) {
        return new DownloadFuture<>((DownloadRunnable) runnable, value);
    }
}

package com.clay.downloadlibrary.download;

import android.support.annotation.NonNull;

import com.clay.downloadlibrary.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.ResponseBody;

/**
 * 作者 : Clay
 * 日期 : 2019-01-07  17:29
 * 说明 : 下载操作
 */
public class DownloadRunnable implements Runnable, Comparable<DownloadRunnable> {

    // 10 kb
    private static final long REFRESH_INTEVAL_SIZE = 10240;
    // 10 s
    private static final int REFRESH_INTEVAL_TIMEOUT = 10;
    // buffer 2kb
    private static final int BUFFER_SIZE = 8192;

    private DownloadTask task;
    private DownloadListener downloadListener;

    private Disposable mDownloadDisposable;

    private volatile boolean penddingFlag = true;
    private volatile boolean pauseFlag = false;
    private volatile boolean stopFlag = false;

    DownloadRunnable(DownloadTask task) {
        this.task = task;
        this.downloadListener = task.getDonwloadListener();
    }

    boolean isPendding() {
        return penddingFlag;
    }

    boolean isPaused() {
        return pauseFlag;
    }

    void pauseDownload() {
        if (pauseFlag) {
            return;
        }
        pauseFlag = true;
    }

    void resumeDownload() {
        if (!pauseFlag) {
            return;
        }
        pauseFlag = false;
        synchronized (this) {
            notify();
        }
    }

    void cancelDownload() {
        stopFlag = true;
        if (mDownloadDisposable != null && !mDownloadDisposable.isDisposed()) {
            mDownloadDisposable.dispose();
        }
        resumeDownload();
    }

    @Override
    public void run() {
        try {
            penddingFlag = false;
            while (pauseFlag) {
                operatePaused();
            }
            Observable.just(task)
                    .filter(new Predicate<DownloadTask>() {
                        @Override
                        public boolean test(DownloadTask task) throws Exception {
                            buildDownloadFile();
                            String parentPath = new File(task.getLocalPath()).getParent();
                            boolean enoughSpace = FileUtil.isEnoughSpace(parentPath, task.getTotalSize(), 0.8f);
                            if (!enoughSpace) {
                                long availableSize = FileUtil.getAvailableSpace(parentPath);
                                long totalSize = task.getTotalSize();
                                String formatMsg = String.format("No enough space on device: availableSize=%s, materialSize=%s", availableSize, totalSize);
                                operateFailed(new IOException(formatMsg));
                            }
                            return enoughSpace;
                        }
                    })
                    // 开始下载
                    .flatMap(new Function<DownloadTask, ObservableSource<ResponseBody>>() {
                        @Override
                        public ObservableSource<ResponseBody> apply(DownloadTask task) throws Exception {
                            return DownloadManager.getInstance().getDownloadInterface()
                                    .download("bytes=" + task.getDownloadedSize() + "-", task.getUrl());
                        }
                    })
                    .retryWhen(new RetryWhenFunction(3) {
                        @Override
                        protected ObservableSource<?> retry(int count) {
                            operateRetry(count);
                            return super.retry(count);
                        }
                    })
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {
                            mDownloadDisposable = disposable;
                            operateStarted();
                        }
                    })
                    // 保存文件
                    .subscribe(new Consumer<ResponseBody>() {
                        @Override
                        public void accept(ResponseBody responseBody) throws Exception {
                            if (task.getTotalSize() <= 0) {
                                task.setTotalSize(task.getDownloadedSize() + responseBody.contentLength());
                            }
                            save(responseBody.byteStream());
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            operateFailed(throwable);
                        }
                    });
        } finally {
            operateFinished();
        }
    }

    private void save(InputStream inputStream) {
        RandomAccessFile raf = null;
        try {
            File targetFile = new File(task.getLocalPath());
            raf = new RandomAccessFile(targetFile, "rw");
            raf.seek(task.getDownloadedSize());
            byte[] readBytes = new byte[BUFFER_SIZE];
            long prevTime = System.nanoTime();
            int len;
            long tempSize = 0, tempTime, speed;
            operateProgress(0, 0);
            while ((len = inputStream.read(readBytes)) != -1) {
                if (stopFlag) {
                    operateStoped();
                    return;
                }

                while (pauseFlag) {
                    operatePaused();
                }

                raf.write(readBytes, 0, len);

                tempSize += len;
                tempTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - prevTime);
                // 下载超过100K或者10秒做一次更新
                if (tempSize > REFRESH_INTEVAL_SIZE && tempTime >= 1 || tempTime >= REFRESH_INTEVAL_TIMEOUT) {
                    speed = tempSize / tempTime;

                    operateProgress(tempSize, speed);

                    tempSize = 0;
                    prevTime = System.nanoTime();
                }
            }
            if (tempSize > 0) {
                long seconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - prevTime);
                speed = seconds == 0 ? tempSize : tempSize / seconds;
                operateProgress(tempSize, speed);
            }

            operateSuccessed();
        } catch (IOException e) {
            if (stopFlag) {
                operateFailed(e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void operateStarted() {
        if (downloadListener != null) {
            downloadListener.onDownloadStart(task);
        }
    }

    private void operateProgress(long tempSize, long speed) {
        task.setDownloadedSize(task.getDownloadedSize() + tempSize);
        if (task.getDonwloadListener() != null) {
            task.getDonwloadListener().onDownloadUpdated(task, speed);
        }
    }

    private void operateRetry(int retryCount) {
        if (downloadListener != null) {
            downloadListener.onDownloadRetry(task, retryCount);
        }
    }

    private void operatePaused() {
        if (downloadListener != null) {
            downloadListener.onDownloadPaused(task);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                if (downloadListener != null) {
                    downloadListener.onDownloadResumed(task);
                }
            }
        }
    }

    private void operateStoped() {
        if (downloadListener != null) {
            downloadListener.onDownloadCanceled(task);
        }
    }

    private void operateSuccessed() {
        if (downloadListener != null) {
            downloadListener.onDownloadSuccessed(task);
        }
    }

    private void operateFailed(Throwable throwable) {
        if (downloadListener != null) {
            downloadListener.onDownloadFailed(task, throwable);
        }
    }

    private void operateFinished() {
        if (downloadListener != null) {
            downloadListener.onDownloadFinished(task);
        }
    }

    private void buildDownloadFile() throws IOException {
        File file = new File(task.getLocalPath());
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        } else if (task.isOverWirte()) {
            task.setDownloadedSize(0);
        } else {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            task.setDownloadedSize(raf.length());
            raf.close();
        }
    }

    @Override
    public int compareTo(@NonNull DownloadRunnable o) {
        return o.task.getPriority() - task.getPriority();
    }
}

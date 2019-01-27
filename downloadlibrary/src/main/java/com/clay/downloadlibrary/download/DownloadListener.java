package com.clay.downloadlibrary.download;

/**
 * 作者 : Clay
 * 日期 : 2019-01-07  17:29
 * 说明 : 下载监听
 */
public abstract class DownloadListener {

    public void onDownloadStart(DownloadTask task) {

    }

    public void onDownloadUpdated(DownloadTask task, long trafficSpeed) {

    }

    public void onDownloadPaused(DownloadTask task) {

    }

    public void onDownloadResumed(DownloadTask task) {

    }

    public abstract void onDownloadSuccessed(DownloadTask task);

    public void onDownloadCanceled(DownloadTask task) {

    }

    public abstract void onDownloadFailed(DownloadTask task, Throwable throwable);

    public void onDownloadRetry(DownloadTask task, int retryCount) {

    }

    public void onDownloadFinished(DownloadTask task) {

    }
}

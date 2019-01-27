package com.clay.downloadlibrary.download;

import android.text.TextUtils;

/**
 * 下载任务
 */
public class DownloadTask {

    final static String DEFAULT = "default";
    final static int DEFAULT_PRIORITY = 0;

    private int priority = DEFAULT_PRIORITY;
    // 是否覆盖
    private boolean isOverWirte = false;
    // 是否解压
    private boolean isUnzip = false;
    private String mTag;
    private String mUrl;
    private String mLocalPath;
    private String md5;
    private String mUnzipPath;
    private long mDownloadedSize;
    private long mTotalSize;
    private DownloadListener mDonwloadListener;

    private DownloadTask() {
    }

    public static final class Builder {
        private int priority = DEFAULT_PRIORITY;
        private boolean isOverWirte = false;
        private boolean isUnzip = false;
        private String mTag;
        private String mUrl;
        private String mLocalPath;
        private String md5;
        private String mUnzipPath;
        private long mTotalSize;
        private DownloadListener mDonwloadListener;

        public Builder() {
            priority = DEFAULT_PRIORITY;
            isOverWirte = false;
            isUnzip = false;
            mTag = DEFAULT;
            mUrl = null;
            mLocalPath = null;
            md5 = null;
            mUnzipPath = null;
            mTotalSize = -1;
            mDonwloadListener = null;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder setOverWirte(boolean overWirte) {
            isOverWirte = overWirte;
            return this;
        }

        public Builder setUnzip(boolean unzip) {
            isUnzip = unzip;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder setUrl(String url) {
            mUrl = url;
            return this;
        }

        public Builder setLocalPath(String localPath) {
            mLocalPath = localPath;
            return this;
        }

        public Builder setMd5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder setUnzipPath(String unzipPath) {
            mUnzipPath = unzipPath;
            return this;
        }

        public Builder setTotalSize(long size) {
            mTotalSize = size;
            return this;
        }

        public Builder setDonwloadListener(DownloadListener donwloadListener) {
            mDonwloadListener = donwloadListener;
            return this;
        }

        public DownloadTask build() {
            checkParams();
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.priority = this.priority;
            downloadTask.isOverWirte = this.isOverWirte;
            downloadTask.isUnzip = this.isUnzip;
            downloadTask.mTag = this.mTag;
            downloadTask.mUrl = this.mUrl;
            downloadTask.mLocalPath = this.mLocalPath;
            downloadTask.md5 = this.md5;
            downloadTask.mUnzipPath = this.mUnzipPath;
            downloadTask.mTotalSize = this.mTotalSize;
            downloadTask.mDonwloadListener = this.mDonwloadListener;
            return downloadTask;
        }

        private void checkParams() {
            if (TextUtils.isEmpty(this.mUrl)) {
                throw new NullPointerException("download resource url can't be null");
            }
            if (TextUtils.isEmpty(this.mLocalPath)) {
                throw new NullPointerException("save path can't be null");
            }
            if (this.isUnzip && TextUtils.isEmpty(this.mUnzipPath)) {
                throw new NullPointerException("unzip path can't be null");
            }
        }
    }

    public int getPriority() {
        return priority;
    }

    public boolean isOverWirte() {
        return isOverWirte;
    }

    public boolean isUnzip() {
        return isUnzip;
    }

    public String getTag() {
        return mTag;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public String getMd5() {
        return md5;
    }

    public String getUnzipPath() {
        return mUnzipPath;
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    void setTotalSize(long totalSize) {
        mTotalSize = totalSize;
    }

    public long getDownloadedSize() {
        return mDownloadedSize;
    }

    void setDownloadedSize(long downloadedSize) {
        mDownloadedSize = downloadedSize;
    }

    public DownloadListener getDonwloadListener() {
        return mDonwloadListener;
    }

    @Override
    public String toString() {
        return "DownloadTask{" +
                "priority=" + priority +
                ", mTag='" + mTag + '\'' +
                ", mDownloadedSize=" + mDownloadedSize +
                ", mTotalSize=" + mTotalSize +
                '}';
    }
}

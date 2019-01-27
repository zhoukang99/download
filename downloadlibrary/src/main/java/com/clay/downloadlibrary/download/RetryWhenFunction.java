package com.clay.downloadlibrary.download;

import android.util.Log;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * 作者 : Clay
 * 日期 : 2019-01-09  20:10
 * 说明 : 自定义重试次数方法，且延迟retryCount * 10秒
 */

public class RetryWhenFunction implements Function<Observable<Throwable>, ObservableSource<?>> {
    private static final String TAG = "RetryWhenFunction";

    private final static int DEFAULT_DELAY_TIME = 10;
    private final int mFixedDelayTime;
    private final int maxRetryCount;
    private int retryCount;

    public RetryWhenFunction(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        this.mFixedDelayTime = -1;
    }

    public RetryWhenFunction(int maxRetryCount, int delayTime) {
        this.mFixedDelayTime = delayTime;
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 默认的重试策略
     * @return 无限次的重试，且每次重试间隔位10秒
     */
    public static RetryWhenFunction DEFAULT() {
        return new RetryWhenFunction(Integer.MAX_VALUE, DEFAULT_DELAY_TIME);
    }

    @Override
    public ObservableSource<?> apply(final Observable<Throwable> throwableObservable) throws Exception {
        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Throwable throwable) {
                throwable.printStackTrace();
                if (throwable instanceof UnknownHostException || throwable instanceof HttpException || throwable instanceof InterruptedException) {
                    return Observable.error(throwable);
                }
                if (++retryCount < maxRetryCount) {
                    Log.w(TAG, String.format("The %sth retry because %s", retryCount, throwable.toString()));
                    return retry(retryCount);
                } else {
                    return Observable.error(throwable);
                }
            }
        });
    }

    /**
     * 重新执行
     * @param count 重试的次数
     */
    protected ObservableSource<?> retry(int count) {
        return Observable.timer(mFixedDelayTime > 0 ? mFixedDelayTime : DEFAULT_DELAY_TIME * count, TimeUnit.SECONDS);
    }
}

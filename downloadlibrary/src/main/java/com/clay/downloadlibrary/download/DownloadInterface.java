package com.clay.downloadlibrary.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 作者 : Clay
 * 日期 : 2019-01-07  17:29
 * 说明 : 下载接口
 */
public interface DownloadInterface {

	@Streaming
	@GET
	Observable<ResponseBody> download(@Header("Range") String range, @Url String url);
}

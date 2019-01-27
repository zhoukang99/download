package com.clay.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clay.downloadlibrary.download.DownloadFuture;
import com.clay.downloadlibrary.download.DownloadListener;
import com.clay.downloadlibrary.download.DownloadManager;
import com.clay.downloadlibrary.download.DownloadTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final String resource_1 = "http://192.168.30.110/resource/473bb879f7086ef6a94c26c336e7c1df_14987036460.mp4";
    private ProgressBar progressBar1;
    private TextView tv1;
    private Button btn1;

    private final String resource_2 = "http://192.168.30.110/resource/9c4656ebe3128d9cd7f705bb7cd3848b_149803258294.flv";
    private ProgressBar progressBar2;
    private TextView tv2;
    private Button btn2;

    private final String resource_3 = "http://192.168.30.110/resource/abcb3168474150851bb777fa5f54385b_149809652939.avi";
    private ProgressBar progressBar3;
    private TextView tv3;
    private Button btn3;

    private final String resource_4 = "http://192.168.30.110/resource/b64c41208359c70f8a4997969ae4f337_154658647598.mov";
    private ProgressBar progressBar4;
    private TextView tv4;
    private Button btn4;

    private String savePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View item1 = findViewById(R.id.item1);
        progressBar1 = item1.findViewById(R.id.progressBar);
        tv1 = item1.findViewById(R.id.tv_percent);
        btn1 = item1.findViewById(R.id.btn);
        btn1.setText(R.string.download);

        View item2 = findViewById(R.id.item2);
        progressBar2 = item2.findViewById(R.id.progressBar);
        tv2 = item2.findViewById(R.id.tv_percent);
        btn2 = item2.findViewById(R.id.btn);
        btn2.setText(R.string.download);

        View item3 = findViewById(R.id.item3);
        progressBar3 = item3.findViewById(R.id.progressBar);
        tv3 = item3.findViewById(R.id.tv_percent);
        btn3 = item3.findViewById(R.id.btn);
        btn3.setText(R.string.download);

        View item4 = findViewById(R.id.item4);
        progressBar4 = item4.findViewById(R.id.progressBar);
        tv4 = item4.findViewById(R.id.tv_percent);
        btn4 = item4.findViewById(R.id.btn);
        btn4.setText(R.string.download);

        this.savePath = getFilesDir().getAbsolutePath();

        init(btn1, tv1, progressBar1, "1.mp4", resource_1);
        init(btn2, tv2, progressBar2, "2.flv", resource_2);
        init(btn3, tv3, progressBar3, "3.avi", resource_3);
        init(btn4, tv4, progressBar4, "4.mov", resource_4);
    }

    /**
     * 初始化
     */
    public void init(final Button btn,final TextView tv,final ProgressBar pb, final String filename, final String url) {
        btn.setOnClickListener(new View.OnClickListener() {
            private DownloadFuture downloadFuture;
            @Override
            public void onClick(View v) {

                if (getString(R.string.download).equals(btn.getText())) {
                    btn.setText(R.string.download_stop);
                    // 下载
                    downloadFuture = DownloadManager.getInstance().download(url, savePath + File.separator + filename, new DownloadListener() {
                        @Override
                        public void onDownloadSuccessed(DownloadTask task) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn.setClickable(false);
                                    btn.setText(R.string.download_completed);
                                }
                            });
                        }

                        @Override
                        public void onDownloadUpdated(DownloadTask task, long trafficSpeed) {
                            Log.d("download", task.toString());
                            final int p = (int) (task.getDownloadedSize() * 100 / task.getTotalSize());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(p + "%");
                                    pb.setProgress(p);
                                }
                            });
                        }

                        @Override
                        public void onDownloadFailed(DownloadTask task, Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } else {
                    btn.setText(R.string.download);
                    downloadFuture.cancel(true);
                    downloadFuture = null;
                }
            }
        });
    }
}

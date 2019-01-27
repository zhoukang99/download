package com.clay.downloadlibrary.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StatFs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 作者 : Clay
 * 日期 : 2019-01-23  17:09
 * 说明 :
 */

public class FileUtil {
    private static final String TAG = "FileUtil";

    // 缓存区大小
    private static final int BUFFER_SIZE = 8192;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getAvailableSpace(String dir) {
        File directory = new File(dir);
        try {
            final StatFs stats = new StatFs(directory.getPath());
            int curApiVersion = android.os.Build.VERSION.SDK_INT;
            if (curApiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return stats.getBlockSizeLong() * stats.getAvailableBlocksLong();
            } else {
                return stats.getBlockSize() * stats.getAvailableBlocks();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getTotalSpace(String dir) {
        File directory = new File(dir);
        try {
            final StatFs stats = new StatFs(directory.getPath());
            int curApiVersion = android.os.Build.VERSION.SDK_INT;
            if (curApiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return stats.getBlockSizeLong() * stats.getBlockCountLong();
            } else {
                return stats.getBlockSize() * stats.getBlockCount();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static int getAvailableSpacePercent(String dir) {
        File directory = new File(dir);
        try {
            final StatFs stats = new StatFs(directory.getPath());
            int curApiVersion = android.os.Build.VERSION.SDK_INT;
            if (curApiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return (int) (stats.getAvailableBlocksLong() * 100 / stats.getBlockCountLong());
            } else {
                return stats.getAvailableBlocks() * 100 / stats.getBlockCount();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }
    }


    public static byte[] gzipDecompress(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPInputStream gis = new GZIPInputStream(is);

        int count;
        byte buf[] = new byte[BUFFER_SIZE];

        while ((count = gis.read(buf, 0, BUFFER_SIZE)) != -1) {
            baos.write(buf, 0, count);
        }

        byte result[] = baos.toByteArray();

        baos.flush();
        baos.close();
        gis.close();

        return result;
    }

    public static byte[] gzipDecompress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        byte[] bytes = gzipDecompress(bais);
        bais.close();
        return bytes;
    }

    public static byte[] gzipCompress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        gzipCompress(bais, baos);

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();
        bais.close();

        return output;
    }

    public static void gzipCompress(InputStream is, OutputStream os)
            throws Exception {

        GZIPOutputStream gos = new GZIPOutputStream(os);

        int count;
        byte data[] = new byte[BUFFER_SIZE];
        while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
            gos.write(data, 0, count);
        }

        gos.finish();

        // android 5.0以下会报错
        if (Build.VERSION.SDK_INT >= 21) {
            gos.flush();
            gos.close();
        }
    }

    /**
     * 检查空间是否足够
     *
     * @param dir           文件对象
     * @param reservedSize   需要的空间大小
     * @param useablePercent 可用空间的百分比
     * @return true: 空间足够, false: 空间不足
     */
    public static boolean isEnoughSpace(String dir, long reservedSize, float useablePercent) {
        File file = new File(dir);
        return file.exists() && (getTotalSpace(dir) * useablePercent >= reservedSize);
    }
}

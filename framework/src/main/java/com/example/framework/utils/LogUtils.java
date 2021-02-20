package com.example.framework.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.framework.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    private static SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("YYY-MM-dd HH:mm:ss");

    public static void i(String text) {
        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(text)) {
                Log.i(BuildConfig.LOG_TAG, text);
                writToFile(text);
            }
        }
    }

    public static void d(String text) {
        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(text)) {
                Log.d(BuildConfig.LOG_TAG, text);
                writToFile(text);
            }
        }
    }

    public static void e(String text) {
        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(text)) {
                Log.e(BuildConfig.LOG_TAG, text);
                writToFile(text);
            }
        }
    }

    private static void writToFile(String text) {
        String pathGroup = "/sdcard/log";
        String path = pathGroup + "/app.log";

        String content = simpleDateFormat.format(new Date()) + " " + text + "\n";

        File fileGroup = new File(pathGroup);
        if (!fileGroup.exists()) {
            fileGroup.mkdirs();
        }

        FileOutputStream fileOutputStream;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = new FileOutputStream(path, true);
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fileOutputStream, Charset.forName("GBK"))
            );
            bufferedWriter.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

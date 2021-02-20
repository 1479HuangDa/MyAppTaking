package com.example.framework.utils;

public class TimeUtils {

    /**
     * 转换毫米格式 HH:mm:ss
     */
    public static String formatDuring(long ms) {
        //hours 考虑到时区的问题
        long hours = ((ms+ 28800000) % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);

        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (ms % (1000 * 60)) / 1000;

        String h = hours + "";
        if (hours < 10) {
            h = "0" + hours;
        }

        String m = minutes + "";
        if (minutes < 10) {
            m = "0" + minutes;
        }

        String s = seconds + "";
        if (seconds < 10) {
            s = "0" + seconds;
        }

        return h + ":" + m + ":" + s;
    }

}

package com.pim.server.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String res = "";
        if (!"".equals(s)) {
            try {
                res = String.valueOf(sdf.parse(s).getTime() / 1000);
            } catch (Exception e) {
                System.out.println("传入了null值");
            }
        } else {
            long time = System.currentTimeMillis();
            res = String.valueOf(time / 1000);
        }

        return res;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(int time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times = format.format(new Date(time * 1000L));
        return times;
    }

    /*
     * 获得当前时间
     */
    public static String getDateTime() {
        Date date = new Date();
        String strDateFormat = "yyyy-MM-dd HH:mm:ss";//yyyy-MM-dd HH:mm:ss
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        return sdf.format(date);
    }

    /*
     * 获得当前Date
     */
    public static String getDate() {
        Date date = new Date();
        String strDateFormat = "yyyy-MM-dd";//yyyy-MM-dd HH:mm:ss
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        return sdf.format(date);
    }

    /**
     * 获取纳秒时间搓
     *
     * @return
     */
    public static long getNanoTime() {
        return System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
    }

    public static String getTimeSt() {
        return System.currentTimeMillis() + "";
    }
}

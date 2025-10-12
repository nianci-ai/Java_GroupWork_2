package com.schedule.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类 - 提供日期处理相关的功能
 */
public class DateUtil {
    // 默认日期格式
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";
    public static final String TIME_ONLY_FORMAT = "HH:mm";
    
    /**
     * 将日期格式化为字符串
     * @param date 日期对象
     * @param format 日期格式
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.format(date);
    }
    
    /**
     * 使用默认格式将日期格式化为字符串
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 将字符串解析为日期对象
     * @param dateStr 日期字符串
     * @param format 日期格式
     * @return 日期对象
     * @throws ParseException 解析异常
     */
    public static Date parseDate(String dateStr, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.parse(dateStr);
    }
    
    /**
     * 使用默认格式将字符串解析为日期对象
     * @param dateStr 日期字符串
     * @return 日期对象
     * @throws ParseException 解析异常
     */
    public static Date parseDate(String dateStr) throws ParseException {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 获取今天的日期（时间部分为0）
     * @return 今天的日期
     */
    public static Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 获取本周的第一天（周日或周一，取决于地区设置）
     * @return 本周第一天
     */
    public static Date getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 获取本月的第一天
     * @return 本月第一天
     */
    public static Date getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(Date startDate, Date endDate) {
        long diffTime = endDate.getTime() - startDate.getTime();
        return diffTime / (1000 * 60 * 60 * 24);
    }
    
    /**
     * 计算两个日期之间的小时差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 小时差
     */
    public static long hoursBetween(Date startDate, Date endDate) {
        long diffTime = endDate.getTime() - startDate.getTime();
        return diffTime / (1000 * 60 * 60);
    }
    
    /**
     * 计算两个日期之间的分钟差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分钟差
     */
    public static long minutesBetween(Date startDate, Date endDate) {
        long diffTime = endDate.getTime() - startDate.getTime();
        return diffTime / (1000 * 60);
    }
    
    /**
     * 向日期添加指定的天数
     * @param date 原始日期
     * @param days 要添加的天数
     * @return 添加后的日期
     */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
    
    /**
     * 向日期添加指定的小时数
     * @param date 原始日期
     * @param hours 要添加的小时数
     * @return 添加后的日期
     */
    public static Date addHours(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }
    
    /**
     * 判断两个日期是否是同一天
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 是否是同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 判断一个日期是否在另一个日期之前
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return date1是否在date2之前
     */
    public static boolean isBefore(Date date1, Date date2) {
        return date1.before(date2);
    }
    
    /**
     * 判断一个日期是否在另一个日期之后
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return date1是否在date2之后
     */
    public static boolean isAfter(Date date1, Date date2) {
        return date1.after(date2);
    }
}
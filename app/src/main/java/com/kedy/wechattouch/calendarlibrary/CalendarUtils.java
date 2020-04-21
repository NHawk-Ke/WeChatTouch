package com.kedy.wechattouch.calendarlibrary;

import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CalendarUtils {
    private static final String TAG = "CalendarUtils";

    //固定值
    static final int NUM_ROW = 6;
    static final int NUM_COL = 7;
    static final int NUM_CELL = 42; // 6*7

    //计算dip2px
    private float mScale;
    private int maxHeightDp;
    private int maxWidthDp;
    //一个统一的日历用来做运算
    private Calendar mCalendar;
    //缓存天数信息
    private HashMap<String, List<DayBox>> mCacheMonthData;

    //单个实例
    private static final CalendarUtils instance = new CalendarUtils();

    private CalendarUtils() {
        mCacheMonthData = new HashMap<>();
        mCalendar = Calendar.getInstance();
    }

    static CalendarUtils getInstance() {
        return instance;
    }

    int[] setScale(DisplayMetrics displayMetrics) {
        mScale = displayMetrics.density;

        return new int[]{(int) (displayMetrics.widthPixels / mScale),
                (int) (displayMetrics.heightPixels / mScale)};
    }

    public void clearCacheMonth() {
        mCacheMonthData = new HashMap<>();
    }

    /***
     * 获取当月的所有显示日期，包含上月和下月的一部分
     *
     * @param year 目标年份
     * @param month 目标月份（0-based）
     * @return 一个包含6*7（42）个DayBox的List
     */
    List<DayBox> getMonthDate(int year, int month) {
        //通过key寻找这个月份是否有缓
        String key = year + " " + month;
        if (mCacheMonthData.containsKey(key)) {
            return mCacheMonthData.get(key);
        }

        //创建一个array储存当前月份的所有DayBox
        List<DayBox> dayBoxes = new ArrayList<>();

        //根据当前年月计算上一个月和下一个月是哪年哪月
        int lastMonthYear = year;
        int lastMonthMonth = month - 1;
        if (month == 0){
            lastMonthYear = year - 1;
            lastMonthMonth = 11;
        }
        int nextMonthYear = year;
        int nextMonthMonth = month + 1;
        if (month == 11) {
            nextMonthYear = year + 1;
            nextMonthMonth = 0;
        }

        //计算上个月，当前月，下个月所需要的天数
        int lastMonthDaysNeeded = getFirstDayOfMonth(year, month);
        int currentMonthDays = getNumDays(year, month);
        int lastMonthDays = getNumDays(lastMonthYear, lastMonthMonth);

        //创建上个月，当前月，下个月所需的DayBox
        for (int i = lastMonthDaysNeeded; i > 0; i--) {
            dayBoxes.add(createDayBox(lastMonthYear, lastMonthMonth, lastMonthDays - i - 1, false));
        }
        for (int i = 0; i < currentMonthDays; i++) {
            dayBoxes.add(createDayBox(year, month, i, true));
        }
        for (int i = 0; i < NUM_CELL - currentMonthDays - lastMonthDaysNeeded; i++) {
            dayBoxes.add(createDayBox(nextMonthYear, nextMonthMonth, i, false));
        }

        //缓存信息
        mCacheMonthData.put(key, dayBoxes);
        return dayBoxes;
    }

    /***
     * 获取当月的第一天为星期几，0为周日6为周六
     *
     * @param year 目标年份
     * @param month 目标月份（0-based）
     * @return 0到6的int
     */
    private int getFirstDayOfMonth(int year, int month) {
        mCalendar.set(year, month, 0);
        int result = mCalendar.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "first day of " + year + "-" + month + " is : " + result);
        return result;
    }

    /***
     * 获取某年中一个月份的天数
     *
     * @param year 目标年份
     * @param month 目标月份（0-based）
     * @return 28,29,30,31的其中一个
     */
    private int getNumDays(int year, int month) {
        if (month == 3 || month == 5 || month == 8 || month == 10) {
            return 30;
        } else if (month == 2) {
            if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                return 29;
            } else {
                return 28;
            }
        } else {
            return 31;
        }
    }

    private DayBox createDayBox(int year, int month, int day, boolean isCurrentMonth) {
        DayBox dayBox = new DayBox();
        dayBox.setYear(year);
        dayBox.setMonth(month);
        dayBox.setDay(day);
        dayBox.setCurrentMonth(isCurrentMonth);
        return dayBox;
    }

    /**
     * 计算当前日期
     */
    int[] getCurrentDate() {
        return new int[]{mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)};
    }

    /***
     * 根据当前的dip比例转换dpValue到px
     *
     * @param dpValue float值
     * @return pixel
     */
    int dip2px(float dpValue) {
        return (int) (dpValue * mScale + 0.5f);
    }

}

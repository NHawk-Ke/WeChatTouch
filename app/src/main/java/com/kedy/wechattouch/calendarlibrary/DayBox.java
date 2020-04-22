package com.kedy.wechattouch.calendarlibrary;

import android.graphics.RectF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class DayBox implements Serializable {
    private RectF mRectF;
    private RectF mSingleIconRectF;
    private List<RectF> mDoubleIconRectFs;
    private List<RectF> mTripleIconRectFs;
    private int mYear;
    private int mMonth;
    private int mDay;
    private boolean mIsCurrentMonth;

    int getYear() {
        return mYear;
    }

    void setYear(int year) {
        mYear = year;
    }

    int getMonth() {
        return mMonth;
    }

    void setMonth(int month) {
        mMonth = month;
    }

    int getDay() {
        return mDay;
    }

    void setDay(int day) {
        mDay = day;
    }

    boolean isCurrentMonth() {
        return mIsCurrentMonth;
    }

    void setCurrentMonth(boolean currentMonth) {
        mIsCurrentMonth = currentMonth;
    }

    RectF getRectF() {
        return mRectF;
    }

    void setRectF(RectF rectF) {
        mRectF = rectF;
    }

    RectF getSingleIconRectF() {
        return mSingleIconRectF;
    }

    List<RectF> getDoubleIconRectFs() {
        return mDoubleIconRectFs;
    }

    List<RectF> getTripleIconRectFs() {
        return mTripleIconRectFs;
    }

    void setIconRectFs(int iconSize, int textHeight, int daySpace, int textSpace) {
        int iconTop = (int) mRectF.top + textHeight + daySpace + textSpace;
        mSingleIconRectF = new RectF(
                mRectF.centerX() - iconSize / 2f, iconTop,
                mRectF.centerX() + iconSize / 2f, mRectF.bottom
        );

        mDoubleIconRectFs = new ArrayList<>(2);
        mDoubleIconRectFs.add(new RectF(
                mRectF.left + iconSize / 3f, iconTop,
                mRectF.left + iconSize / 3f + iconSize, mRectF.bottom
        ));
        mDoubleIconRectFs.add(new RectF(
                mRectF.right - iconSize / 3f - iconSize, iconTop,
                mRectF.right - iconSize / 3f, mRectF.bottom
        ));

        mTripleIconRectFs = new ArrayList<>(3);
        mTripleIconRectFs.add(new RectF(
                mRectF.left, iconTop,
                mRectF.left + iconSize, mRectF.bottom
        ));
        mTripleIconRectFs.add(new RectF(
                mRectF.left + iconSize, iconTop,
                mRectF.right - iconSize, mRectF.bottom
        ));
        mTripleIconRectFs.add(new RectF(
                mRectF.right - iconSize, iconTop,
                mRectF.right, mRectF.bottom
        ));
    }

    boolean isContains(float x, float y) {
        return mRectF.contains(x, y);
    }

    int[] getDate() {
        return new int[]{mYear, mMonth, mDay};
    }

    String getDateStr() {
        return mYear + "-" + mMonth + "-" + mDay;
    }

}

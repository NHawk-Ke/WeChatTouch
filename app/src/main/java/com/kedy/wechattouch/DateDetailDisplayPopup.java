package com.kedy.wechattouch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class DateDetailDisplayPopup extends PopupWindow {
    private static final String TAG = "DateDetailDisplayPopup";

    private View mView;

    // Configs
    private int MAX_HEIGHT;
    private int MAX_WIDTH;
    private int VERTICAL_OFFSET;
    private float mScale;
    private static final int ANCHORED_GRAVITY = Gravity.TOP | Gravity.START;

    // Widgets
    private TextView mPopupTime;
    private TextView mPopupDescription;

    public DateDetailDisplayPopup (Activity context) {
        super(context);
        setOutsideTouchable(true);
        setFocusable(false);

        init(context);
    }

    @SuppressLint("InflateParams")
    private void init(Activity context) {
        LayoutInflater inflater = context.getLayoutInflater();
        mView = inflater.inflate(R.layout.date_detail_popup, null);
        setContentView(mView);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mScale = displayMetrics.density;
        MAX_HEIGHT = displayMetrics.heightPixels;
        MAX_WIDTH = displayMetrics.widthPixels;
        VERTICAL_OFFSET = dip2px(10);
        setWidth((int) (0.7 * MAX_WIDTH));

        setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        mPopupTime = mView.findViewById(R.id.popup_plan_time);
        mPopupDescription = mView.findViewById(R.id.popup_plan_description);

    }

    public void show(Point clickPoint, String datetimeStr, String description) {
        if (!isShowing()) {;
            mPopupTime.setText(datetimeStr);
            if (description.isEmpty())
                mPopupDescription.setText("无");
            else
                mPopupDescription.setText(description);

            int viewHeight = mView.getHeight();
            int viewWidth = mView.getWidth();

            if(clickPoint.x <= MAX_WIDTH / 2){
                if(clickPoint.y + viewHeight < MAX_HEIGHT){
                    setAnimationStyle(R.style.Animation_top_left);
                    if (clickPoint.x + viewWidth < MAX_WIDTH)
                        showAtLocation(mView, ANCHORED_GRAVITY, clickPoint.x , clickPoint.y + VERTICAL_OFFSET);
                    else
                        showAtLocation(mView, ANCHORED_GRAVITY, clickPoint.x - (clickPoint.x + viewWidth - MAX_WIDTH) , clickPoint.y + VERTICAL_OFFSET);
                }else {
                    setAnimationStyle(R.style.Animation_bottom_left);
                    if (clickPoint.x + viewWidth < MAX_WIDTH)
                        showAtLocation(mView, ANCHORED_GRAVITY, clickPoint.x , clickPoint.y - viewHeight - VERTICAL_OFFSET);
                    else
                        showAtLocation(mView, ANCHORED_GRAVITY, clickPoint.x - (clickPoint.x + viewWidth - MAX_WIDTH) , clickPoint.y - viewHeight - VERTICAL_OFFSET);
                }
            }else {
                if(clickPoint.y + viewHeight < MAX_HEIGHT){
                    setAnimationStyle(R.style.Animation_top_right);
                    showAtLocation(mView, ANCHORED_GRAVITY, clickPoint.x - viewWidth, clickPoint.y + VERTICAL_OFFSET);
                }else {
                    setAnimationStyle(R.style.Animation_bottom_right);
                    showAtLocation(mView, ANCHORED_GRAVITY, clickPoint.x - viewWidth, clickPoint.y - viewHeight - VERTICAL_OFFSET);
                }
            }
        }
    }

    private int dip2px(float dpValue) {
        return (int) (mScale * dpValue + 0.5f);
    }
}

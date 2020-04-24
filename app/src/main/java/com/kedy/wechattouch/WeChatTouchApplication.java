package com.kedy.wechattouch;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeChatTouchApplication extends Application {
    private List<Map<String, Object>> icons = null;

    // Newly added plan
    private boolean newPlan;
    private String planTitle;
    private int planIconID;
    private String planDatetime;
    private String planDescription;

    public List<Map<String, Object>> getIcons() {
        return icons;
    }

    public void setIcons (List<Map<String, Object>> icons) {
        this.icons = icons;
    }

    public boolean hasNewPlan() {
        return newPlan;
    }

    public List<Object> getNewPlan() {
        newPlan = false;
        List<Object> result = new ArrayList<>();
        result.add(planTitle);
        result.add(planIconID);
        result.add(planDatetime);
        result.add(planDescription);
        return result;
    }

    public void setPlan(String title, int iconID, String datetime, String description) {
        planTitle = title;
        planIconID =iconID;
        planDatetime = datetime;
        planDescription = description;
        newPlan = true;
    }
}

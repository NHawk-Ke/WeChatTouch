package com.kedy.wechattouch;

import android.app.Application;

public class WeChatTouchApplication extends Application {
    private Integer insertedPlanId = -1;

    public Integer getInsertedPlanId() {
        return insertedPlanId;
    }

    public void setInsertedPlanId(Integer id) {
        insertedPlanId = id;
    }

}

package com.levine.abllib;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.levine.abllib.utils.AblUtil;
import com.levine.abllib.utils.AblViewUtil;

import java.util.List;

import static android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT;

/**
 * 2019/4/21
 * 11:29
 * Levine
 * wechat 1483232332
 */
public class AblService extends AccessibilityService {

    private static AblService mAblService;

    public static AblService getInstance() {
        if (mAblService == null) {
            throw new NullPointerException("AblService辅助服务未开启");
        }
        return mAblService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAblService = this;
        init();
    }

    private void init() {

    }

    @Override
    protected boolean onGesture(int gestureId) {
        return super.onGesture(gestureId);
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LogUtils.v("onServiceConnected");
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        //配置监听的事件类型为界面变化|点击事件
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_CLICKED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        config.flags = AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        LogUtils.v(event.getPackageName() + "");
    }

    @Override
    public void onInterrupt() {

    }
}

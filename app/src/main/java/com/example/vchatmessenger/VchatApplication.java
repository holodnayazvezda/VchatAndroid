package com.example.vchatmessenger;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.activities.GroupViewActivity;

public class VchatApplication extends Application {

    private static int activityCount = 0;
    private static boolean isChatActivityStarted = false;
    private static boolean isGroupViewActivityStarted = false;

    @Override
    public void onCreate (){
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
    }

    public boolean isAppForeground() {
        return activityCount > 0;
    }

    public boolean isChatActivityStarted() {return isChatActivityStarted;}
    public boolean isGroupViewActivityStarted() {return isGroupViewActivityStarted;}

    @Override
    public void onTerminate (){
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    private static final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        public void onActivityCreated(Activity activity, Bundle bundle) {
            // заделка на будущее
        }

        public void onActivityDestroyed(Activity activity) {
            // заделка на будущее
        }

        public void onActivityPaused(Activity activity) {
            activityCount --;
            if (activity instanceof ChatViewActivity) {
                isChatActivityStarted = false;
            } else if (activity instanceof GroupViewActivity) {
                isGroupViewActivityStarted = false;
            }
        }

        public void onActivityResumed(Activity activity) {
            activityCount ++;
            if (activity instanceof ChatViewActivity) {
                isChatActivityStarted = true;
            } else if (activity instanceof GroupViewActivity) {
                isGroupViewActivityStarted = true;
            }
        }

        public void onActivitySaveInstanceState(Activity activity,
                                                Bundle outState) {
            // заделка на будущее
        }

        public void onActivityStarted(Activity activity) {
            // заделка на будущее
        }

        public void onActivityStopped(Activity activity) {
            // заделка на будущее
        }
    }
}
package com.study.camera2;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

   private static Context mContext;

    @Override
    public void onCreate() {
        mContext=this;
        super.onCreate();
    }

    public static  Context getContext(){

        if (mContext!=null){

            return mContext;
        }else{
            return null;
        }

    }
}

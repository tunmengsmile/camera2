/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.study.camera2.util;

import android.os.Build;


/***
 * 设置log 输出 打印行数据；
 */

public class Log {

    //所有使用这个类的应用，日志都将使用这个标签前缀。
    private static final String GLOBAL_TAG = "CameraApp";

    //也就是在开发版中（userdebug）该值为真打印log 日志，在用户版本（user ）为false  不打印log 日志，控制日志的开关；
    public static final boolean DEBUG;
    //在'userdebug'变体中运行时为真，日志级别高于VERBOSE。
    public static final boolean VERBOSE;


    static {
        //判断是否是开发版；
       // DEBUG = Build.TYPE.equals("userdebug");
        //强制打印日志；
        DEBUG = true;

       // isLoggable是android.util.Log提供的方法，用于检查指定TAG的level，
        //参数1 输出的标签前缀； 参数2 输出等级level >=大于设定的值的日志会被打印出来；isLoggable返回true，反之则返回false；

/*
        *//**
         * Priority constant for the println method; use Log.v.
         *//*
        public static final int VERBOSE = 2;

        *//**
         * Priority constant for the println method; use Log.d.
         *//*
        public static final int DEBUG = 3;

        *//**
         * Priority constant for the println method; use Log.i.
         *//*
        public static final int INFO = 4;

        *//**
         * Priority constant for the println method; use Log.w.
         *//*
        public static final int WARN = 5;

        *//**
         * Priority constant for the println method; use Log.e.
         *//*
        public static final int ERROR = 6;

        *//**
         * Priority constant for the println method.
         *//*
        public static final int ASSERT = 7;*/

        //输出等级的设置有如上几种
        VERBOSE = DEBUG && android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.VERBOSE);
    }

    /***
     * 组装一个TAG 对象；传入的是关键子字符串；
     */
    public static final class Tag {

        final String mValue;

        public Tag(String tag) {
            this.mValue = tag;
        }

        @Override
        public String toString() {
            return mValue;
        }
    }





    /**
     * TAG 中放置的是一个刷选标签的名字，除了前缀我们可以拿此标签进行日志的筛选；
     *  msg 是我们输出的日志内容；
     */
    public static void v(Tag tag, String... msg) {
        if (VERBOSE) {
            android.util.Log.v(GLOBAL_TAG, makeLogStringWithLongInfo(tag, msg));
        }
    }

    /**
     * Please use new version v(String, Throwable).
     * @deprecated
     */
    public static void v(Tag tag, String msg, Throwable tr) {
        if (VERBOSE) {
            android.util.Log.v(GLOBAL_TAG, makeLogStringWithLongInfo(tag, msg), tr);
        }
    }

    /**
     * Enabled if running in 'userdebug' variant and Log level is higher than VERBOSE.
     */
    public static void v(String... message) {
        if (VERBOSE) {
            android.util.Log.v(GLOBAL_TAG, makeLogStringWithLongInfo(message));
        }
    }

    /**
     * Enabled if running in 'userdebug' variant and Log level is higher than VERBOSE.
     */
    public static void v(String message, Throwable e) {
        if (VERBOSE) {
            android.util.Log.v(GLOBAL_TAG, makeLogStringWithLongInfo(message), e);
        }
    }

    /**
     * Please use new version d(String...).
     * @deprecated
     */
    public static void d(Tag tag, String... msg) {
        if (DEBUG || android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(GLOBAL_TAG, makeLogStringWithLongInfo(tag, msg));
        }
    }

    /**
     * Please use new version d(String, Throwable).
     * @deprecated
     */
    public static void d(Tag tag, String msg, Throwable tr) {
        if (DEBUG || android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(GLOBAL_TAG, makeLogStringWithLongInfo(tag, msg), tr);
        }
    }

    /**
     * Enabled if running in 'userdebug' variant or Log level is higher than DEBUG.
     */
    public static void d(String... message) {
        if (DEBUG || android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(GLOBAL_TAG, makeLogStringWithLongInfo(message));
        }
    }

    /**
     * Enabled if running in 'userdebug' variant or Log level is higher than DEBUG.
     */
    public static void d(String message, Throwable e) {
        if (DEBUG || android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(GLOBAL_TAG, makeLogStringWithLongInfo(message), e);
        }
    }

    /**
     * Please use new version i(String...).
     * @deprecated
     */
    public static void i(Tag tag, String... msg) {
        android.util.Log.i(GLOBAL_TAG, makeLogStringWithLongInfo(tag, msg));
    }

    /**
     * Please use new version i(String, Throwable).
     * @deprecated
     */
    public static void i(Tag tag, String msg, Throwable tr) {
        android.util.Log.i(GLOBAL_TAG, makeLogStringWithLongInfo(tag, msg), tr);
    }

    /**
     * Always enabled.
     */
    public static void i(String... message) {
        android.util.Log.i(GLOBAL_TAG, makeLogStringWithLongInfo(message));
    }

    /**
     * Always enabled.
     */
    public static void i(String message, Throwable e) {
        android.util.Log.i(GLOBAL_TAG, makeLogStringWithLongInfo(message), e);
    }

    /**
     * Please use new version w(String...).
     * @deprecated
     */
    public static void w(Tag tag, String... message) {
        android.util.Log.w(GLOBAL_TAG, makeLogStringWithShortInfo(tag, message));
    }

    /**
     * Please use new version w(String, Throwable).
     * @deprecated
     */
    public static void w(Tag tag, String message, Throwable e) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(tag, message), e);
    }

    /**
     * Always enabled.
     */
    public static void w(String message, Throwable e) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(message), e);
    }

    /**
     * Always enabled.
     */
    public static void w(String... message) {
        android.util.Log.w(GLOBAL_TAG, makeLogStringWithShortInfo(message));
    }


    /**
     * Please use new version e(String...).
     * @deprecated
     */
    public static void e(Tag tag, String... msg) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(tag, msg));
    }

    /**
     * Please use new version e(String, Throwable).
     * @deprecated
     */
    public static void e(Tag tag, String msg, Throwable tr) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(tag, msg), tr);
    }

    /**
     * Always enabled.
     */
    public static void e(String... message) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(message));
    }

    /**
     * Always enabled.
     */
    public static void e(String message, Throwable e) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(message), e);
    }


    public static boolean isDebugOsBuild() {
        return "userdebug".equals(Build.TYPE) || "eng".equals(Build.TYPE);
    }



    /************************************----------以下为辅助方法---------------*****************************************/

    /****
     * 将传入的ta 和输出信息拼接成字符串；
     * @param tag
     * @param message
     * @return
     */
    private static String makeLogStringWithLongInfo(Tag tag, String... message) {





       //返回一个表示该线程堆栈转储的堆栈跟踪元素数组。如果该线程尚未启动或已经终止，
       //则该方法将返回一个零长度数组。如果返回的数组不是零长度的，则其第一个元素代表堆栈顶，它是该序列中最新的方法调用。最后一个元素代表堆栈底，是该序列中最旧的方法调用。
//      StackTraceElement[] stackTraceElements=Thread.currentThread().getStackTrace();
//        System.out.println("The stackTraceElements length:"+stackTraceElements.length);
//        for(int i=0;i<stackTraceElements.length;i++){
//            System.out.println("\n---the  "+i+"  element"+"---");
//            //栈信息最近调用的信息
//            System.out.println("toString:"+stackTraceElements[i].toString());
                //类名字
//            System.out.println("ClassName:"+stackTraceElements[i].getClassName());
        //文件名字
//            System.out.println("FileName:"+stackTraceElements[i].getFileName());
        //行号
//            System.out.println("LineNumber:"+stackTraceElements[i].getLineNumber());
        //调用方法；
//            System.out.println("MethodName:"+stackTraceElements[i].getMethodName());*/
//        }
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder();
        if (tag != null) {
            builder.append(tag.toString());
        }
        appendTag(builder, stackTrace);
        appendTraceInfo(builder, stackTrace);
        //最后拼接出要打印的信息；
        for (String i : message) {
            builder.append(i);
        }
        return builder.toString();
    }


    /****
     *
     * 打印出文件名字， 调用方法，行号 拼接的输出信息；
     * 不需要传入的ta 只需要输出信息拼接成字符串；
     * @param message
     * @return
     */
    private static String makeLogStringWithLongInfo(String... message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder();
        appendTag(builder, stackTrace);
        appendTraceInfo(builder, stackTrace);
        for (String i : message) {
            builder.append(i);
        }
        return builder.toString();
    }

    /***
     * 只需要打印文件名字  和tag，
     * @param tag
     * @param message
     * @return
     */
    @Deprecated
    private static String makeLogStringWithShortInfo(Tag tag, String... message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder(tag.toString());
        appendTag(builder, stackTrace);
        for (String i : message) {
            builder.append(i);
        }
        return builder.toString();
    }

    /***
     *  只需要打印文件名字
     * @param message
     * @return
     */
    private static String makeLogStringWithShortInfo(String... message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder();
        appendTag(builder, stackTrace);
        for (String i : message) {
            builder.append(i);
        }
        return builder.toString();
    }

    /***
     * 拼接调用打印日志地方的类名字[HDRCheckerFilter]
     * @param builder
     * @param stackTrace
     */
    private static void appendTag(StringBuilder builder, StackTraceElement stackTrace) {
        builder.append('[');
        builder.append(suppressFileExtension(stackTrace.getFileName()));
        builder.append("] ");
    }

     /****
     * 拼接打印日志时调用的方法名字和行号；
      *
      * 如下规则  setFaces L105
     * @param builder
     * @param stackTrace
     */
    private static void appendTraceInfo(StringBuilder builder, StackTraceElement stackTrace) {
        builder.append(stackTrace.getMethodName());
        builder.append(" L");
        builder.append(stackTrace.getLineNumber());
        builder.append(" ");
    }

    /***
     * 截取文件名字；
     * @param filename
     * @return
     */
    private static String suppressFileExtension(String filename) {
        int extensionPosition = filename.lastIndexOf('.');
        if (extensionPosition > 0 && extensionPosition < filename.length()) {
            return filename.substring(0, extensionPosition);
        } else {
            return filename;
        }
    }



    private static String makeLogStringWithLongInfoL(String... message) {
        StackTraceElement stackTraceCall6 = null;
        StackTraceElement stackTraceCall5 = null;
        StackTraceElement[] stackTraceArray = Thread.currentThread().getStackTrace();

        if (stackTraceArray.length > 6) {
            stackTraceCall6 = Thread.currentThread().getStackTrace()[6];
        }
        stackTraceCall5 = Thread.currentThread().getStackTrace()[5];
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder();
        appendTag(builder, stackTrace);
        appendTraceInfo(builder, stackTrace);
        for (String i : message) {
            builder.append(i);
        }
        appendCall(builder, stackTraceCall5);
        if (stackTraceCall6 != null) {
            appendCall2(builder, stackTraceCall6);
        }
        return builder.toString();
    }

    private static void appendCall(StringBuilder builder, StackTraceElement stackTrace) {
        builder.append("  <- ");
        String file = stackTrace.getFileName();
        String fileSub = file.substring(0,file.length()-6);
        builder.append(fileSub);
        builder.append(":");
        builder.append(stackTrace.getMethodName());
        builder.append(":");
        builder.append(stackTrace.getLineNumber());
    }

    private static void appendCall2(StringBuilder builder, StackTraceElement stackTrace) {
        builder.append("  <- ");
        String file = stackTrace.getFileName();
        if (file != null) {
            String fileSub = file.substring(0, file.length() - 6);
            builder.append(fileSub);
            builder.append(":");
        }
        builder.append(stackTrace.getMethodName());
        builder.append(":");
        builder.append(stackTrace.getLineNumber());
    }
}

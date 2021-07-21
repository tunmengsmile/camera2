package com.study.camera2.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.study.camera2.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class TextActivity extends AppCompatActivity implements View.OnClickListener {
    //手机NAND闪存，这个叫做“外部存储的主要存储”(primary storage)键，
    public static String VOLUME_EXTERNAL_PATH = MediaStore.VOLUME_EXTERNAL_PRIMARY;
    //外置SDK 的名字键；
    private  static  String  EXTERNAL_SDK ="898d-1ef2";
    private  static  boolean  isSDCardStorage =false;
    PopupWindow popupWindow;
    com.study.camera2.util.Log.Tag TAG =new com.study.camera2.util.Log.Tag("TextActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // creatThread();
        //createJoin();
        com.study.camera2.util.Log.d(TAG,"onCreat");

        setContentView(R.layout.activity_text2);
        findViewById(R.id.module_face).setOnClickListener(this);
        findViewById(R.id.module_picture).setOnClickListener(this);
        findViewById(R.id.module_camera2_mediaRecorder).setOnClickListener(this);
        com.study.camera2.util.Log.d(TAG,"module_camera2_mediaRecorder");

     /*   findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater inflater = (LayoutInflater) TextActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View vPopupWindow = inflater.inflate(R.layout.module_text_dialog, null, false);//引入弹窗布局
                popupWindow = new PopupWindow(vPopupWindow, 300, ActionBar.LayoutParams.WRAP_CONTENT, true);
                //设置背景透明
                //addBackground();

                //设置进出动画
                //popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
                //引入依附的布局
                View parentView = LayoutInflater.from(TextActivity.this).inflate(R.layout.activity_text2, null);
                //相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
                popupWindow.showAtLocation(parentView, Gravity.CENTER|Gravity.RIGHT, 200, 500);



            }



        });
*/

       // dialog();



   /*     String filename ="/storage/emulated/0/DCIM/Camera/IMG_20201014_100647MP.jpg";
        File file = new File(filename);
        Uri uri = FileProvider.getUriForFile(this, "com.study.camera2.fileProvider", file);
        InputStream inputStream = null;

      File file1 = null;
        try {
            file1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath()+"/apps/123.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        copyFile(this,file1,uri);
*/

     // appSpecif();
       //获取外部存储的键名字；
     //   String volumeExtername = getVolumeExtername();
      // 根据URi 地址和id生成新URi地址
       //数据库字段；
      //  Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri(volumeExtername), 302);
      //spannier();
    }

    /***
     * join 的作用是当前线程等待调用join方法的线程执行完毕后再进行执行当前线程；
     */
    private void createJoin() {
        //再主线程中开启子线程执行耗时操作
        ceshiThread ceshiThread = new ceshiThread("Join线程");
        com.study.camera2.util.Log.d(ceshiThread.getName()+"---开启执行join----"+System.currentTimeMillis());

        ceshiThread.start();
        try {
            //阻塞主线程让ceshiThread执行完成后再执行主线程接下来的任务；
            ceshiThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        com.study.camera2.util.Log.d(ceshiThread.getName()+"---继续执行主线程----"+System.currentTimeMillis());



    }
   public class  ceshiThread extends  Thread{
        ceshiThread(String ThreadName){

            this.setName(ThreadName);

        }

       @Override
       public void run() {
           try {
               Thread.sleep(4000);
               com.study.camera2.util.Log.d(this.getName()+"---让join睡四秒----"+System.currentTimeMillis());
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
   }
 /*   private void spannier() {

        final Spinner sw = findViewById(R.id.magic_idp_dialog_spinner_w);

        // 将可选内容与ArrayAdapter连接起来
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_list, R.layout.simple_spinner_item);
        // 第一个参数为Context对象
        // 第二个参数为要显示的数据源,也就是在string.xml配置的数组列表
        // 第三个参数为设置Spinner的样式

        // 设置Spinner中每一项的样式
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        // 设置Spinner数据来源适配器
        sw.setAdapter(adapter);

        // 使用内部类形式来实现事件监听
        sw.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                *//*
                 * 第一个参数parent是你当前所操作的Spinner，可根据parent.getId()与R.id.
                 * currentSpinner是否相等，来判断是否你当前操作的Spinner,一般在onItemSelected
                 * 方法中用switch语句来解决多个Spinner问题。
                 * 第二个参数view一般不用到；
                 * 第三个参数position表示下拉中选中的选项位置，自上而下从0开始；
                 * 第四个参数id表示的意义与第三个参数相同。
                 *//*

                //对选中项进行显示
                //Toast用于临时信息的显示
                //第一个参数是上下文环境，可用this；
                //第二个参数是要显示的字符串；
                //第三个参数是显示的时间长短；
                String str = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "您选择的国家是："+str, Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
    }
*/


    // 设置背景透明
    private void addBackground() {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.7f;//调节透明度
        getWindow().setAttributes(lp);
        //dismiss时恢复原样
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
    }







    public  void  dialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this, 2);
        View view = this.getLayoutInflater().inflate(R.layout.activity_text, null);

        final Spinner sw = view.findViewById(R.id.magic_idp_dialog_spinner_w);

        /***
         * 监听选择宽高的尺寸的条目响应事件；
         */
        AdapterView.OnItemSelectedListener onItemLister = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //MagicLog.INSTANCE.showLog("i:  "+i);

                //设置设置宽高选中的索引；
                sw.setSelection(i);
                //设置设置宽高选中的索引；
                //sh.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        //设置宽度选择的监听器；
        sw.setOnItemSelectedListener(onItemLister);
        //设置高度的选择监听器
       // sh.setOnItemSelectedListener(onItemLister);

        builder.setTitle("你好")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //hintKbTwo(w, context);
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /***
     * 获取我们要存储在在外部存储的位置键名字；
     */
    private String  getVolumeExtername() {
        String retVolume = "";
        //是外置存储的话；
        if (isSDCardStorage){
            //获取可用存储的键；
            Set<String> externalVolumeNames = MediaStore.getExternalVolumeNames(this);
            Iterator<String> iterator = externalVolumeNames.iterator();

            while (iterator.hasNext()){
                String next = iterator.next();
                Log.e("dbc",next);

                if (EXTERNAL_SDK.equalsIgnoreCase(next)){

                    //走的是外置SDK村粗路径； 找到外置sdk 名字

                    retVolume=next;



                }
                return retVolume;
            }
            return retVolume;

        }else{

            return VOLUME_EXTERNAL_PATH;
        }


    }

    /***
     * 外部存储沙河路径；存储文件
     */
    private void appSpecif() {
        File[] externalMediaDirs = getExternalMediaDirs();

        for (int i = 0; i < externalMediaDirs.length; i++) {
            Log.e("dbc","getExternalMediaDirs===="+externalMediaDirs[i].getAbsolutePath());
        }

        File[] obbDirs = getObbDirs();

        for (int i = 0; i <obbDirs.length ; i++) {
            Log.e("dbc","getObbDirs===="+externalMediaDirs[i].getAbsolutePath());

        }

        File obbDir = getObbDir();
        Log.e("dbc","getObbDir===="+obbDir.getAbsolutePath());

        File externalCacheDir = getExternalCacheDir();
        Log.e("dbc","externalCacheDir===="+externalCacheDir.getAbsolutePath());

        File[] externalCacheDirs = getExternalCacheDirs();

        for (int i = 0; i < externalCacheDirs.length; i++) {
            Log.e("dbc","getExternalCacheDirs===="+externalCacheDirs[i].getAbsolutePath());
        }

        File[] externalFilesDirs = getExternalFilesDirs(Environment.DIRECTORY_PODCASTS);
        for (int i = 0; i < externalFilesDirs.length; i++) {
            Log.e("dbc","getExternalFilesDirs===="+externalFilesDirs[i].getAbsolutePath());
        }
        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
        Log.e("dbc","getExternalFilesDir===="+externalFilesDir.getAbsolutePath());


    }


    /***
     * 在Documents文件夹下面创建文件；
     */
     public void  getInsertDoucuments(){

         ContentValues mValue = new ContentValues();
         mValue.put(MediaStore.Video.Media.TITLE,"123.txt");
         mValue.put(MediaStore.Video.Media.DISPLAY_NAME,"123.txt");
         mValue.put(MediaStore.Video.Media.MIME_TYPE,"123.txt");


     }



    public static boolean copyFile (Context mxc,File sourceFilePath, final Uri insertUri) {
        if (insertUri == null) {
            return false;
        }
        ContentResolver resolver = mxc.getContentResolver();
        InputStream is = null;//输入流
        OutputStream os = null;//输出流
        try {
            is = resolver.openInputStream(insertUri);
            File sourceFile = sourceFilePath;


                os = new FileOutputStream(sourceFile); // 读入原文件
                //输入流读取文件，输出流写入指定目录
                return copyFileWithStream(os, is);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean copyFileWithStream (OutputStream os, InputStream is) {
        if (os == null || is == null) {
            return false;
        }
        int read = 0;
        while (true) {
            try {
                byte[] buffer = new byte[1444];
                while ((read = is.read(buffer)) != - 1){
                    os.write(buffer, 0, read);
                    os.flush();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    os.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.module_face:
                Intent intent1 = new Intent(this, FaceActivity.class);
                startActivity(intent1);
                break;
            case R.id.module_picture:
                Intent intent = new Intent(this, PhotoPictureCamera2Activity.class);
                startActivity(intent);
                break;
            case R.id.module_camera2_mediaRecorder:
                //跳转视频录制；
                Intent intent2 = new Intent(this, MediaRecoderCamera2Activity.class);
                startActivity(intent2);
                break;


        }

    }


    public  void  creatThread(){

        MyThread myThread = new MyThread("测试线程");
        myThread.start();

        MyRunnAble myRunnAble = new MyRunnAble();

        Thread thread = new Thread(myRunnAble);
        thread.setName("runnable ---线程");
        thread.start();

        MyCall myCall = new MyCall();

        FutureTask<String> stringFutureTask = new FutureTask<>(myCall);
         new Thread(stringFutureTask).start();
    }


    public  class  MyThread   extends  Thread{

         public  MyThread(String ThradName){

             //设置线程得优先级
             setPriority(Thread.MAX_PRIORITY);
             //设置线程得名字；
             setName(ThradName);
         }

        @Override
        public void run() {

             //执行线程；
            com.study.camera2.util.Log.d("执行线程"+getName());

        }
    }


    public  class MyRunnAble implements  Runnable{


        @Override
        public void run() {
            com.study.camera2.util.Log.d("实现runble 程序执行类");
        }
    }


    public  class  MyCall implements Callable<String> {


        @Override
        public String call() throws Exception {

            com.study.camera2.util.Log.d("实现MyCall程序执行类");
            return "call被执行了";
        }
    }

}

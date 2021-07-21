package com.study.camera2.activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.study.camera2.view.AutoFitTextureView;
import com.study.camera2.CameraHelper;
import com.study.camera2.R;

/****
 * 执行顺序；
 * 初始化动态授权,这是基本操作
 * 初始化一个子线程的Handler,Camera2的操作可以放在主线程也可以放在子线程.按例一般都是子线程里,但是Camera2只需要我们提供一个子线程的Handler就行了.
 * 初始化ImageReader,这个没有初始化顺序要求,并且它有数据回调接口,接口回调的图片数据我们直接保存到内部存储空间,所以提前初始化提供给后续使用.
 * 初始化TextureView,添加TextureView的接口回调.
 * 在TextureView的接口回调里回调启用成功方法后,我们开始初始化相机管理类initCameraManager
 * 然后继续初始化CameraDevice.StateCallback 摄像头设备状态接口回调类,先初始化提供给后续使用.(在这个接口类的开启相机的回调方法里,我们需要实现创建预览图像请求配置和创建获取数据会话)
 * 继续初始化CameraCaptureSession.StateCallback 摄像头获取数据会话类的状态接口回调类,先初始化提供给后续使用.(在这个接口类的配置成功回调方法里,我们需要实现预览图像或者实现拍照)
 * 继续初始化CameraCaptureSession.CaptureCallback 摄像头获取数据会话类的获取接口回调类,先初始化提供给后续使用.(啥都不干)
 * 判断摄像头前后,选择对应id
 * 打开指定id的摄像头
 * 实现拍照
 */

//camera2拍照
public class PhotoPictureCamera2Activity extends AppCompatActivity {

    //相机权限
    private String[] permission = {Manifest.permission.CAMERA};
     private AutoFitTextureView textureView;
     private TextView tv_num;
    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setFullScreenConfigs();
        //设置虚拟导航栏的隐藏；
        setSystemUiVisibility();
        textureView = findViewById(R.id.textureView);
        tv_num = findViewById(R.id.tv_num);
        initPermission();
        cameraHelper = new CameraHelper(this, textureView);
        findViewById(R.id.btnTakePic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraHelper!=null){
                    try {
                       cameraHelper.takePic();
                        //cameraHelper.takePicBurst(tv_num);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        findViewById(R.id.ivExchange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.switchCamera();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        setSystemUiVisibility();
    }

    /**
            * 初始化权限
     */
    private void initPermission() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,permission,1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         if (cameraHelper!=null){
             cameraHelper.releaseCamera();
         }
    }
    /***
     * 全屏显示
     */
    private void setFullScreenConfigs() {
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(flag);
    }

    /***
     * 隐藏虚拟导航栏
     */
    public void setSystemUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

}

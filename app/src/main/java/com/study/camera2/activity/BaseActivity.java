package com.study.camera2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.study.camera2.R;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
    }

    /***
     * 判断是否拥有权限；
     * @return
     */
    protected boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /***
     * 进行权限申请；
     */
    protected void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (       shouldShowRequestPermissionRationale(PERMISSION_CAMERA)
                    || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)
                    || shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(RECORD_AUDIO)
            )
            requestPermissions(new String[]{PERMISSION_CAMERA,
                    PERMISSION_STORAGE,ACCESS_FINE_LOCATION,RECORD_AUDIO}, PERMISSIONS_REQUEST);
        }
    }


/***
 *  权限申请的回调结果
 */



@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean isGranted = false;
            if (grantResults.length >= 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED&& grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            }
            if (isGranted) {
                //获取到权限；
                RequestPerssionSucess();

            } else {
                finish();
            }
        }

    }

     abstract void  RequestPerssionSucess();







    /***
     * 全屏显示
     */
    public void setFullScreenConfigs() {
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

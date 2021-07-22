package com.study.camera2.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.study.camera2.CameraMediaRecorderHelper;
import com.study.camera2.R;
import com.study.camera2.view.AutoFitTextureView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/****
 * camera2 +mediaRecorder 实现视频录制， 同时录制时不重新创建sesssion这种方式进行视频录制；
 */
public class MediaRecoderCamera2Activity extends AppCompatActivity implements View.OnClickListener {
    private AutoFitTextureView textureView;
    private ImageView btnStart;
    private CameraMediaRecorderHelper cameraMediaRecorderHelper;

    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        setContentView(R.layout.activity_media_recoder_camera2);
        setFullScreenConfigs();
        btnStart = findViewById(R.id.btnStart);
        findViewById(R.id.ivExchange).setOnClickListener(this);
        btnStart.setOnClickListener(this);
        //设置虚拟导航栏的隐藏；
        setSystemUiVisibility();
        textureView = findViewById(R.id.textureView);
        CameraMediaRecorderHelper();

    }



    /***
     * 创建录制视频的实例类；
     */
    private void CameraMediaRecorderHelper() {
        cameraMediaRecorderHelper = new CameraMediaRecorderHelper(this, textureView);

    }


    void RequestPerssionSucess() {
        //获取到权限；
        CameraMediaRecorderHelper();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnStart:

                if (cameraMediaRecorderHelper.ismIsRecordingVideo()) {
                    btnStart.setImageResource(R.mipmap.ic_start);
                    cameraMediaRecorderHelper.stopRecordingVideo();

                } else {
                   btnStart.setImageResource(R.mipmap.ic_stop);
                    cameraMediaRecorderHelper.startRecordingVideo();
                }
                break;


            case R.id.ivExchange:
                //切换前后摄像头
              /*  if (cameraMediaRecorderHelper!=null)
                    cameraMediaRecorderHelper.switchCamera();*/
                //
                cameraMediaRecorderHelper.delete(getContentResolver(),"1609835774439.mp4");

                break;


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraMediaRecorderHelper!=null){
            cameraMediaRecorderHelper.releaseCamera();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        setSystemUiVisibility();
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
            if (  shouldShowRequestPermissionRationale(PERMISSION_CAMERA)
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

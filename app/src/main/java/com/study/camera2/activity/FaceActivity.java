package com.study.camera2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.RectF;
import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.study.camera2.CameraFaceHelper;
import com.study.camera2.R;
import com.study.camera2.view.AutoFitTextureView;
import com.study.camera2.view.FaceView;

import java.util.ArrayList;

/***
 * 人脸检测；
 */
public class FaceActivity extends AppCompatActivity implements CameraFaceHelper.FaceDetectListener {

    private AutoFitTextureView mTv;
    private CameraFaceHelper cameraFaceHelper;
    private FaceView faceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        mTv = findViewById(R.id.textureView);
        faceView = findViewById(R.id.faceView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraFaceHelper = new CameraFaceHelper(this, mTv);
        cameraFaceHelper.setFaceDetectListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraFaceHelper!=null){
            cameraFaceHelper.releaseCamera();
        }
    }

    @Override
    public void onFaceDetect(Face[] var1,  ArrayList<RectF> faces) {
        //获取到预览设置的尺寸
        Size previewSize = cameraFaceHelper.getPreviewSize();
        faceView.setFaces(faces,previewSize);

    }

    @Override
    protected void onResume() {
        setSystemUiVisibility();
        super.onResume();
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

package com.study.camera2.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
/***
 * 人脸框view
 */
public class FaceView  extends View {

    private Paint mPaint;
    private String mCorlor = "#42ed45";
    private ArrayList<RectF> mFaces;
    private Context context;
    private Object mLock = new Object();
    private long mLastTimestamp = 0;
    Size preSize;
    public FaceView(Context context) {
        super(context);
        context=context;
        init();

    }

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        context=context;
        init();

    }

    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        context=context;
        init();
    }
    private void init() {

        mPaint = new Paint();
        mPaint.setColor(Color.parseColor(mCorlor));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(7);
        mPaint.setAntiAlias(true);
    }

    /***
     * 更新人脸数据；
     * @param faces
     * @param previewSize
     */
    public void  setFaces(ArrayList<RectF> faces, Size previewSize) {
        preSize=previewSize;
        synchronized (mLock) {
            mFaces.clear();
            if (faces == null) {
                postInvalidate();
                return;
            }
            this.mFaces = faces;

            postInvalidate();
            mLastTimestamp = System.currentTimeMillis();
        }
    }

   /* @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        long diff = System.currentTimeMillis() - mLastTimestamp;
        synchronized (mLock) {

            if (mFaces.size() > 0) {
                for (RectF data : mFaces) {
                        //获取到相机设置的预览尺寸的宽高；
                        int preview_w = preSize.getWidth();
                        int preview_h = preSize.getHeight();
                        Log.i("cover", "onDraw preview_w: "+preview_w);

                        //得到当前 人脸框view 的宽高；
                        int sw = getWidth();
                        int sh = getHeight();

                        Log.e("cover",sw+"----sw--sh----------"+sh);
                        //计算一个缩放比例
                        float scalex = sw / (float) preview_w;//0.75
                        float scaley = sh / (float) preview_h;//0.99
                        Log.e("cover",scalex+"----scalex--scaley----------"+scaley);
                        float scale = scalex;
                        //获取到缩放比例最大的一个值，以此为基准；
                        if (scale < scaley) {
                            scale = scaley;
                        }
                        //人脸检测到的人脸位置区域值；
                           data.centerX();
                        float x = data.left;
                        float y = data.face.bbox_4_render.y;
                        float w = data.face.bbox_4_render.width;
                        float h = data.face.bbox_4_render.height;
                        //后置摄像头
                        if (CameraActivity.mCameraId == CameraCharacteristics.LENS_FACING_BACK) {
                            x = preview_w - (x + w);
                        }
                        //前置摄像头
                        if (CameraActivity.mCameraId == CameraCharacteristics.LENS_FACING_FRONT) { // for aikit
                            x = preview_w - (x + w);
                        }
                        x = (int) (x * scale);
                        y = (int) (y * scale);
                        w = (int) (w * scale);
                        h = (int) (h * scale);
                        String name = data.name;
                        int color = Color.parseColor("#FFA500");
                        String msg;
                        msg = String.format(getResources().getString(R.string.stranger_flag), data.temperature);
                        mPntFace.setColor(color);
                        mPntTxt.setColor(color);
                        mPaintCircle.setColor(color);
                        mPaintCircle.setStrokeWidth(2f);
                        float centerX = x + w/2;
                        float centerY = y + h/2;
                        //半径值；
                        float radius = (float) (Math.sqrt(w*w + h*h)/2);
                       *//* if (data.temperature >= 34&&data.temperature <=50){


                            if (data.temperature>36.9 &&data.temperature<=37.5){
                                msg=36.9+"℃";

                            }
                            //画布上绘制文本；
                            //x的坐标= 圆心点坐标x - 文本长度的一半
                            //y的坐标= 圆心点坐标y- 圆弧半径- 文本宽度
                            c.drawText(msg, centerX - mPntTxt.measureText(msg)/2, centerY - radius - 15f, mPntTxt);
                        }*//*
                        //画一个描边圆
                        c.drawCircle(centerX, centerY, radius, mPaintCircle);
                }
            }
        }
        invalidate();
    }

*/




}

package com.study.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.study.camera2.util.BitmapUtils;
import com.study.camera2.view.AutoFitTextureView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/***
 * camera helper 帮助类
 */
public class CameraFaceHelper {
    private Activity mActivity;
    private AutoFitTextureView mTextureView;
    private CameraManager mCameraManager;
    private ImageReader mImageReader= null;
    private android.hardware.camera2.CameraDevice CameraDevice =null;
    private CameraCaptureSession mCameraCaptureSession= null;
    private String mCameraId = "0";
    //Camrea 的一些属性
    private CameraCharacteristics mCameraCharacteristics;
    ////默认使用前置摄像头
    private int currentCameraId = CameraCharacteristics.LENS_FACING_FRONT;
    private boolean canTakePic = true  ;                 //是否可以拍照
    private Handler mCameraHandler;
    //创建线程
    private HandlerThread handlerThread;
    //预览的捕获请求
    private CaptureRequest.Builder mCaptureRequest;
    //拍照的捕获请求
    private CaptureRequest.Builder captureRequest;
    private SurfaceTexture surfaceTexture;
    private Surface mSurface;

    public Size getPreviewSize() {
        return previewSize;
    }

    //获取到最佳预览尺寸；
    private Size previewSize;
    private int mDisplayRotation;
    //从textureView 回调中获取到可用预览的监听
    int  preWidth ;
    int  preHeight;
    //图片矫正的方向；
    int  mJpegOrientation;
    //手机方向；
    int  iphone ;
    private  MyOrientationEventListener myOrientationEventListener;
    //人脸检测开关；
    private  int mFaceDetectMode = CaptureResult.STATISTICS_FACE_DETECT_MODE_OFF;     //人脸检测模式
    //是否开启人脸检测
    private  boolean openFaceDetect = true;
    //人脸检测坐标转换矩阵
    private  Matrix mFaceDetectMatrix;
    //保存人脸坐标信息
    ArrayList<RectF> mFacesRect= new ArrayList<RectF>();
    //人脸检测监听回调
    private FaceDetectListener mFaceDetectListener;

    @SuppressLint("NewApi")
    public CameraFaceHelper(Activity mActivity, AutoFitTextureView mTextureView) {
        mFaceDetectMatrix = new Matrix();
        mDisplayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation() ; //手机方向
         //人脸检测回调
        //设置手机方向的监听器；
        myOrientationEventListener = new MyOrientationEventListener(mActivity);
        //开启监听；
       // myOrientationEventListener.disable(); //当不需要监听的时候,一定要执行结束监听,即使结束activity也没用,一样会监听
        myOrientationEventListener.enable();
        this.mActivity = mActivity;
        this.mTextureView = mTextureView;
        //初始化线程
        initHandler();
        //设置textureView的监听；
        initTextureViewSetSurfaceTextureListener(mTextureView);
    }

    /***人脸检测监听的回调接口****/
    public interface FaceDetectListener {
        void onFaceDetect( Face[] var1,  ArrayList<RectF> faces );
    }

    public final void setFaceDetectListener(FaceDetectListener listener) {
        this.mFaceDetectListener = listener;
    }






    /***
     * 1
     * 初始化handler 线程绑定handlerThread 的looper ，相机传递参数时需要这个线程
     */
    private void initHandler() {

         handlerThread = new HandlerThread("camera_handler_Therad");
         handlerThread.start();
         mCameraHandler = new Handler(handlerThread.getLooper());


    }

    /***
     * 2
     * 设置TextureView监听器， 监听surfaceview 的回调
     * @param mTextureView
     */
    private void initTextureViewSetSurfaceTextureListener(TextureView mTextureView) {

        mTextureView.setSurfaceTextureListener((TextureView.SurfaceTextureListener)(new TextureView.SurfaceTextureListener() {
            public void onSurfaceTextureSizeChanged(@Nullable SurfaceTexture surface, int width, int height) {


            }

            public void onSurfaceTextureUpdated(@Nullable SurfaceTexture surface) {
            }

            public boolean onSurfaceTextureDestroyed(@Nullable SurfaceTexture surface) {
                //释放camera
                releaseCamera();
                return true;
            }

            public void onSurfaceTextureAvailable(@Nullable SurfaceTexture surface, int width, int height) {
                  preHeight=height;
                  preWidth=width;
                  openCamera();
            }
        }));

    }




    public  void openCamera(){
        //创建相机的管理者
        mCameraManager= (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        //设置相机的特征信息；
        setCameraInfo(mCameraManager);

        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mActivity,"没有相机劝降",Toast.LENGTH_SHORT);
            return;
        }
        try {
            mCameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    /***
     * 设置摄像头信息；
     * @param mCameraManager
     */
    public  void setCameraInfo( CameraManager mCameraManager) {


        try {
            // 返回当前设备中可用的相机列表
            String[] cameraIdList = mCameraManager.getCameraIdList();
            if (cameraIdList == null || cameraIdList.length <= 0) {
                Toast.makeText(mActivity, "没有可用相机", Toast.LENGTH_SHORT).show();
                return;

            }
            //遍历所有可用的相机设备；
            for (int i = 0; i < cameraIdList.length; i++) {
                //根据摄像头id返回该摄像头的相关信息
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraIdList[i]);
                //获取摄像头方向。前置摄像头（LENS_FACING_FRONT）或 后置摄像头
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == currentCameraId) {
                    //设备等于后置；
                    mCameraId = cameraIdList[i];
                    //后置特征信息；
                    mCameraCharacteristics = cameraCharacteristics;
                }
            }

            //相机是否支持新特征
            int supportLevel = mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

            if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {

                Log.e("initCameraInfo: ", "相机硬件不支持新特性");
                return;
            }
            // 获取摄像头支持的配置属性
            //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // 获取摄像头支持的输出图片的最大尺寸
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
            // 创建一个ImageReader对象，用于获取摄像头的图像数据
            initImageAvailableListener(largest.getWidth(), largest.getHeight());
            // 预览尺寸集合中的数据；
            Size[] preSizes = map.getOutputSizes(SurfaceTexture.class);
            // 获取最佳的预览尺寸
            // previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), preWidth, preHeight, largest);
            // Size previewSize =chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), pictureSize.getWidth(), pictureSize.getHeight(), 1080, 1920);
            previewSize = getMatchingPreSize(preSizes);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (openFaceDetect)
            initFaceDetect();   //初始化人脸检测相关参数


    }

    /***
     * 初始化人脸检测的信息；
     */
    private void initFaceDetect() {
        //同时检测到人脸的数量
        int faceDetectCount = mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
        //人脸检测的模式
        int[] faceDetectModes = mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
        /*
        相机支持的人脸检测模式分为3种：
        STATISTICS_FACE_DETECT_MODE_FULL ：
        完全支持。返回人脸的矩形位置、可信度、特征点(嘴巴、眼睛等的位置)、和 人脸ID
        STATISTICS_FACE_DETECT_MODE_SIMPLE：
        支持简单的人脸检测。返回的人脸的矩形位置和可信度。
        STATISTICS_FACE_DETECT_MODE_OFF：
        不支持人脸检测*/

        for (int i = 0; i <faceDetectModes.length ; i++) {
            int faceDetectMode = faceDetectModes[i];
            if (faceDetectMode==CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_OFF){
                //不支持人脸检测
                mFaceDetectMode=CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_OFF;
            }else if (faceDetectMode==CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_FULL){
                //完全支持人脸检测；
                mFaceDetectMode=CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_FULL;

            }else if (faceDetectMode==CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE){
                //支持人脸简单检测；
                mFaceDetectMode=CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE;
            }

        }
        if (mFaceDetectMode == 0) {
            Log.e("TAG","相机硬件不支持人脸检测");
        } else {

            //获取传感器活动区域大小的矩形

           /* 也就是说，这块矩形区域是相机传感器捕捉图像数据时使用的范围。检测人脸所得到的坐标也正是基于此矩形的。
            当我们拿到了这块矩形后，又知道预览时的矩形（即AutoFitTextureView的大小），这样我们就能找出这两块矩形直接的转换关系（即 mFaceDetectMatrix），从而就能够将传感器中的人脸坐标转换成预览页面中的坐标
            */


            //获取相机的成像区域，硬件物理层的成像区域；
            Rect cRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);//获取成像区
            Log.e("cRect: ", cRect.width()+"width----height"+cRect.height());

            Rect activeArraySizeRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);


            //相机成像区和预览成像区的比例；
            float scaledWidth = (float)this.previewSize.getWidth() / (float)activeArraySizeRect.width();
            float scaledHeight = (float)this.previewSize.getHeight() / (float)activeArraySizeRect.height();


            //是否是前置摄像头；
            boolean mirror = currentCameraId == 0;
            //传感器方向；
            int sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            //对矩阵进行旋转；
            mFaceDetectMatrix.setRotate(sensorOrientation);

            //对矩阵进行缩放；
            mFaceDetectMatrix.postScale(mirror ? -scaledWidth : scaledWidth, scaledHeight);

            if (exchangeWidthAndHeight(iphone, sensorOrientation)) {
                //进行移动；
                mFaceDetectMatrix.postTranslate((float)previewSize.getHeight(), (float)this.previewSize.getWidth());
            }
            Log.e("TAG","成像区域  " + activeArraySizeRect.width() + "  " + activeArraySizeRect.height() + " 比例: " + (float)activeArraySizeRect.width() / (float)activeArraySizeRect.height());
            Log.e("TAG","预览区域  " + this.previewSize.getWidth() + "  " + previewSize.getHeight() + " 比例 " + (float)previewSize.getWidth() / (float)previewSize.getHeight());
            for (int i = 0; i <faceDetectModes.length ; i++) {
                int mode = faceDetectModes[i];
                Log.e("TAG","支持的人脸检测模式 " + mode);
            }
            Log.e("TAG","同时检测到人脸的数量 " + faceDetectCount);
        }
    }







    private final boolean exchangeWidthAndHeight(int displayRotation, int sensorOrientation) {
        boolean exchange = false;
        switch(displayRotation) {
            case 0:
            case 2:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    exchange = true;
                }
                break;
            case 1:
            case 3:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    exchange = true;
                }
                break;
            default:
                Log.e("TAG","Display rotation is invalid: " + displayRotation);
        }

        Log.e("TAG","屏幕方向  " + displayRotation);
        Log.e("TAG","相机方向  " + sensorOrientation);
        return exchange;
    }

    /****
     * 创建图片读取器
     * @param width
     * @param height
     */
    private void initImageAvailableListener(int width, int height) {
        //创建图片读取器,参数为分辨率宽度和高度/图片格式/需要缓存几张图片,我这里写的1意思是获取1张照片
        mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);
        /***
         * 设置图片监听器，监听可用图片
         */
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
//        这里的image.getPlanes()[0]其实是图层的意思,因为我的图片格式是JPEG只有一层所以是geiPlanes()[0],如果你是其他格式(例如png)的图片会有多个图层,就可以获取指定图层的图像数据　　　　　　　
                ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                image.close();
                long timestamp = System.currentTimeMillis();
                String picfile = BitmapUtils.getPicfile(bytes, timestamp + ".jpg");

              //前置时左右翻转时处理，后置是正常的，不需要处理了
                    if ("1".equals(mCameraId)){
                        //读取图片的角度；
                        int degree = readPictureDegree(picfile);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                      //  旋转后的bitmap //生成图片时角度会消失所以我们需要提前转正；
                        Bitmap rotaingBitamp = BitmapUtils.rotaingBitamp(degree, bitmap);
                        //镜像处理
                        bitmap = BitmapUtils.mirror(rotaingBitamp);
                        BitmapUtils.saveBitmap(bitmap,timestamp+".jpg");
                    }




            }
        },mCameraHandler);


    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        System.out.println("图片旋转了：" + degree + " 度");
        return degree;
    }






    private Size getMatchingPreSize(Size[] previewSize) {
        Size selectSize = null;
        try {


            int textureViewWidth = preWidth; //屏幕分辨率宽
            int textureViewHeigt = preHeight; //屏幕分辨率高
            /**
             * 循环40次,让宽度范围从最小逐步增加,找到最符合屏幕宽度的分辨率,
             * 你要是不放心那就增加循环,肯定会找到一个分辨率,不会出现此方法返回一个null的Size的情况
             * ,但是循环越大后获取的分辨率就越不匹配
             */
            for (int j = 1; j < 41; j++) {
                for (int i = 0; i < previewSize.length; i++) { //遍历所有Size
                    Size itemSize = previewSize[i];
                    Log.e("tag","当前itemSize 宽="+itemSize.getWidth()+"高="+itemSize.getHeight());
                    //判断当前Size高度小于屏幕宽度+j*5  &&  判断当前Size高度大于屏幕宽度-j*5
                    if (itemSize.getHeight() < (textureViewWidth + j*5) && itemSize.getHeight() > (textureViewWidth - j*5)) {
                        if (selectSize != null){ //如果之前已经找到一个匹配的宽度
                            if (Math.abs(textureViewHeigt-itemSize.getWidth()) < Math.abs(textureViewHeigt - selectSize.getWidth())){ //求绝对值算出最接近设备高度的尺寸
                                selectSize = itemSize;
                                continue;
                            }
                        }else {
                            selectSize = itemSize;
                        }

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("tag", "getMatchingSize2: 选择的分辨率宽度="+selectSize.getWidth());
        Log.e("tag", "getMatchingSize2: 选择的分辨率高度="+selectSize.getHeight());
        return selectSize;
    }






    /**
     *
     * 摄像头状态接口回调类:
     *主要是负责回调摄像头的开启/断开/异常/销毁.我们使用CameraManager打开指定id的摄像头时需要添加这个回调.
     */

    public CameraDevice.StateCallback mStateCallback=new android.hardware.camera2.CameraDevice.StateCallback() {

        /**
         * 摄像头打开时
         * @param camera
         */
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            CameraDevice = camera;
            takePreview();

        }

        /***
         * 摄像头断开；
         * @param camera
         */
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        /***
         * 摄像头出现异常操作
         * @param camera
         * @param error
         */
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }

        /**
         * 摄像头关闭时
         * @param camera
         */
        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
        }
    };


    /**
     * 开始预览
     *
     *  * 通过cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) 创建一个用于预览的Builder对象
     *      * 为该Builder对象添加一个Surface对象，并设置各种相关参数
     *      * 通过cameraDevice.createCaptureSession创建一个会话，第一个参数中传了一个 surface 和 mImageReader?.surface。这表明了这次会话的图像数据的输出到这两个对象
     *      * 当会话创建成功时，通过 session.setRepeatingRequest(captureRequestBuilder.build(), mCaptureCallBack, mCameraHandler) 发起预览请求
     *      *
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePreview() {
        //获取surfacetexture;
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        //设置TextureView的缓冲区大小
        Log.e("KK","width:"+previewSize.getWidth()+"---height-"+previewSize.getHeight());
        mSurfaceTexture.setDefaultBufferSize( previewSize.getWidth(), previewSize.getHeight());
        //获取Surface显示预览数据
        Surface mSurface = new Surface(mSurfaceTexture);
        try {
            //创建预览请求
            mCaptureRequest = CameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //开启人脸检测，人脸检测为可用；
            if (openFaceDetect && this.mFaceDetectMode !=  CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF) {
                mCaptureRequest.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, 1);
            }

            // 设置自动对焦模式
            mCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //设置Surface作为预览数据的显示界面
            mCaptureRequest.addTarget(mSurface);
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            CameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                //配置预览成功；
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCameraCaptureSession = session;
                        //注意这里使用的是 setRepeatingRequest() 请求通过此捕获会话无休止地重复捕获图像。用它来一直请求预览图像
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest.build(), mSessionCaptureCallback, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                /**
                 * 配置预览失败；
                 * @param session
                 */
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }





    public class MyOrientationEventListener extends OrientationEventListener{

        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation != ORIENTATION_UNKNOWN) {
                iphone = normalize(orientation);
                Log.e("iphone",iphone+"");
            }
        }
        private int normalize(int orientation) {
            if ((orientation > 315) || (orientation <= 45)) {
                return 0;
            }
            if (orientation > 45 && orientation <= 135) {
                return 90;
            }
            if (orientation <= 225) {
                return 180;
            }
            if (orientation > 225 && orientation <= 315) {
                return 270;
            }
            return  0;
        }
    }
    /****
     *
     * @param mOrientation
     */
    private void updateJpegOrientation(int mOrientation) {
        if (mCameraManager == null) {
            return;
        }
        try {
            CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(mCameraId);
            //获取到传感器方向；
            int sensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Log.e("iphone",sensorOrientation+"");
            //后置摄像时计算角度；
            if (cc.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
                //当前屏幕的自然方向 加上传感器的方向；
                this.mJpegOrientation = (mOrientation + sensorOrientation) % 360;
            } else {
                //前置摄像头；
                if (mOrientation % 180 == 0) {
                    this.mJpegOrientation = (mOrientation + sensorOrientation) % 360;
                } else {
                    this.mJpegOrientation = (sensorOrientation - mOrientation + 360) % 360;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }






    /**
     * 为Size定义一个比较器Comparator获取支持的最大尺寸；
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }




    /***
     * 拍照；
     * @throws CameraAccessException
     */
    public void  takePic() throws CameraAccessException {
        if ( CameraDevice== null || !mTextureView.isAvailable() || !canTakePic) {
            return;
        }
        // 创建一个适合于静态图像捕获的请求，图像质量优先于帧速率
        captureRequest  = CameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        // 闪光灯
        captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        // 自动对焦
        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        updateJpegOrientation(iphone);
        captureRequest.set(CaptureRequest.JPEG_ORIENTATION, mJpegOrientation);
        Surface surface = mImageReader.getSurface();
        captureRequest.addTarget(surface);
        CaptureRequest request = captureRequest.build();
        // 停止连续取景
        mCameraCaptureSession.stopRepeating();
        //发起拍照请求
        mCameraCaptureSession.capture(request, mSessionCaptureCallback, mCameraHandler); //获取拍照
    }





    /**
     *
     *
     * 摄像头获取会话数据回调监听；
     */
    public  CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            // 重设自动对焦模式
         //   captureRequest.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            // 设置自动曝光模式
            //captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            try {
                //重新进行预览
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest.build(), mSessionCaptureCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            //开启人脸检测，检测模式可用
            if (openFaceDetect && mFaceDetectMode != CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF){
                //获取到结果进行处理；
                handleFaces(result);
            }



        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }



    };

    private final void handleFaces(TotalCaptureResult result) {
        final Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        mFacesRect.clear();
        for (int i = 0; i <faces.length ; i++) {
            Face face = faces[i];
            Rect bounds = face.getBounds();
            int left = bounds.left;
            int top = bounds.top;
            int right = bounds.right;
            int bottom = bounds.bottom;
            RectF rawFaceRect = new RectF((float)left, (float)top, (float)right, (float)bottom);
            this.mFaceDetectMatrix.mapRect(rawFaceRect);
            RectF resultFaceRect;
            if ( currentCameraId == CaptureRequest.LENS_FACING_FRONT) {
                resultFaceRect=rawFaceRect;
            }else{
                resultFaceRect= new RectF(rawFaceRect.left, rawFaceRect.top - (float)this.previewSize.getWidth(), rawFaceRect.right, rawFaceRect.bottom - (float)this.previewSize.getWidth());
            }
            mFacesRect.add(resultFaceRect);
            Log.e("TAG","原始人脸位置: " + bounds.width() + " * " + bounds.height() + "   " + bounds.left + ' ' + bounds.top + ' ' + bounds.right + ' ' + bounds.bottom + "   分数: " + face.getScore());
            Log.e("TAG","转换后人脸位置: " + resultFaceRect.width() + " * " + resultFaceRect.height() + "   " + resultFaceRect.left + ' ' + resultFaceRect.top + ' ' + resultFaceRect.right + ' ' + resultFaceRect.bottom + "   分数: " + face.getScore());


        }

        this.mActivity.runOnUiThread((Runnable)(new Runnable() {
            public final void run() {
                if (mFaceDetectListener != null) {
                    mFaceDetectListener.onFaceDetect(faces,mFacesRect);
                }

            }
        }));
        Log.e("TAG","onCaptureCompleted  检测到 " + faces.length + " 张人脸");
    }












    /**
     * 切换摄像头
     */
    public void switchCamera() {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                //遍历camera所有id 拿到相应的特征；
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                if (currentCameraId == CameraCharacteristics.LENS_FACING_BACK && characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    currentCameraId=CameraCharacteristics.LENS_FACING_FRONT;
                    CameraDevice.close();
                    openCamera();
                    break;
                } else if (currentCameraId == CameraCharacteristics.LENS_FACING_FRONT && characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    currentCameraId=CameraCharacteristics.LENS_FACING_BACK;
                    CameraDevice.close();
                    openCamera();
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /***
     * 释放资源；
     */
    public void  releaseCamera() {
        if (myOrientationEventListener!=null)
        myOrientationEventListener.disable();
        if (mCaptureRequest != null) {
            mCaptureRequest.removeTarget(mSurface);
            mCaptureRequest = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (surfaceTexture != null){
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (mCameraCaptureSession != null) {
            try {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCameraCaptureSession = null;
        }

        if (CameraDevice != null) {
            CameraDevice.close();
            CameraDevice = null;
        }
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacksAndMessages(null);
            mCameraHandler = null;
        }
        if ( handlerThread!= null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }

        mCameraManager = null;
        mSessionCaptureCallback = null;
        mStateCallback = null;







    }




}

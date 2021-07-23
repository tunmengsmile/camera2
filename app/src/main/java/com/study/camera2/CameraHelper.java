
package com.study.camera2;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.study.camera2.activity.CameraActionSound;
import com.study.camera2.util.BitmapUtils;
import com.study.camera2.view.AutoFitTextureView;
import com.study.camera2.view.MediaStoreSave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/***
 * camera helper 帮助类
 */
public class CameraHelper {
    private Activity mActivity;
    private AutoFitTextureView mTextureView;
    private CameraManager mCameraManager;
    private ImageReader mImageReader = null;
    private android.hardware.camera2.CameraDevice CameraDevice = null;
    private CameraCaptureSession mCameraCaptureSession = null;
    private String mCameraId = "0";
    //Camrea 的一些属性
    private CameraCharacteristics mCameraCharacteristics;
    ////默认使用前置摄像头
    private int currentCameraId = CameraCharacteristics.LENS_FACING_BACK;
    private boolean canTakePic = true;                 //是否可以拍照
    private Handler mCameraHandler;
    //创建线程
    private HandlerThread handlerThread;
    //预览的捕获请求
    private CaptureRequest.Builder mpreviewRequest;
    //拍照的捕获请求
    private CaptureRequest.Builder captureRequest;
    private SurfaceTexture surfaceTexture;
    //预览使用的surface;
    private Surface previewSurface;
    //获取到最佳预览尺寸；
    private Size previewSize;
    //从textureView 回调中获取到可用预览的监听
    int preWidth;
    int preHeight;
    //图片矫正的方向；
    int mJpegOrientation;
    //手机方向；
    int iphone;
    private final MyOrientationEventListener myOrientationEventListener;
    private final CameraActionSound cameraActionSound;
    //下发数组数据；
    public static final CaptureRequest.Key<int[]> BM_ISSUED = new CaptureRequest.Key<int[]>("ts.algo.resize.roi", int[].class);

   //下发参数设置；x,y,w,h
    int[]  bmRoi= new int[]{0,0,1920,1080};
    //回传数据；
    public static final CaptureResult.Key<int[]> BM_RESULT = new CaptureResult.Key<int[]>("ts.algo.resize.roi", int[].class);


    @SuppressLint("NewApi")
    public CameraHelper(Activity mActivity, AutoFitTextureView mTextureView) {
        //添加声音的特效；
        cameraActionSound = new CameraActionSound();
        //预加载声音；
        cameraActionSound.load(CameraActionSound.SOUND_TYPE_CAPTURE);
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

        mTextureView.setSurfaceTextureListener((TextureView.SurfaceTextureListener) (new TextureView.SurfaceTextureListener() {
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
                preHeight = height;
                preWidth = width;
                openCamera();
            }
        }));

    }


    public void openCamera() {
        //创建相机的管理者
        mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        //设置相机的特征信息；
        setCameraInfo(mCameraManager);

        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mActivity, "没有相机劝降", Toast.LENGTH_SHORT);
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
    public void setCameraInfo(CameraManager mCameraManager) {


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
                } else {

                    //设备等于前置；
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

            // 预览集合中的数据；
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

    }


    /***
     * 匹配合适的预览尺寸
     * @param choices
     * @param width
     * @param height
     * @param aspectRatio
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            //没有合适的预览尺寸
            return choices[0];
        }
    }


    /****
     * 创建图片读取器
     * @param width
     * @param height
     */
    private void
    initImageAvailableListener(int width, int height) {
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
                //获取存储图片的Uri 路径，创建文件夹的同时声明文件的名字
                Uri uri = MediaStoreSave.insertPicUri(timestamp + ".jpg", "image/jpeg", "DCIM/text", MyApplication.getContext());
                //通过URi 获取到输入输出流 进行读写操作
                MediaStoreSave.SavePic(uri, bytes);
                //查询到Uri 然后将图片写入DOWNLOAD目录下面；
                Uri imageIns = MediaStoreSave.getImageIns(MyApplication.getContext(), timestamp + ".jpg");
                copyFile(MyApplication.getContext(), imageIns);
                //前置时左右翻转时处理，后置是正常的，不需要处理了
              /*      if ("1".equals(mCameraId)){
                        //读取图片的角度；
                        int degree = readPictureDegree(picfile);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                      //  旋转后的bitmap //生成图片时角度会消失所以我们需要提前转正；
                        Bitmap rotaingBitamp = BitmapUtils.rotaingBitamp(degree, bitmap);
                        //镜像处理
                        bitmap = BitmapUtils.mirror(rotaingBitamp);
                        BitmapUtils.saveBitmap(bitmap,timestamp+".jpg");
                    }*/
            }
        }, mCameraHandler);


    }


    /**
     * 将文件写入应用公共目录Download
     *
     * @param mxc
     * @param
     * @param insertUri
     * @return
     */
    public static boolean copyFile(Context mxc, final Uri insertUri) {

        File file = null;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath() + "/123.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (insertUri == null) {
            return false;
        }
        ContentResolver resolver = mxc.getContentResolver();
        InputStream is = null;//输入流
        OutputStream os = null;//输出流
        try {
            is = resolver.openInputStream(insertUri);
            os = new FileOutputStream(file); // 读入原文件
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


    /**
     * 输出输入进行读写操作
     *
     * @param os
     * @param is
     * @return
     */
    private static boolean copyFileWithStream(OutputStream os, InputStream is) {
        if (os == null || is == null) {
            return false;
        }
        int read = 0;
        while (true) {
            try {
                byte[] buffer = new byte[1444];
                while ((read = is.read(buffer)) != -1) {
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


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
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
                    Log.e("tag", "当前itemSize 宽=" + itemSize.getWidth() + "高=" + itemSize.getHeight());
                    //判断当前Size高度小于屏幕宽度+j*5  &&  判断当前Size高度大于屏幕宽度-j*5
                    if (itemSize.getHeight() < (textureViewWidth + j * 5) && itemSize.getHeight() > (textureViewWidth - j * 5)) {
                        if (selectSize != null) { //如果之前已经找到一个匹配的宽度
                            if (Math.abs(textureViewHeigt - itemSize.getWidth()) < Math.abs(textureViewHeigt - selectSize.getWidth())) { //求绝对值算出最接近设备高度的尺寸
                                selectSize = itemSize;
                                continue;
                            }
                        } else {
                            selectSize = itemSize;
                        }

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("tag", "getMatchingSize2: 选择的分辨率宽度=" + selectSize.getWidth());
        Log.e("tag", "getMatchingSize2: 选择的分辨率高度=" + selectSize.getHeight());
        return selectSize;
    }


    /**
     * 摄像头状态接口回调类:
     * 主要是负责回调摄像头的开启/断开/异常/销毁.我们使用CameraManager打开指定id的摄像头时需要添加这个回调.
     */

    public CameraDevice.StateCallback mStateCallback = new android.hardware.camera2.CameraDevice.StateCallback() {

        /**
         * 摄像头打开时
         * @param camera
         */
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            CameraDevice = camera;
            //创建session
            creatSession();

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
     * <p>
     * * 通过cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) 创建一个用于预览的Builder对象
     * * 为该Builder对象添加一个Surface对象，并设置各种相关参数
     * * 通过cameraDevice.createCaptureSession创建一个会话，第一个参数中传了一个 surface 和 mImageReader?.surface。这表明了这次会话的图像数据的输出到这两个对象
     * * 当会话创建成功时，通过 session.setRepeatingRequest(captureRequestBuilder.build(), mCaptureCallBack, mCameraHandler) 发起预览请求
     * *
     */


    public class MyOrientationEventListener extends OrientationEventListener {

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
                Log.e("iphone", iphone + "");
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

            return 0;
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
            Log.e("iphone", sensorOrientation + "");
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
     * 创建session ，主要设置surface 数据流操作；
     */
    private void creatSession() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        //设置显示预览大小的尺寸；
        mSurfaceTexture.setDefaultBufferSize(1920, 1080);
        //获取Surface显示预览数据
        previewSurface = new Surface(mSurfaceTexture);
        try {

            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            CameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                //配置预览成功；
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    //请求预览；
                    takePreview();
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

    /***
     * 发起预览请求；
     */

    private void takePreview() {

        try {
            //创建预览请求的携带参数builder;
            mpreviewRequest = CameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //预览的surface；
            mpreviewRequest.addTarget(previewSurface);
            //预览请求设置下发参数；
            mpreviewRequest.set(BM_ISSUED,bmRoi);


            //发起重复预览请求；
            mCameraCaptureSession.setRepeatingRequest(mpreviewRequest.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /***
     * 捕获的实时回调；
     */
    public CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {



        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            //捕获开始的时候添加声音；
            cameraActionSound.play(CameraActionSound.SOUND_TYPE_CAPTURE);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            // 重设自动对焦模式
            captureRequest.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            // 设置自动曝光模式
            captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            captureRequest.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            if (result != null) {
                //自动曝光（AE）目标图像亮度的调整值；
                Integer integer1 = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION);
                //ISO感光灵敏度
                Integer integer2 = result.get(CaptureResult.SENSOR_SENSITIVITY);
                //每个像素的曝光时间。
                Long aLong = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                //获取底层返回来的数据；
                int[]  Roi= result.get(BM_RESULT);
                Log.e("BM_GET_DATA", "x y :" + Roi[0]+"-----"+Roi[1] +":w ===h :"+Roi[2]+"---"+Roi[3]);
            }

        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }



    };

    /***
     * 拍照；
     * @throws CameraAccessException
     */
    public void takePic() throws CameraAccessException {
        if (CameraDevice == null || !mTextureView.isAvailable() || !canTakePic) {
            return;
        }
        // 创建一个适合于静态图像捕获的请求builder
        captureRequest = CameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        // 自动对焦
        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        updateJpegOrientation(iphone);
        //设置图片方向；
        captureRequest.set(CaptureRequest.JPEG_ORIENTATION, mJpegOrientation);
        //拍照后返回数据用到的surface;
        Surface surface = mImageReader.getSurface();
        captureRequest.addTarget(surface);
        // 停止连续取景
       // mCameraCaptureSession.stopRepeating();
        //发起拍照请求
        mCameraCaptureSession.capture(captureRequest.build(), CaptureCallback, mCameraHandler); //获取拍照

    }


    /***
     * 长按连拍20张图片；
     * @param tv_num
     */
    public void takePicBurst(final TextView tv_num) throws CameraAccessException {
        if (CameraDevice == null || !mTextureView.isAvailable() || !canTakePic) {
            return;
        }
        //创建CaptureRequest 请求列表；
        ArrayList<CaptureRequest> captureList = new ArrayList<>();

        for (int i = 0; i <= 20; i++) {
            CaptureRequest.Builder captureBuilder = CameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            //自动对焦；
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            updateJpegOrientation(iphone);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mJpegOrientation);
            Surface surface = mImageReader.getSurface();
            captureBuilder.addTarget(surface);

            captureList.add(captureBuilder.build());

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
            int mPictureCounter = 0;

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                Log.d("peng", "正在存第-----" + mPictureCounter + "张");
                mPictureCounter++;
                if (mPictureCounter >= 20) {
                    tv_num.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_num.setText("" + mPictureCounter);
                        }
                    });

                }

            }
        };

        mCameraCaptureSession.stopRepeating();
        mCameraCaptureSession.captureBurst(captureList, CaptureCallback, null);
    }

    /**
     * 摄像头获取会话数据回调监听；
     */
    public CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            //捕获开始的时候添加声音；
            cameraActionSound.play(CameraActionSound.SOUND_TYPE_CAPTURE);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            // 设置自动对焦模式
            // Log.e("dengbaocheng_app_pre_xiafa :", "ldc" + 12);

            // mCaptureRequest.set(CONTROL_ENABLE_LDC,12);
            // 重设自动对焦模式
            captureRequest.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            // 设置自动曝光模式
            captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            captureRequest.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            if (result != null) {
                //自动曝光（AE）目标图像亮度的调整值；
                Integer integer1 = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION);
                //ISO感光灵敏度
                Integer integer2 = result.get(CaptureResult.SENSOR_SENSITIVITY);
                //每个像素的曝光时间。
                Long aLong = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            }

        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

    };

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                //遍历camera所有id 拿到相应的特征；
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                if (currentCameraId == CameraCharacteristics.LENS_FACING_BACK && characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    currentCameraId = CameraCharacteristics.LENS_FACING_FRONT;
                    CameraDevice.close();
                    openCamera();
                    break;
                } else if (currentCameraId == CameraCharacteristics.LENS_FACING_FRONT && characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    currentCameraId = CameraCharacteristics.LENS_FACING_BACK;
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
    public void releaseCamera() {
        if (myOrientationEventListener != null)
            myOrientationEventListener.disable();
        //移除surface，
        if (mpreviewRequest != null) {
            mpreviewRequest.removeTarget(previewSurface);
            mpreviewRequest = null;
        }
        if (previewSurface != null) {
            previewSurface.release();
            previewSurface = null;
        }


        if (surfaceTexture != null) {
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
        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }

        mCameraManager = null;
        mSessionCaptureCallback = null;
        mStateCallback = null;


    }


}

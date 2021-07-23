package com.study.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.study.camera2.view.AutoFitTextureView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/***
 *camera2 +MediaRecorder  实现视频录制 同时 实现录制过程不重新创建session 的方式；
 */
public class CameraMediaRecorderHelper {
    String TAG = "CameraMediaRecorderHelper";
    //传感器方向；
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();


    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private Activity mActivity;
    private AutoFitTextureView mTextureView;
    private CameraManager mCameraManager;
    private android.hardware.camera2.CameraDevice CameraDevice = null;
    //创建的session
    private CameraCaptureSession mCameraCaptureSession = null;
    private String mCameraId = "0";
    //传感器方向；
    private Integer mSensorOrientation;

    //Camrea 的一些属性
    private CameraCharacteristics mCameraCharacteristics;
    //默认使用前置摄像头
    private int currentCameraId = CameraCharacteristics.LENS_FACING_FRONT;
    private Handler mCameraHandler;
    //创建线程
    private HandlerThread handlerThread;
    private SurfaceTexture surfaceTexture;
    //设置预览流surface
    private Surface mSurface;
    //获取到最佳预览尺寸；
    private Size previewSize;
    //获取到最佳视频尺寸；
    private Size mVideoSize;
    int preWidth;
    int preHeight;
    private MediaRecorder mMediaRecorder;
    //mediaRecorder 使用的surface 只能用mediaCodec 进行创建；
    private Surface mRecorderSurface;
    //视频会话捕获请求
    private CaptureRequest.Builder mVideoBuilder;
    //视频录制状态的标记
    private boolean mIsRecordingVideo;
    private FileDescriptor fd;
    private Cursor cursor;

    @SuppressLint("NewApi")
    public CameraMediaRecorderHelper(Activity mActivity, AutoFitTextureView mTextureView) {
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
            Toast.makeText(mActivity, "没有相机权限", Toast.LENGTH_SHORT);
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
            //传感器方向；
            mSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            // 预览集合中的数据；
            Size[] preSizes = map.getOutputSizes(SurfaceTexture.class);
            // 获取最佳的预览尺寸
            // Size previewSize =chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), pictureSize.getWidth(), pictureSize.getHeight(), 1080, 1920);
            previewSize = getMatchingPreSize(preSizes);

            //录制视频可用的尺寸大小
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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
            //当camera打开成功返回可用cameradevice 时，我们去创建session;
            createSession();
        }

        /***
         * 摄像头断开；
         * @param camera
         */
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            CameraDevice.close();
            CameraDevice = null;
        }

        /***
         * 摄像头出现异常操作
         * @param camera
         * @param error
         */
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            CameraDevice.close();
            CameraDevice = null;
            if (null != mActivity) {
                mActivity.finish();
            }
        }


    };


    /***
     *  创建session
     *
     */
    private void createSession() {
        if (null == CameraDevice || !mTextureView.isAvailable() || null == previewSize) {
            return;
        }
        mSurface = new Surface(mTextureView.getSurfaceTexture());
        try {
            //在mediaRecorder prepare()之后这个设置的mRecorderSurface 才可以使用,相当于我们创建
            //session 时就就初始化了一次mediaRecorder,并且这个mediRecorder状态到了prepare()时这个surface 才可以使用
            initMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            //设置缓冲区预览的大小
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            //发起创建session 的请求 预览只需要一个surface
            CameraDevice.createCaptureSession(Arrays.asList(mSurface, mRecorderSurface), sessionStateCb, mCameraHandler);

        } catch (CameraAccessException | IllegalStateException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /****
     * 创建session 的回调
     */
    private CameraCaptureSession.StateCallback sessionStateCb = new CameraCaptureSession
            .StateCallback() {

        /***
         * session 创建成功所走的回调函数；
         * @param session
         */
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "session onConfigured id: " + session.getDevice().getId());
            mCameraCaptureSession = session;
            //发送视频预览请求；
            sendVideoPreviewRequest();
        }

        /***
         * session 创建失败的回调；
         * @param session
         */
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "create session fail id: " + session.getDevice().getId());
        }
    };

    /****
     * 发送视频录制的会话请求。使其重复进行捕获
     */
    private void sendVideoPreviewRequest() {
        try {
            mVideoBuilder = CameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            //预览的surface加入到管道；
            mVideoBuilder.addTarget(mSurface);
            //加入到管道中,视频录制的surface
            mVideoBuilder.addTarget(mRecorderSurface);
            mVideoBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            mVideoBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(30, 30));
            //进行重复请求预览
            mCameraCaptureSession.setRepeatingRequest(mVideoBuilder.build(), CaptureCallback, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /****
     * 设置捕获的监听器；
     */
    public CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            //super.onCaptureCompleted(session, request, result);
            if (result != null) {
                //可以从result 获取到一些底层返回的数据结果；如AE的状态什么的；

                Integer integer = result.get(CaptureResult.CONTROL_AE_STATE);
            }
        }


    };

/*************************------------------录制的方法----------------*************************************/

    /***
     * mediaRecorder 初始化的配置
     * @throws IOException
     */
    public void initMediaRecorder() throws IOException {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
            //创建surface
            mRecorderSurface = MediaCodec.createPersistentInputSurface();
        } else {
            // 重置之前的实例
            mMediaRecorder.reset();

        }
        if (mRecorderSurface != null) {
            // 设置surface
            mMediaRecorder.setInputSurface(mRecorderSurface);
        }

        //视频源,意思是从Surface里面读取画面去录制
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //音频源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //  mMediaRecorder.setProfile(mProfile);
        //输出格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //捕获率
        mMediaRecorder.setCaptureRate(30);
        //帧率
        mMediaRecorder.setVideoFrameRate(30);
        //码率
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        // 视频编码格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //音频编码格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //视频宽高
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        //文件最大设置
        // mMediaRecorder.setMaxFileSize(getMaxFileSize());
        //设置视频录制输出的方向方向；
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }

        long timestamp = System.currentTimeMillis();
        fd = getSaveToGalleryVideoOutputStream(MyApplication.getContext(), timestamp + ".mp4", "video/mp4", "/ucam");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //输出的目录设置；
            mMediaRecorder.setOutputFile(fd);
        } else {
            //设置file 路径目前先用应用私有目录代替， 因为10 以上版本只有这个目录可以创建file
            //输出的目录设置；

            mMediaRecorder.setOutputFile(getVideoFilePath(mActivity));
        }
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "media recorder prepared!");
    }


    /***
     * 开始录制视频；
     */
    public void startRecordingVideo() {
        if (null == CameraDevice || !mTextureView.isAvailable() || null == previewSize) {
            return;
        }
        Log.d(TAG, "edc start video session");
        try {
            mIsRecordingVideo = true;
            //开启子线程，为了防止卡顿出现
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //初始化mediaRecorde；
                        initMediaRecorder();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //开始进行录制；
                    mMediaRecorder.start();
                }
            }).start();

        } catch (Exception e) {
            mIsRecordingVideo = false;
            e.printStackTrace();
        }

    }

    /***
     * 停止视频录制；
     */

    public void stopRecordingVideo() {
        mIsRecordingVideo = false;
        if (mMediaRecorder != null) {
            Log.d(TAG, "stop media recorder");
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        //获取视频的缩略图
        Bitmap bitmap = getVideoThumbnail(mActivity.getContentResolver(), uri);
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
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
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
        sessionStateCb = null;
        mStateCallback = null;


    }

/**************************辅助方法*************************************/

    /***
     * 获取到最佳的预览尺寸；
     * @param previewSize
     * @return
     */
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


    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e("chooseVideoSize", "Couldn't find any suitable video size");
        return choices[choices.length - 1];


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
     * 判断是否在录制状态；
     * @return
     */
    public boolean ismIsRecordingVideo() {
        return mIsRecordingVideo;
    }


    /****************----------------------------分区存储-----------------------------*******************/

    /**
     *
     * 说明：
     * 此方法中MediaStore默认的保存目录是/storage/emulated/0/video
     * 而Environment.DIRECTORY_MOVIES的目录是/storage/emulated/0/Movies
     * 获取存储图片的Uri 路径，创建文件夹的同时声明文件的名字
     * @param context
     * @return
     */


    /**
     * 返回文件描述符
     *
     * @param context
     * @param videoName
     * @param mineType
     * @return
     * @throws
     */
    Uri uri;

    public FileDescriptor getSaveToGalleryVideoOutputStream(@NonNull Context context, @NonNull String videoName, @NonNull String mineType, String subDir) throws FileNotFoundException {

        //大于29 进行分区存储
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //最后一个参数创建子目录；
            uri = getSaveToGalleryVideoUri(context, videoName, mineType, subDir);
            if (uri == null)
                return null;
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "w");
            FileDescriptor mfileDescriptor = fileDescriptor.getFileDescriptor();
            return mfileDescriptor;
        } else {

            return null;
        }


    }

    /***
     *
     * @param context  上下文
     * @param videoName  视频文件名字带有后缀名如 :"123.mp4"
     * @param mineType  视频格式类型如："video/mp4"
     * @param subDir   创建的子目录如："/ucam"
     * @return uri
     */
    public Uri getSaveToGalleryVideoUri(Context context, String videoName, String mineType, String subDir) {
        ContentValues values = new ContentValues();
        //视频的名字
        values.put(MediaStore.Video.Media.DISPLAY_NAME, videoName);
        //视频类型；
        values.put(MediaStore.Video.Media.MIME_TYPE, mineType);
        //修改时间
        values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //大于等于10 的版本设置子目录
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + subDir);
        } else {
            //数据库表中列字段存储的是数据的绝对路径；android 10 之前，将绝对路径设置到这个key中；这个字段可能在后面的数据库查询可能会用到；
            values.put(MediaStore.MediaColumns.DATA, getpath());
        }
        Uri mInsert = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        return mInsert;

    }


    /***
     * 录制视频的保存路径；
     * 需要先想明白需要存的数据是属于app私有的还是需要分享的，如果是app私有的，存在getExternalFilesDir()返回的文件夹下，
     * 也就是Android/data/包名/files/文件夹；如果是需要分享的，
     * 需要采用媒体库（MediaStore）的方式来存取，后面会讲怎么存取。
     * 需要指出的是在分区存储模型下存取共享媒体文件是不需要存储权限的，
     * 而旧的存储模型是需要存储权限的。
     * @param context
     * @return
     */
    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    /***
     * 在Android10 之前将创建的绝对路径存放到 数据库表中关键字是：_data的列中
     * @return
     */
    public String getpath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "ucamera" + File.separator + "123.mp4";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        return path;
    }


    /***
     * 我们通过cursor 查询出来的path 可以进行new file 操作；
     * @param cr
     * @param uri
     * @return
     */
    //获取缩略图；
    public Bitmap getVideoThumbnail(ContentResolver cr, Uri uri) {
        query(cr);
        Cursor cursor = null;
        String path;
        try {
            //数据库表中_data列的字段
            String[] proj = {MediaStore.Video.Media.DATA};
            //查询获取_data这一列的cursor对象，
            cursor = cr.query(uri, proj, null, null, null);
            StringBuilder res = new StringBuilder();
            //获取到列的索引
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            res.append(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA) + "").append("\t\t");
            //表示指向查询到的列的第一行数据；
            cursor.moveToFirst();
            res.append("\n");
            //取出第一行这一列索引的数据
            path = cursor.getString(column_index);
            res.append(cursor.getString(column_index)).append("\t\t");
            Log.e("deng", "createCursor: getvideothumbnail " + cr + " count " + cursor.getCount() + " \n" + res);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
    }

    /***
     * 查询video 表中的所有数据
     * @param cr
     */
    public void query(ContentResolver cr) {
        //按照修改时间进行排序；
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        //查询video表获取cursor对象;
        Cursor c = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, sortOrder);
        //获取总共有多少列数据
        int colnum = c.getColumnCount();
        StringBuilder res = new StringBuilder();
        //遍历获取列的字段名字和列的索引进行打印;因为默认cursor
        for (int i = 0; i < colnum; i++) {
            //"\t" 表示一个tab
            res.append(c.getColumnName(i)).append(c.getColumnIndex(c.getColumnName(i))).append("\t");
        }
        res.append("\n");
        //将cursor 移动到数据的第一行
        while (c.moveToNext()) {
            for (int i = 0; i < colnum; i++) {
                try {
                    // 打印每一列中的第一行数据
                    res.append(c.getString(i)).append("\t");
                } catch (Exception e) {
                    res.append(c.getType(i)).append("\t");
                }

            }

            res.append("\n");
        }
        //移动光标到第一行
        c.moveToFirst();
        //log打印出我们查询的数据库表数据；
        Log.d("deng", "zzzh createCursor: addImage " + cr + " count " + c.getCount() + " \n" + res);
    }

    /***
     * 根据文件名字，删除应用自己创建的文件；
     * @param contentResolver
     * @param name
     */
   //_display_name  删除名字 为 这个的数据 1609835774439.mp4
    public void delete(ContentResolver contentResolver,String name) {
        try {

            //根据日期降序查询
            String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
            //查询条件 根据名字进行查询；
            String selection = MediaStore.Images.Media.DISPLAY_NAME + "='" + name + "'";
            //查询获取cursor，所以相当于找带有name的这一行,只显示两列 分别为_id _display_name ;
            cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.VideoColumns._ID,MediaStore.Images.Media.DISPLAY_NAME}, selection, null, sortOrder);

            if (cursor != null ) {
                //获取字段为_id 的这个字段列的索引
                int columnId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                //移动到第一行数据
                cursor.moveToFirst();
                //获取第一行列字段_id 下面的数据
                int mediaId = cursor.getInt(columnId);
                //根据_id 生成uri;
                Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId);
                contentResolver.delete(uri,null,null);
            }
        }catch (Exception e){
            e.toString();
        }
        cursor.close();
    }

    /***
     * 根据文件名字，删除应用自己创建的文件；
     * @param contentResolver
     * @param name
     */
    //_display_name  删除名字 为 这个的数据 1609835774439.mp4
    public void otherDatadelete(ContentResolver contentResolver,String name) {
        try {

            //根据日期降序查询
            String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
            //查询条件 根据名字进行查询；
            String selection = MediaStore.Images.Media.DISPLAY_NAME + "='" + name + "'";
            //查询获取cursor，所以相当于找带有name的这一行,只显示两列 分别为_id _display_name ;
            cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.VideoColumns._ID,MediaStore.Images.Media.DISPLAY_NAME}, selection, null, sortOrder);

            if (cursor != null ) {
                //获取字段为_id 的这个字段列的索引
                int columnId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                //移动到第一行数据
                cursor.moveToFirst();
                //获取第一行列字段_id 下面的数据
                int mediaId = cursor.getInt(columnId);
                //根据_id 生成uri;
                Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId);
                contentResolver.delete(uri,null,null);
            }
        }catch (RecoverableSecurityException e){
            e.toString();
            cursor.close();
        }
        cursor.close();
    }



}